package de.nomagic.swd.packets;

public class OkPacket extends RequestPacket
{
    private long data;
    private int dataParity;

    public OkPacket()
    {
    }

    protected String specificReport()
    {
        return ",ACK=OK,data=" + String.format("0x%08X", data) + ", dpararty=" + dataParity;
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
