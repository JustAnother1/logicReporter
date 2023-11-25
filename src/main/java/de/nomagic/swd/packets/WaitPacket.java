package de.nomagic.swd.packets;

import java.io.PrintStream;

public class WaitPacket extends RequestPacket
{

    public WaitPacket()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void reportTo(PrintStream out)
    {
        reportRequestTo(out);
    }
}
