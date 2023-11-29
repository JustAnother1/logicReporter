package de.nomagic.swd.packets;

import java.io.PrintStream;

public abstract class RequestPacket extends SwdPacket
{
    protected boolean isDP;
    protected boolean isRead;
    protected int a2a3;
    protected int requestParity;

    protected abstract String specificReport();

    @Override
    public void reportTo(PrintStream out)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("SWD Packet(");
        if(true == isDP)
        {
            buf.append("DP,");
        }
        else
        {
            buf.append("AP,");
        }
        if(true == isRead)
        {
            buf.append("Reading,");
        }
        else
        {
            buf.append("Writing,");
        }
        buf.append("a23=" + a2a3);
        buf.append(",parity=" + requestParity);
        buf.append(specificReport());
        buf.append(")");
        out.println(buf.toString());
    }

    public void setisDp(boolean isDP)
    {
        this.isDP = isDP;
    }

    public void setisRead(boolean isRead)
    {
        this.isRead = isRead;
    }

    public void setA2A3(int a2a3)
    {
        this.a2a3 = a2a3;
    }

    public void setRequestParity(int parity)
    {
        requestParity = parity;
    }

}
