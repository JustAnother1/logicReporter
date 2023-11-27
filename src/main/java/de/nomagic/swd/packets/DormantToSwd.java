package de.nomagic.swd.packets;

import java.io.PrintStream;

public class DormantToSwd extends SwdPacket
{

    public DormantToSwd()
    {
    }

    @Override
    public void reportTo(PrintStream out)
    {
        out.println("Dormant to SWD");
    }

}
