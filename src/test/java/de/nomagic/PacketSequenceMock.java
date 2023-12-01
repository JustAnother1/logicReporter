package de.nomagic;

import java.util.Vector;

import de.nomagic.swd.packets.SwdPacket;

public class PacketSequenceMock implements PacketSequence
{
    private Vector<SwdPacket> packets = new Vector<SwdPacket>();

    public PacketSequenceMock()
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
