package de.nomagic.swd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import de.nomagic.PacketSequence;
import de.nomagic.swd.packets.Disconnect;
import de.nomagic.swd.packets.FaultPacket;
import de.nomagic.swd.packets.IdleBits;
import de.nomagic.swd.packets.JtagToDormant;
import de.nomagic.swd.packets.JtagToSwd;
import de.nomagic.swd.packets.LineReset;
import de.nomagic.swd.packets.SwdPacket;
import de.nomagic.swd.packets.SwdToDormant;
import de.nomagic.swd.packets.WaitPacket;

class BitStreamCrackerTest
{
    @Test
    void test_no_packetSource()
    {
        BitStreamCracker cut = new BitStreamCracker(null);
        assertNotNull(cut);
    }

    //  - packet read or write -> 8bit +turn + 3 bit (+ turn) + 33 bit
    //  - dormant to SWD = at least 8x1 + (128 bit) 0100 1001 1100 1111 1001 0000 0100 0110 1010 1001 1011 0100 1010 0001 0110 0001 1001 0111 1111 0101 1011 1011 1100 0111 0100 0101 0111 0000 0011 1101 1001 1000 0000 0101 1000 + at least 50x1


    //  - packet -> wait or Fail -> 8bit + turn + 3 bit
    @Test
    void test_Packet_wait()
    {
        PacketSequence ps = new PacketSequence();
        BitStreamCracker cut = new BitStreamCracker(ps);

        cut.add_one();  // start
        cut.add_zero(); // 0 = DP; 1 = AP
        cut.add_one();  // 0 = write, 1 = read
        cut.add_one();  // A2
        cut.add_one();  // A3
        cut.add_one();  // parity even
        cut.add_zero(); // Stop
        cut.add_one();  // park

        cut.add_one();  // turn
        cut.add_zero(); // ACK
        cut.add_one();  // ACK
        cut.add_zero(); // ACK

        assertEquals(0, cut.detectPackages());
        SwdPacket res = ps.getNextPacket();
        assertTrue(res instanceof WaitPacket);
        res = ps.getNextPacket();
        assertNull(res);
    }

    //  - packet -> wait or Fail -> 8bit + turn + 3 bit
    @Test
    void test_Packet_fault()
    {
        PacketSequence ps = new PacketSequence();
        BitStreamCracker cut = new BitStreamCracker(ps);

        cut.add_one();  // start
        cut.add_zero(); // 0 = DP; 1 = AP
        cut.add_one();  // 0 = write, 1 = read
        cut.add_one();  // A2
        cut.add_one();  // A3
        cut.add_one();  // parity even
        cut.add_zero(); // Stop
        cut.add_one();  // park

        cut.add_one();  // turn
        cut.add_zero(); // ACK
        cut.add_zero(); // ACK
        cut.add_one();  // ACK

        assertEquals(0, cut.detectPackages());
        SwdPacket res = ps.getNextPacket();
        assertTrue(res instanceof FaultPacket);
        res = ps.getNextPacket();
        assertNull(res);
    }

    //  - idle bits single 0 bits
    @Test
    void test_idleBits()
    {
        PacketSequence ps = new PacketSequence();
        BitStreamCracker cut = new BitStreamCracker(ps);

        cut.add_zero();
        cut.add_zero();
        cut.add_zero();
        cut.add_zero();
        cut.add_one();
        cut.add_one();
        cut.add_one();
        cut.add_one();
        cut.add_one();
        cut.add_one();
        cut.add_one();
        cut.add_one();

        assertEquals(0, cut.detectPackages());
        SwdPacket res = ps.getNextPacket();
        assertTrue(res instanceof IdleBits);
        res = ps.getNextPacket();
        assertNull(res);
    }


    //  - SWD to dormant = at least 50 bits 1 + (16bit) 0011 1101 1100 0111
    @Test
    void test_SWD_to_Dormant()
    {
        PacketSequence ps = new PacketSequence();
        BitStreamCracker cut = new BitStreamCracker(ps);

        for(int i = 0; i < 50; i++)
        {
            cut.add_one();
        }

        cut.add_zero();
        cut.add_zero();
        cut.add_one();
        cut.add_one();

        cut.add_one();
        cut.add_one();
        cut.add_zero();
        cut.add_one();

        cut.add_one();
        cut.add_one();
        cut.add_zero();
        cut.add_zero();

        cut.add_zero();
        cut.add_one();
        cut.add_one();
        cut.add_one();

        assertEquals(0, cut.detectPackages());
        SwdPacket res = ps.getNextPacket();
        assertTrue(res instanceof SwdToDormant);
        res = ps.getNextPacket();
        assertNull(res);
    }

    //  - JTAG to dormant = at least 5x1 + (31bit) 010 1110 1110 1110 1110 1110 1110 0110
    @Test
    void test_JTAG_to_dormant()
    {
        PacketSequence ps = new PacketSequence();
        BitStreamCracker cut = new BitStreamCracker(ps);

        cut.add_one();
        cut.add_one();
        cut.add_one();
        cut.add_one();
        cut.add_one();

        cut.add_zero();
        cut.add_one();
        cut.add_zero();

        cut.add_one();
        cut.add_one();
        cut.add_one();
        cut.add_zero();

        cut.add_one();
        cut.add_one();
        cut.add_one();
        cut.add_zero();

        cut.add_one();
        cut.add_one();
        cut.add_one();
        cut.add_zero();

        cut.add_one();
        cut.add_one();
        cut.add_one();
        cut.add_zero();

        cut.add_one();
        cut.add_one();
        cut.add_one();
        cut.add_zero();

        cut.add_one();
        cut.add_one();
        cut.add_one();
        cut.add_zero();

        cut.add_zero();
        cut.add_one();
        cut.add_one();
        cut.add_zero();

        assertEquals(0, cut.detectPackages());
        SwdPacket res = ps.getNextPacket();
        assertTrue(res instanceof JtagToDormant);
        res = ps.getNextPacket();
        assertNull(res);
    }

    //  - disconnect at least 8x0
    @Test
    void test_disconnect()
    {
        PacketSequence ps = new PacketSequence();
        BitStreamCracker cut = new BitStreamCracker(ps);
        for(int i = 0; i < 8; i++)
        {
            cut.add_zero();
        }
        assertEquals(0, cut.detectPackages());
        SwdPacket res = ps.getNextPacket();
        assertTrue(res instanceof Disconnect);
        res = ps.getNextPacket();
        assertNull(res);
    }

    //  - JTAG to SWD = 16 bit =  (16bit) 0111 1001 1110 0111
    @Test
    void test_JTAG_to_SWD()
    {
        PacketSequence ps = new PacketSequence();
        BitStreamCracker cut = new BitStreamCracker(ps);

        cut.add_zero();
        cut.add_one();
        cut.add_one();
        cut.add_one();

        cut.add_one();
        cut.add_zero();
        cut.add_zero();
        cut.add_one();

        cut.add_one();
        cut.add_one();
        cut.add_one();
        cut.add_zero();

        cut.add_zero();
        cut.add_one();
        cut.add_one();
        cut.add_one();

        assertEquals(0, cut.detectPackages());
        SwdPacket res = ps.getNextPacket();
        assertTrue(res instanceof JtagToSwd);
        res = ps.getNextPacket();
        assertNull(res);
    }

    //  - Line Reset at least 50x1 and 2x0
    @Test
    void test_lineReset()
    {
        PacketSequence ps = new PacketSequence();
        BitStreamCracker cut = new BitStreamCracker(ps);
        for(int i = 0; i < 50; i++)
        {
            cut.add_one();
        }
        cut.add_zero();
        cut.add_zero();
        assertEquals(0, cut.detectPackages());
        SwdPacket res = ps.getNextPacket();
        assertTrue(res instanceof LineReset);
        res = ps.getNextPacket();
        assertNull(res);
    }

    @Test
    void test_emptyFlush()
    {
        PacketSequence ps = new PacketSequence();
        BitStreamCracker cut = new BitStreamCracker(ps);
        assertEquals(9, cut.detectPackages());
        cut.flush();
        SwdPacket res = ps.getNextPacket();
        assertNull(res);
    }

}
