package de.nomagic.swd.packets;

import java.io.PrintStream;

public class JtagToSwd extends SwdPacket
{

    public JtagToSwd()
    {
    }

    @Override
    public void reportTo(PrintStream out)
    {
        out.println("switch from JTAG to SWD");
    }


}
