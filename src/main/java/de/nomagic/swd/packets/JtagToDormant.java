package de.nomagic.swd.packets;

import java.io.PrintStream;

public class JtagToDormant extends SwdPacket
{
    public JtagToDormant()
    {
    }

    @Override
    public void reportTo(PrintStream out)
    {
        out.println("switch from JTAG to Dormant");
    }

}
