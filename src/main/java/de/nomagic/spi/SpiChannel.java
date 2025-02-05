package de.nomagic.spi;

import de.nomagic.Channel;
import de.nomagic.Configuration;
import de.nomagic.logic.SampleSource;

public class SpiChannel
{
    private final Channel chan;
    private final SampleSource samples;
    private final boolean valid;

    public SpiChannel(Channel ChannelTyp, Configuration cfg)
    {
        chan = ChannelTyp;
        samples = cfg.get_channel(chan);
        if(false == samples.isValid())
        {
            valid = false;
            return;
        }
        valid = true;
    }

    public boolean isValid()
    {
        return valid;
    }

    public String getName()
    {
        return chan.name;
    }

    public long getNumberOfEdges()
    {
        return samples.getNumberEdges();
    }

    public Channel getChannel()
    {
        return chan;
    }

    public boolean isHighAt(double d)
    {
        return samples.isHighAt(d);
    }

    public double getTimeOfEdgeAfter(double now_time)
    {
        return samples.getTimeOfEdgeAfter(now_time);
    }

}
