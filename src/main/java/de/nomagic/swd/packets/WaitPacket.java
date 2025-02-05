package de.nomagic.swd.packets;

public class WaitPacket extends RequestPacket
{

    public WaitPacket()
    {
    }

    protected String specificReport()
    {
        return ",ACK=WAIT";
    }
}
