package de.nomagic.swd.packets;

import java.io.PrintStream;

public class LineReset extends SwdPacket
{

    public LineReset()
    {
    }

    @Override
    public void reportTo(PrintStream out)
    {
        out.println("\r\n\r\n\r\n\r\nLine Reset");
    }

}
