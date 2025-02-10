package de.nomagic.spi;

import java.io.PrintStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.Channel;
import de.nomagic.Configuration;

public class SpiReporter
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private Configuration cfg;

    private SpiChannel clkChannel;
    private SpiChannel ncsChannel;
    private SpiChannel misoChannel;
    private SpiChannel mosiChannel;
    private SpiChannel io2Channel;
    private SpiChannel io3Channel;

    public SpiReporter(Configuration cfg)
    {
        this.cfg = cfg;
    }

    public boolean reportTo(PrintStream out)
    {
        if((null == out) || (null == cfg))
        {
            log.error("no output stream or data streams provided !");
            return false;
        }

        clkChannel = new SpiChannel(Channel.SPI_CLK, cfg);
        ncsChannel = new SpiChannel(Channel.SPI_nCS, cfg);
        misoChannel = new SpiChannel(Channel.SPI_MISO, cfg);
        mosiChannel = new SpiChannel(Channel.SPI_MOSI, cfg);
        io2Channel = new SpiChannel(Channel.SPI_MOSI, cfg);
        io3Channel = new SpiChannel(Channel.SPI_MOSI, cfg);

        if(false == clkChannel.isValid())
        {
            log.error("signal CLK not valid !");
            return false;
        }
        if(false == ncsChannel.isValid())
        {
            log.error("signal /CS not valid !");
            return false;
        }
        if(false == misoChannel.isValid())
        {
            log.error("signal MISO not valid !");
            return false;
        }
        if(false == mosiChannel.isValid())
        {
            log.error("signal MOSI not valid !");
            return false;
        }
        if(false == io2Channel.isValid())
        {
            log.debug("signal IO2 not valid !");
            io2Channel = null;
        }
        if(false == io3Channel.isValid())
        {
            log.debug("signal IO3 not valid !");
            io3Channel = null;
        }

        long numEdges = clkChannel.getNumberOfEdges();

        if(numEdges < ncsChannel.getNumberOfEdges())
        {
            log.error("signal /CS has more edges than CLK! That makes no sence, right?");
            return false;
        }
        if(numEdges < misoChannel.getNumberOfEdges())
        {
            log.error("signal MISO has more edges than CLK! That makes no sence, right?");
            return false;
        }
        if(numEdges < mosiChannel.getNumberOfEdges())
        {
            log.error("signal MOSI has more edges than CLK! That makes no sence, right?");
            return false;
        }
        if(null != io2Channel)
        {
            if(numEdges < io2Channel.getNumberOfEdges())
            {
                log.error("signal IO2 has more edges than CLK! That makes no sence, right?");
                return false;
            }
        }
        if(null != io3Channel)
        {
            if(numEdges < io3Channel.getNumberOfEdges())
            {
                log.error("signal IO3 has more edges than CLK! That makes no sence, right?");
                return false;
            }
        }

        return parseBits(out);
    }

    /**
     *
     * mode CPOL CPHA write bit                             read bit
     * ---- ---- ---- ------------------------------------- --------------
     * 0    0    0    falling clock, and when /CS activates rising clock
     * 1    0    1    rising clock                          falling clock
     * 2    1    0    rising clock, and when /CS activates  falling clock
     * 3    1    1    falling clock                         rising clock
     *
     *
     * @param out
     * @return
     */
    public boolean parseBits(PrintStream out)
    {
        double now_time = 0.0;
        double next_time = 0.0;
        double edge_time;
        int mode = cfg.getSpiMode();
        boolean nCsIsHigh = ncsChannel.isHighAt(0.0);

        for(long i = 0; i < ncsChannel.getNumberOfEdges(); i++)
        {
            next_time = ncsChannel.getTimeOfEdgeAfter(now_time);
            if(true == nCsIsHigh)
            {
                // slave not selected -> skip all this
            }
            else
            {
                // slave selected -> record data
                boolean clkIsHigh = clkChannel.isHighAt(now_time);
                SpiTransfer transfer = new SpiTransfer(now_time);
                switch(mode)
                {
                case 0:
                case 3:
                    // find all rising clock edges until next time
                    // read Miso + Mosi at these times
                    edge_time = clkChannel.getTimeOfEdgeAfter(now_time);
                    while(edge_time < next_time)
                    {
                        clkIsHigh = !clkIsHigh; // switch to new value
                        if(true == clkIsHigh)
                        {
                            // rising edge
                            if(true == misoChannel.isHighAt(edge_time))
                            {
                                transfer.addMisoBit(1);
                            }
                            else
                            {
                                transfer.addMisoBit(0);
                            }
                            if(true == mosiChannel.isHighAt(edge_time))
                            {
                                transfer.addMosiBit(1);
                            }
                            else
                            {
                                transfer.addMosiBit(0);
                            }
                            if(null != io2Channel)
                            {
                                if(true == io2Channel.isHighAt(edge_time))
                                {
                                    transfer.addIo2Bit(1);
                                }
                                else
                                {
                                    transfer.addIo2Bit(0);
                                }
                            }
                            if(null != io3Channel)
                            {
                                if(true == io3Channel.isHighAt(edge_time))
                                {
                                    transfer.addIo3Bit(1);
                                }
                                else
                                {
                                    transfer.addIo3Bit(0);
                                }
                            }
                        }
                        // else falling edge
                        edge_time = clkChannel.getTimeOfEdgeAfter(edge_time);
                    }
                    break;

                case 1:
                case 2:
                    // find all falling clock edges until next time
                    // read Miso + Mosi at these times
                    edge_time = clkChannel.getTimeOfEdgeAfter(now_time);
                    while(edge_time < next_time)
                    {
                        clkIsHigh = !clkIsHigh; // switch to new value
                        if(true == clkIsHigh)
                        {
                            // ignore rising edge
                        }
                        else
                        {
                            // falling edge
                            if(true == misoChannel.isHighAt(edge_time))
                            {
                                transfer.addMisoBit(1);
                            }
                            else
                            {
                                transfer.addMisoBit(0);
                            }
                            if(true == mosiChannel.isHighAt(edge_time))
                            {
                                transfer.addMosiBit(1);
                            }
                            else
                            {
                                transfer.addMosiBit(0);
                            }
                            if(null != io2Channel)
                            {
                                if(true == io2Channel.isHighAt(edge_time))
                                {
                                    transfer.addIo2Bit(1);
                                }
                                else
                                {
                                    transfer.addIo2Bit(0);
                                }
                            }
                            if(null != io3Channel)
                            {
                                if(true == io3Channel.isHighAt(edge_time))
                                {
                                    transfer.addIo3Bit(1);
                                }
                                else
                                {
                                    transfer.addIo3Bit(0);
                                }
                            }
                        }
                        edge_time = clkChannel.getTimeOfEdgeAfter(edge_time);
                    }
                    break;

                default:
                    log.error("invalid SPI mode of {} !", mode);
                    return false;
                }

                out.println(transfer.toString());
            }
            // finished with this segment
            now_time = next_time;
            nCsIsHigh = !nCsIsHigh;
        }
        return true;
    }


}
