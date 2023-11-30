package de.nomagic.swd.packets;

import java.io.PrintStream;

public class IdleBits extends SwdPacket
{
    private int numBits;

    public IdleBits(int num)
    {
        numBits = num;
    }

    @Override
    public void reportTo(PrintStream out)
    {
        out.println("Idle bits (" + numBits + ")");
    }

}
