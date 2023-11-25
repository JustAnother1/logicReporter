package de.nomagic.swd.packets;

import java.io.PrintStream;

public class OkPacket extends RequestPacket
{
    private long data;
    private int dataParity;

    public OkPacket()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void reportTo(PrintStream out)
    {
        reportRequestTo(out);
    }

    public void setData(long data)
    {
        this.data = data;
    }

    public void setDataParity(Integer val)
    {
        this.dataParity = val;
    }

}
