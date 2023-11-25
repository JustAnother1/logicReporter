package de.nomagic;

import java.util.Vector;

import de.nomagic.swd.packets.SwdPacket;

public class PacketSequence
{
    private Vector<SwdPacket> packets = new Vector<SwdPacket>();

    public PacketSequence()
    {
    }

    public void add(SwdPacket res)
    {
        packets.add(res);
    }

    public SwdPacket getNextPacket()
    {
        if( true == packets.isEmpty())
        {
            return null;
        }
        else
        {
            SwdPacket res = packets.get(0);
            packets.remove(0);
            return res;
        }
    }

}
