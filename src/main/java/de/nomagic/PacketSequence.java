package de.nomagic;

import de.nomagic.swd.packets.SwdPacket;

public interface PacketSequence
{
    void add(SwdPacket packetPart);
}
