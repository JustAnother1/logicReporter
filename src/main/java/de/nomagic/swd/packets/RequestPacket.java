package de.nomagic.swd.packets;

import java.io.PrintStream;

public abstract class RequestPacket extends SwdPacket
{
    protected boolean isDP;
    protected boolean isRead;
    protected int a2a3;
    protected int requestParity;

    public RequestPacket()
    {
        // TODO Auto-generated constructor stub
    }

    public void reportRequestTo(PrintStream out)
    {
        // TODO Auto-generated method stub

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
