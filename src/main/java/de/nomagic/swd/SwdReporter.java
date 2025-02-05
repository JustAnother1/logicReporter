package de.nomagic.swd;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.Channel;
import de.nomagic.Configuration;
import de.nomagic.logic.SampleSource;
import de.nomagic.logic.ValueDecoder;

public class SwdReporter
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    // recoded bit Stream
    private static final int CLK_FALLING_DATA_0   = 0;
    private static final int CLK_FALLING_DATA_1   = 1;
    private static final int CLK_RISING_DATA_0 = 2;
    private static final int CLK_RISING_DATA_1 = 3;

    private Vector<Integer> edges = new Vector<Integer>();
    private Vector<Double> startTimes = new Vector<Double>();
    private PrintStream out;
    private swdState state;
    private BitStreamCracker bitToPackets;
    private Configuration cfg;
    private boolean report_bit_level = false;


    public SwdReporter(Configuration cfg)
    {
        this.cfg = cfg;
        ValueDecoder valDec = new ValueDecoder();
        String[] translations = cfg.getRegisterTranslationFileNames();
        for(int i = 0; i < translations.length; i++)
        {
            valDec.readTransationsFrom(translations[i]);
        }
        state = new swdState(valDec);
        bitToPackets = new BitStreamCracker(state, valDec);
    }


    public boolean reportTo(PrintStream out) throws IOException
    {
        if((null == out) || (null == cfg))
        {
            log.error("no output stream or data streams provided !");
            return false;
        }
        this.out = out;
        state.reportTo(out);
        state.setConfiguration(cfg);
        if(false == cfg.isValid())
        {
            log.error("configuration or data is invalid");
            return false;
        }
        SampleSource swclk = cfg.get_channel(Channel.SWD_CLK);
        SampleSource swdio = cfg.get_channel(Channel.SWD_IO);
        if(false == swclk.isValid())
        {
            log.error("SWDCLK is invalid");
            return false;
        }
        if(false == swdio.isValid())
        {
            log.error("SWDIO is invalid");
            return false;
        }

        if(swdio.getNumberEdges() >= swclk.getNumberEdges())
        {
            // that can not be right
            log.error("more edges on SWDIO than on SWCLK. That makes no sence, right?");
            return false;
        }
        boolean report_edge_level = cfg.shallReportEdgeLevel();
        report_bit_level = cfg.shallReportBitValues();
        double now_time = 0.0;
        boolean lastStateSwclkHigh = swclk.isHighAt(0.0);
        int nextCheck = 0;
        double lastTime = 0.0;

        for(int i = 0; i < swclk.getNumberEdges(); i++)
        {
            now_time = swclk.getTimeOfEdgeAfter(now_time);
            boolean dataHigh = swdio.isHighAt(now_time);
            if(now_time - lastTime > 0.003)
            {
                double diff = (now_time - lastTime);
                int pause = 0 - (int)(diff * 1000);
                edges.add(pause);
                startTimes.add(lastTime);
            }
            lastTime = now_time;
            if(true == lastStateSwclkHigh)
            {
                // falling edge on SWCLK
                if(true == dataHigh)
                {
                    if(true == report_edge_level) out.append("F1,");
                    edges.add(CLK_FALLING_DATA_1);
                    startTimes.add(now_time);
                }
                else
                {
                    if(true == report_edge_level) out.append("F0,");
                    edges.add(CLK_FALLING_DATA_0);
                    startTimes.add(now_time);
                }
                lastStateSwclkHigh = false;
            }
            else
            {
                // rising edge on SWCLK
                if(true == dataHigh)
                {
                    if(true == report_edge_level) out.append("R1,");
                    edges.add(CLK_RISING_DATA_1);
                    startTimes.add(now_time);
                }
                else
                {
                    if(true == report_edge_level) out.append("R0,");
                    edges.add(CLK_RISING_DATA_0);
                    startTimes.add(now_time);
                }
                lastStateSwclkHigh = true;
            }
            if(i >= nextCheck)
            {
                nextCheck = nextCheck + decodeBits();
            }
        }
        // flush
        // flushing edges
        decodeBits();
        // flushing last bits into packets
        bitToPackets.flush();

        out.println("End of recording");
        // summary
        state.printSummary();
        return true;
    }

    private int decodeBits()
    {
        while(0 < edges.size())
        {
            // data is valid on rising edge
            int curEdge = edges.get(0);
            double startTime = startTimes.get(0);
            if(0 > curEdge)
            {
                int time = Math.abs(curEdge);
                if(1000 > time)
                {
                    out.println("\r\n\r\n\r\nSignal pause of " + time + " ms (start: " + timestamp(startTime) + ")");
                }
                else
                {
                    int sec = time/1000;
                    time = time%1000;
                    out.println("\r\n\r\n\r\nSignal pause of " + sec  + " seconds and " + time + " ms (start: " + timestamp(startTime) + ")");
                }
            }
            else
            {
                switch(curEdge)
                {
                case CLK_FALLING_DATA_1:
                case CLK_FALLING_DATA_0:

                    break;

                case CLK_RISING_DATA_1:
                    bitToPackets.add_one();
                    if(true == report_bit_level) out.append("1,");
                    break;

                case CLK_RISING_DATA_0:
                    bitToPackets.add_zero();
                    if(true == report_bit_level) out.append("0,");
                    break;
                }
            }
            edges.remove(0);
            startTimes.remove(0);
        }
        int numBitsNeeded = 0;
        do
        {
            numBitsNeeded = bitToPackets.detectPackages();
        }while(numBitsNeeded == 0);
        return numBitsNeeded;
    }

    private String timestamp(double time)
    {
        int ms = (int)(time*1000);
        if(1000 > ms)
        {
            // less than a second
            return String.format("(%d ms)", ms);
        }
        else
        {
            if(60*1000 > ms)
            {
                // less than a minute
                int sec = ms/1000;
                ms = ms - (sec*1000);
                return String.format("(%d,%03d s)", sec, ms);
            }
            else
            {
                if(60*60*1000 > ms)
                {
                    // less than an hour
                    int minutes = ms /(60*1000);
                    ms = ms - (minutes * 60 * 1000);
                    int sec = ms/1000;
                    ms = ms - (sec*1000);
                    return String.format("(%d:%d,%03d m:s)", minutes, sec, ms);
                }
                else
                {
                    int hours = ms /(60*60*1000);
                    ms = ms -(hours * 60*60*1000);
                    int minutes = ms /(60*1000);
                    ms = ms - (minutes * 60 * 1000);
                    int sec = ms/1000;
                    ms = ms - (sec*1000);
                    return String.format("(%d:%02d:%d,%03d h:m:s)", hours, minutes, sec, ms);
                }
            }
        }
    }

}
