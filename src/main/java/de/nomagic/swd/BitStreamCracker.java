package de.nomagic.swd;

import java.util.Vector;

import de.nomagic.PacketSequence;
import de.nomagic.swd.packets.Disconnect;
import de.nomagic.swd.packets.FaultPacket;
import de.nomagic.swd.packets.IdleBits;
import de.nomagic.swd.packets.InvalidBits;
import de.nomagic.swd.packets.JtagToDormant;
import de.nomagic.swd.packets.JtagToSwd;
import de.nomagic.swd.packets.OkPacket;
import de.nomagic.swd.packets.SwdPacket;
import de.nomagic.swd.packets.WaitPacket;

public class BitStreamCracker
{
    private Vector<Integer> bits = new Vector<Integer>();
    private final PacketSequence resStream;

    public BitStreamCracker(PacketSequence packets)
    {
        resStream = packets;
    }

    public void add_one()
    {
        bits.add(1);
    }

    public void add_zero()
    {
        bits.add(0);
    }

    /**
     *
     * @return the number of bits missing to detect a packet (0 if a packet was detected, then call again to check for next packet.
     */
    public int detectPackages()
    {
        if(0 == bits.get(0))
        {
            // pattern start with a 0
            return detectZeroPackages();
        }
        else
        {
            // pattern starts with a 1
            return detectOnePackages();
        }
    }

    public void flush()
    {
        // try to report everything that is still in the queue
        int res = 0;
        while(0 == res)
        {
            if(0 == bits.get(0))
            {
                // pattern start with a 0
                res = detectZeroPackages();
            }
            else
            {
                // pattern starts with a 1
                res = detectOnePackages();
            }
        }
        // there will be no new bits be coming, therefore report what we have
        if(0 < bits.size())
        {
            InvalidBits packetPart = new InvalidBits();
            for(int i = 0; i < bits.size(); i++)
            {
                packetPart.add(bits.get(i));
            }
            resStream.add(packetPart);
        }
        // else done
    }


    private int detectZeroPackages()
    {
        // pattern start with a 0
        // could be:
        //  - disconnect at least 8x0
        //  - idle bits single 0 bits
        //  - JTAG to SWD = 16 bit =  (16bit) 0111 1001 1110 0111

        if(9 > bits.size())
        {
            // not enough bits to decide
            return 9 - bits.size();
        }
        // count 0
        int num = 1;
        while(0 == bits.get(num))
        {
            num++;
        }
        if(num == bits.size())
        {
            // only 0 - no 1
            return 1; // we need at least one 1 to be sure that the sequence has ended
        }
        if(1 == num)
        {
            if(17 > bits.size())
            {
                // not enough bits to decide
                return 17 - bits.size();
            }
            if( // bits.get(0) is 0 we already know that
                   (1 == bits.get(1))
                && (1 == bits.get(2))
                && (1 == bits.get(3))

                && (1 == bits.get(4))
                && (0 == bits.get(5))
                && (0 == bits.get(6))
                && (1 == bits.get(7))

                && (1 == bits.get(8))
                && (1 == bits.get(9))
                && (1 == bits.get(10))
                && (0 == bits.get(11))

                && (0 == bits.get(12))
                && (1 == bits.get(13))
                && (1 == bits.get(14))
                && (1 == bits.get(15))
                )
            {
                // JTAG to SWD
                for(int i = 0; i < 16; i++)
                {
                    bits.remove(0); // bits then shift forward
                }
                SwdPacket res = new JtagToSwd();
                resStream.add(res);
                return 0;
            }
            else
            {
                // idle bit
                bits.remove(0); // bits then shift forward
                SwdPacket res = new IdleBits();
                resStream.add(res);
                return 0;
            }
        }
        else if(num >= 8)
        {
            // disconnect
            for(int i = 0; i < num; i++)
            {
                bits.remove(0); // bits then shift forward
            }
            SwdPacket res = new Disconnect();
            resStream.add(res);
            return 0;
        }
        else
        {
            // num = 2..7
            // idle bits
            for(int i = 0; i < num; i++)
            {
                bits.remove(0); // bits then shift forward
            }
            SwdPacket res = new IdleBits();
            resStream.add(res);
            return 0;
        }
    }

    private int detectOnePackages()
    {
        // pattern starts with a 1
        // could be:
        //  - packet -> wait or Fail -> 8bit + turn + 3 bit
        //  - packet read or write -> 8bit +turn + 3 bit (+ turn) + 33 bit
        //  - JTAG to dormant = at least 5x1 + (31bit) 010 1110 1110 1110 1110 1110 1110 0110
        //  - Line Reset at least 50x1 and 2x0
        //  - SWD to dormant = at least 50 bits 1 + (16bit) 0011 1101 1100 0111
        //  - dormant to SWD = at least 8x1 + (128 bit) 0100 1001 1100 1111 1001 0000 0100 0110 1010 1001 1011 0100 1010 0001 0110 0001 1001 0111 1111 0101 1011 1011 1100 0111 0100 0101 0111 0000 0011 1101 1001 1000 0000 0101 1000 + at least 50x1

        if(12 > bits.size())
        {
            // not enough bits to decide
            return 12 - bits.size();
        }

        // count 1
        int num = 1;
        while(num < bits.size())
        {
            if(1 == bits.get(num))
            {
                num++;
            }
            else
            {
                break;
            }
        }
        if(num == bits.size())
        {
            // just a bunch of 1's. This can go on for ever. We need an 0 to say anything.
            return 50;
        }
        // if a packet then bit 6 is Stop and needs to be 0
        //         012345678 901
        // packet: 1xxxxx011 100 = OK
        // packet: 1xxxxx011 010 = wait
        // packet: 1xxxxx011 001 = fault
        if(    (0 == bits.get(6))  // Stop
            && (1 == bits.get(7))  // park
            && (1 == bits.get(8))  // turn
                  )
        {
            // is a packet
            handlePacket();
        }
        else
        {
            // not a packet therefore:
            //  - JTAG to dormant = at least 5x1 + (31bit) 010 1110 1110 1110 1110 1110 1110 0110
            //  - Line Reset at least 50x1 and 2x0
            //  - SWD to dormant = at least 50 bits 1 + (16bit) 0011 1101 1100 0111
            //  - dormant to SWD = at least 8x1 + (128 bit) 0100 1001 1100 1111 1001 0000 0100 0110 1010 1001 1011 0100 1010 0001 0110 0001 1001 0111 1111 0101 1011 1011 1100 0111 0100 0101 0111 0000 0011 1101 1001 1000 0000 0101 1000 + at least 50x1

            if(num < 8)
            {
                int res;
                res = checkIfJtagToDormant(num);
                if(0 > res)
                {
                    InvalidBits badBits = new InvalidBits();
                    for(int i = 0; i < num; i++)
                    {
                        bits.remove(0); // bits then shift forward
                        badBits.add(0);
                    }
                    resStream.add(badBits);
                    return 0;
                }
                else
                {
                    // we need more bits or we found something
                    return res;
                }
            }
            else if(num < 50)
            {
                int res;
                res = checkIfJtagToDormant(num);
                if(0 > res)
                {
                    res = checkIfDormantToSwd(num);
                }
                if(0 > res)
                {
                    InvalidBits badBits = new InvalidBits();
                    for(int i = 0; i < num; i++)
                    {
                        bits.remove(0); // bits then shift forward
                        badBits.add(0);
                    }
                    resStream.add(badBits);
                    return 0;
                }
                else
                {
                    // we need more bits or we found something
                    return res;
                }
            }
            else
            {
                int res;
                res = checkIfJtagToDormant(num);
                if(0 > res)
                {
                    res = checkIfDormantToSwd(num);
                }
                if(0 > res)
                {
                    res = checkIfSwdToDormant(num);
                }
                if(0 > res)
                {
                    res = checkIfLineReset(num);
                }
                if(0 > res)
                {
                    InvalidBits badBits = new InvalidBits();
                    for(int i = 0; i < num; i++)
                    {
                        bits.remove(0); // bits then shift forward
                        badBits.add(0);
                    }
                    resStream.add(badBits);
                    return 0;
                }
                else
                {
                    // we need more bits or we found something
                    return res;
                }
            }
        }
        return 12;
    }

    private int checkIfSwdToDormant(int num)
    {
        // TODO Auto-generated method stub
        return -1;
    }

    private int checkIfDormantToSwd(int num)
    {
        // TODO Auto-generated method stub
        return -1;
    }

    private int checkIfLineReset(int num)
    {
        //  - Line Reset at least 50x1 and 2x0
        if( (num > 49)
            && (0 == bits.get(num + 0))
            && (0 == bits.get(num + 1))
           )
         {
             // Line Reset
             for(int i = 0; i < (2 + num); i++)
             {
                 bits.remove(0); // bits then shift forward
             }
             SwdPacket res = new JtagToDormant();
             resStream.add(res);
             return 0;
         }
        else
        {
            return -1;
        }
    }

    private int checkIfJtagToDormant(int num)
    {
        //  - JTAG to dormant = at least 5x1 + (31bit) 010 1110 1110 1110 1110 1110 1110 0110
        if(
                (0 == bits.get(num + 0))
             && (1 == bits.get(num + 1))
             && (0 == bits.get(num + 2))

             && (1 == bits.get(num + 3))
             && (1 == bits.get(num + 4))
             && (1 == bits.get(num + 5))
             && (0 == bits.get(num + 6))

             && (1 == bits.get(num + 7))
             && (1 == bits.get(num + 8))
             && (1 == bits.get(num + 9))
             && (0 == bits.get(num + 10))

             && (1 == bits.get(num + 11))
             && (1 == bits.get(num + 12))
             && (1 == bits.get(num + 13))
             && (0 == bits.get(num + 14))

             && (1 == bits.get(num + 15))
             && (1 == bits.get(num + 16))
             && (1 == bits.get(num + 17))
             && (0 == bits.get(num + 18))

             && (1 == bits.get(num + 19))
             && (1 == bits.get(num + 20))
             && (1 == bits.get(num + 21))
             && (0 == bits.get(num + 22))

             && (1 == bits.get(num + 23))
             && (1 == bits.get(num + 24))
             && (1 == bits.get(num + 25))
             && (0 == bits.get(num + 26))

             && (0 == bits.get(num + 27))
             && (1 == bits.get(num + 28))
             && (1 == bits.get(num + 29))
             && (0 == bits.get(num + 30))
             )
         {
             // JTAG to SWD
             for(int i = 0; i < (31 + num); i++)
             {
                 bits.remove(0); // bits then shift forward
             }
             SwdPacket res = new JtagToDormant();
             resStream.add(res);
             return 0;
         }
        else
        {
            return -1;
        }
    }

    private int handlePacket()
    {
        boolean isDP;
        boolean isRead;
        int a2a3 = 0;
        int parity;
        if(0 == bits.get(1))
        {
            // DPnAP
            isDP = false;
        }
        else
        {
            isDP = true;
        }

        if(0 == bits.get(2))
        {
            isRead = false;
        }
        else
        {
            isRead = true;
        }

        a2a3 = bits.get(3) + 2* bits.get(4);

        parity = bits.get(5);

        if(  (1 == bits.get(9))
          && (0 == bits.get(10))
          && (0 == bits.get(11))
          )
        {
            // ACK : OK
            // read =         32bit data + 1 bit parity + turn
            // write = turn + 32bit data + 1 bit parity
            if(12 + 34 < bits.size())
            {
                // not enough bits to decode packet
                return (12 + 34) - bits.size();
            }
            else
            {
                OkPacket res = new OkPacket();
                res.setisDp(isDP);
                res.setisRead(isRead);
                res.setA2A3(a2a3);
                res.setRequestParity(parity);
                if(true == isRead)
                {
                    // read
                    long data = 0;
                    for(int i = 0; i < 32; i++)
                    {
                        data = (data * 2) + bits.get(12 +i);
                    }
                    res.setData(data);
                    res.setDataParity(bits.get(12 + 32));
                }
                else
                {
                    // write
                    long data = 0;
                    for(int i = 0; i < 32; i++)
                    {
                        data = (data * 2) + bits.get(13 +i);
                    }
                    res.setData(data);
                    res.setDataParity(bits.get(13 + 32));
                }
                for(int i = 0; i < (12 + 34); i++)
                {
                    bits.remove(0); // bits then shift forward
                }
                resStream.add(res);
            }
        }
        else if(  (0 == bits.get(9))
               && (1 == bits.get(10))
               && (0 == bits.get(11))
               )
        {
            // ACK : wait
            WaitPacket res = new WaitPacket();
            res.setisDp(isDP);
            res.setisRead(isRead);
            res.setA2A3(a2a3);
            res.setRequestParity(parity);
            for(int i = 0; i < 12; i++)
            {
                bits.remove(0); // bits then shift forward
            }
            resStream.add(res);
        }
        else if(  (0 == bits.get(9))
               && (0 == bits.get(10))
               && (1 == bits.get(11))
               )
        {
            // ACK : Fault
            FaultPacket res = new FaultPacket();
            res.setisDp(isDP);
            res.setisRead(isRead);
            res.setA2A3(a2a3);
            res.setRequestParity(parity);
            for(int i = 0; i < 12; i++)
            {
                bits.remove(0); // bits then shift forward
            }
            resStream.add(res);
        }
        else
        {
            // invalid ACK
            InvalidBits res = new InvalidBits();
            for(int i = 0; i < 12; i++)
            {
                res.add(bits.get(0));
                bits.remove(0); // bits then shift forward
            }
            resStream.add(res);
        }
        return 12;
    }

}
