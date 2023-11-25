package de.nomagic.swd;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.PacketSequence;
import de.nomagic.SaleaDigitalChannel;
import de.nomagic.swd.packets.SwdPacket;

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
    private PacketSequence packets;

    private int progressCounter = 0;
    private boolean report_progress = true;
    private boolean report_edge_level = false;
    private boolean report_bit_level = false;

    public SwdReporter()
    {
        state = new swdState();
        packets = new PacketSequence();
        bitToPackets = new BitStreamCracker(packets);
    }

    public void setSWDIO(SaleaDigitalChannel swdioChannel)
    {
        swdio = swdioChannel;
    }

    public void setSWCLK(SaleaDigitalChannel swclkChannel)
    {
        swclk = swclkChannel;
    }

    public boolean reportTo(PrintStream out) throws IOException
    {
        if((null == out) || (null == swdio) || (null == swclk))
        {
            log.error("no output stream or data streams provided !");
            return false;
        }
        this.out = out;
        if((false == swclk.isValid()) || (false == swdio.isValid()))
        {
            log.error("data is invalid");
            return false;
        }

        double now_time = 0;
        boolean lastStateSwclkHigh = swclk.isInitiallyHigh();
        int nextCheck = 0;

        for(int i = 0; i < swclk.getNumberEdges(); i++)
        {
            now_time = swclk.getTimeOfEdgeAfter(now_time);
            boolean dataHigh = swdio.isHighAt(now_time);
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
            if(true == report_progress)
            {
                progressCounter++;
                if(100 < progressCounter)
                {
                    out.append("*");
                    progressCounter = 0;
                }
            }
            if(i >= nextCheck)
            {
                nextCheck = nextCheck + decodeBits();
            }
            // for testing only
            if(i > 1000)
            {
                break;
            }
        }
        // flush
        // flushing edges
        decodeBits();
        // flushing last bits into packets
        bitToPackets.flush();
        // flushing last packets
        SwdPacket foundPacket = packets.getNextPacket();
        while(null != foundPacket)
        {
            foundPacket.reportTo(out);
            foundPacket = packets.getNextPacket();
        }

        out.append("End of recording");
        return true;
    }

    private int decodeBits()
    {
        while(0 < edges.size())
        {
            // data is valid on rising edge (TODO?)
            switch(edges.get(0))
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
            edges.remove(0);
        }
        int numBitsNeeded = bitToPackets.detectPackages();
        SwdPacket foundPacket = packets.getNextPacket();
        if(null != foundPacket)
        {
            foundPacket.reportTo(out);
        }
        return numBitsNeeded;
    }

}
