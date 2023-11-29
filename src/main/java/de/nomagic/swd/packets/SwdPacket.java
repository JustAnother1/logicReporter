package de.nomagic.swd.packets;

import java.io.PrintStream;

public abstract class SwdPacket
{

    public SwdPacket()
    {
    }

    public abstract void reportTo(PrintStream out);

}
