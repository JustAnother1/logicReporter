package de.nomagic.swd.packets;


public class FaultPacket extends RequestPacket
{

    public FaultPacket()
    {
        // TODO Auto-generated constructor stub
    }

    protected String specificReport()
    {
        return ",ACK=Fault";
    }

}
