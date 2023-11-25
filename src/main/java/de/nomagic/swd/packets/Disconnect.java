package de.nomagic.swd.packets;

import java.io.PrintStream;

public class Disconnect extends SwdPacket
{
    public Disconnect()
    {
    }

    @Override
    public void reportTo(PrintStream out)
    {
        out.println("SWD - Disconnect");
    }

}
