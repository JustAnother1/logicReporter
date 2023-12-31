package de.nomagic.swd;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.logic.SaleaDigitalChannel;
import de.nomagic.logic.ValueDecoder;

public class SwdReporter
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private SaleaDigitalChannel swdio;
    private SaleaDigitalChannel swclk;

    // recoded bit Stream
    private static final int CLK_FALLING_DATA_0   = 0;
    private static final int CLK_FALLING_DATA_1   = 1;
    private static final int CLK_RISING_DATA_0 = 2;
    private static final int CLK_RISING_DATA_1 = 3;

    private Vector<Integer> edges = new Vector<Integer>();
    private PrintStream out;
    private swdState state;
    private BitStreamCracker bitToPackets;

    private boolean report_edge_level = false;
    private boolean report_bit_level = true;

    public SwdReporter()
    {
        ValueDecoder valDec = new ValueDecoder();
        valDec.readTransationsFrom("arm_cortex_m_registers.txt");
        state = new swdState(valDec);
        bitToPackets = new BitStreamCracker(state, valDec);
    }

    public void setSWDIO(SaleaDigitalChannel swdioChannel)
    {
        swdio = swdioChannel;
    }

    public void setSWCLK(SaleaDigitalChannel swclkChannel)
    {
        swclk = swclkChannel;
    }

    public void setReportEdges(boolean val)
    {
        report_edge_level = val;
    }

    public void setReportBits(boolean val)
    {
        report_bit_level = val;
    }

    public boolean reportTo(PrintStream out) throws IOException
    {
        if((null == out) || (null == swdio) || (null == swclk))
        {
            log.error("no output stream or data streams provided !");
            return false;
        }
        this.out = out;
        state.reportTo(out);
        if((false == swclk.isValid()) || (false == swdio.isValid()))
        {
            log.error("data is invalid");
            return false;
        }

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
            }
            lastTime = now_time;
            if(true == lastStateSwclkHigh)
            {
                // falling edge on SWCLK
                if(true == dataHigh)
                {
                    if(true == report_edge_level) out.append("F1,");
                    edges.add(CLK_FALLING_DATA_1);
                }
                else
                {
                    if(true == report_edge_level) out.append("F0,");
                    edges.add(CLK_FALLING_DATA_0);
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
                }
                else
                {
                    if(true == report_edge_level) out.append("R0,");
                    edges.add(CLK_RISING_DATA_0);
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
            // data is valid on rising edge (TODO?)
            int curEdge = edges.get(0);
            if(0 > curEdge)
            {
                int time = Math.abs(curEdge);
                if(1000 > time)
                {
                    out.println("Signal pause of " + time + " ms");
                }
                else
                {
                    int sec = time/1000;
                    time = time%1000;
                    out.println("Signal pause of " + sec  + " seconds and " + time + " ms");
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
        }
        int numBitsNeeded = 0;
        do
        {
            numBitsNeeded = bitToPackets.detectPackages();
        }while(numBitsNeeded == 0);
        return numBitsNeeded;
    }

}
