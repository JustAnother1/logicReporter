package de.nomagic.swd.packets;

import java.io.PrintStream;

public class SwdToDormant extends SwdPacket
{

    public SwdToDormant()
    {
    }

    @Override
    public void reportTo(PrintStream out)
    {
        out.println("switch from SWD to dormant");
    }

}
