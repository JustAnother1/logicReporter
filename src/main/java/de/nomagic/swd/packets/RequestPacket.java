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
        if(true == isDP)
        {
            switch(a2a3)
            {
            case 0: buf.append("(DPIDR/DPIDR1/BASEPTR0/BASEPTR1)"); break;
            case 1: buf.append("(CTRL-STAT/DLCR/DLPIDR/EVENTSTAT/SELECT1/TARGETID)"); break;
            case 2: buf.append("(SELECT/RESEND)"); break;
            case 3: buf.append("(RDBUFF/TARGETSEL)"); break;
            default: buf.append("(invalid)"); break;
            }
        }
        else
        {
            switch(a2a3)
            {
            case 0: buf.append("(CSW/BD0/MBT/T0RT/CFG1)"); break;
            case 1: buf.append("(TAR/BD1/CFG)"); break;
            case 2: buf.append("(BD2/BASE)"); break;
            case 3: buf.append("(DRW/BD3/IDR)"); break;
            default: buf.append("(invalid)"); break;
            }
        }
        buf.append(",parity=" + requestParity);
        buf.append(specificReport());
        buf.append(")");
        out.println(buf.toString());
    }

    public void setisDp(boolean isDP)
    {
        this.isDP = isDP;
    }

    public boolean getIsDp()
    {
        return isDP;
    }

    public void setisRead(boolean isRead)
    {
        this.isRead = isRead;
    }

    public boolean getIsRead()
    {
        return isRead;
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
