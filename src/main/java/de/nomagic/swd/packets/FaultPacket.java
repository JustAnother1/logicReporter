package de.nomagic.swd.packets;

import java.io.PrintStream;

public class FaultPacket extends RequestPacket
{

    public FaultPacket()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void reportTo(PrintStream out)
    {
        reportRequestTo(out);
    }

}
