package de.nomagic;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwdReporter
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private SaleaDigitalChannel swdio;
    private SaleaDigitalChannel swclk;

    // state:


    // recoded bit Stream
    private static final int CLK_FALLING_DATA_0   = 0;
    private static final int CLK_FALLING_DATA_1   = 1;
    private static final int CLK_RISING_DATA_0 = 2;
    private static final int CLK_RISING_DATA_1 = 3;

    private Vector<Integer> edges = new Vector<Integer>();
    private Vector<Integer> bits = new Vector<Integer>();
    private PrintStream out;

    public SwdReporter()
    {

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
                    // out.append("F1,");
                    edges.add(CLK_FALLING_DATA_1);
                }
                else
                {
                    // out.append("F0,");
                    edges.add(CLK_FALLING_DATA_0);
                }
                lastStateSwclkHigh = false;
            }
            else
            {
                // rising edge on SWCLK
                if(true == dataHigh)
                {
                    // out.append("R1,");
                    edges.add(CLK_RISING_DATA_1);
                }
                else
                {
                    // out.append("R0,");
                    edges.add(CLK_RISING_DATA_0);
                }
                lastStateSwclkHigh = true;
            }
            if(i >= nextCheck)
            {
                nextCheck = nextCheck + decodeBits();
            }
        }
        decodeBits(); // decode last bits
        out.append("End of recording");
        return true;
    }

    private int decodeBits()
    {
        if(0 < edges.size())
        {
            // data is valid on rising edge (TODO?)
            switch(edges.get(0))
            {
            case CLK_FALLING_DATA_1:
            case CLK_FALLING_DATA_0:

                break;

            case CLK_RISING_DATA_1:
                bits.add(1);
                out.append("1,");
                break;

            case CLK_RISING_DATA_0:
                bits.add(0);
                out.append("0,");
                break;
            }
            edges.remove(0);
        }
        // disconnect at least 8x0

        // Packet -> wait or Fail -> 8bit + 3 bit

        // paket read or write -> 8bit + 3 bit + 33 bit

        // JTAG to SWD = 16 bit =  (16bit) 0111 1001 1110 0111

        // SWD to dormant = at least 50 bits 1 + (16bit) 0011 1101 1100 0111

        // dormant to SWD = at least 8x1 + (128 bit) 0100 1001 1100 1111 1001 0000 0100 0110 1010 1001 1011 0100 1010 0001 0110 0001 1001 0111 1111 0101 1011 1011 1100 0111 0100 0101 0111 0000 0011 1101 1001 1000 0000 0101 1000 + at least 50x1

        // JTAG to dormant = at least 5x1 + (31bit) 010 1110 1110 1110 1110 1110 1110 0110

        // Line Reset at least 50x1 and 2x0
        return 1;
    }

}
