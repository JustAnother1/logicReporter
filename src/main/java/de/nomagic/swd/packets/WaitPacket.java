package de.nomagic.swd.packets;

import java.io.PrintStream;

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
