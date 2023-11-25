package de.nomagic.swd.packets;

import java.io.PrintStream;

public class IdleBits extends SwdPacket
{

    public IdleBits()
    {
    }

    @Override
    public void reportTo(PrintStream out)
    {
        out.println("Idle bits");
    }

}
