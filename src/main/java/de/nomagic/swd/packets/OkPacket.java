package de.nomagic.swd.packets;

import java.io.PrintStream;

public class OkPacket extends RequestPacket
{
    private long data;
    private int dataParity;

    public OkPacket()
    {
    }

    protected String specificReport()
    {
        return ",ACK=OK,data=" + data + ", dpararty=" + dataParity;
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
