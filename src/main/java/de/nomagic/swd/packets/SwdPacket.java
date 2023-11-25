package de.nomagic.swd.packets;

import java.io.PrintStream;

public abstract class SwdPacket
{

    public SwdPacket()
    {
        // TODO Auto-generated constructor stub
    }

    public abstract void reportTo(PrintStream out);

}
