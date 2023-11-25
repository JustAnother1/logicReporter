package de.nomagic.swd.packets;

import java.io.PrintStream;

public class JtagToSwd extends SwdPacket
{

    public JtagToSwd()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void reportTo(PrintStream out)
    {
        out.println("switch from JTAG to SWD");
    }


}
