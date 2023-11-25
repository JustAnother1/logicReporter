package de.nomagic.swd.packets;

import java.io.PrintStream;
import java.util.Vector;

public class InvalidBits extends SwdPacket
{
    private Vector<Integer> bits = new Vector<Integer>();

    public InvalidBits()
    {
    }

    @Override
    public void reportTo(PrintStream out)
    {
        if(0 < bits.size())
        {
            out.print("ERROR: INVALID BITS -->");
            for(int i = 0; i < bits.size(); i++)
            {
                if(0 == bits.get(i))
                {
                    out.print("0");
                }
                else
                {
                    out.print("1");
                }
                out.println(" <-- INVALID BITS !!!");
            }
        }
        else
        {
            out.println("ERROR: INVALID BITS !!!");
        }
    }

    public void add(Integer val)
    {
        bits.add(val);
    }

}
