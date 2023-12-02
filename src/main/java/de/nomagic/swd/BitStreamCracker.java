package de.nomagic.swd;

import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.nomagic.PacketSequence;
import de.nomagic.swd.packets.Disconnect;
import de.nomagic.swd.packets.DormantToSwd;
import de.nomagic.swd.packets.FaultPacket;
import de.nomagic.swd.packets.IdleBits;
import de.nomagic.swd.packets.InvalidBits;
import de.nomagic.swd.packets.JtagToDormant;
import de.nomagic.swd.packets.JtagToSwd;
import de.nomagic.swd.packets.LineReset;
import de.nomagic.swd.packets.OkPacket;
import de.nomagic.swd.packets.SwdPacket;
import de.nomagic.swd.packets.SwdToDormant;
import de.nomagic.swd.packets.WaitPacket;

public class BitStreamCracker
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private final PacketSequence resStream;
    private Vector<Integer> bits = new Vector<Integer>();

    private int numBitsMissing;

    public BitStreamCracker(PacketSequence packets)
    {
        if(null == packets)
        {
            log.error("No PacketSequence provided!");
        }
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
        if(true == bits.isEmpty())
        {
            return 9;
        }
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
        while((false == bits.isEmpty()) && (0 == res))
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

        if(8 > bits.size())
        {
            // not enough bits to decide
            return 8 - bits.size();
        }
        // count 0
        int num = 1;
        while(num < bits.size())
        {
            if(0 == bits.get(num))
            {
                num++;
            }
            else
            {
                break;
            }
        }
        if(1 == num)
        {
            if(16 > bits.size())
            {
                // not enough bits to decide
                return 16 - bits.size();
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
                SwdPacket res = new IdleBits(1);
                resStream.add(res);
                return 0;
            }
        }
        else if(num > 7)
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
            SwdPacket res = new IdleBits(num);
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
            return 1;
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
            return handlePacket();
        }
        else
        {
            numBitsMissing = 500;
            // not a packet therefore:
            //  - JTAG to dormant = at least 5x1 + (31bit) 010 1110 1110 1110 1110 1110 1110 0110
            //  - Line Reset at least 50x1 and 2x0
            //  - SWD to dormant = at least 50 bits 1 + (16bit) 0011 1101 1100 0111
            //  - dormant to SWD = at least 8x1 + (128 bit) 0100 1001 1100 1111 1001 0000 0100 0110 1010 1001 1011 0100 1010 0001 0110 0001 1001 0111 1111 0101 1011 1011 1100 0111 0100 0101 0111 0000 0011 1101 1001 1000 0000 0101 1000 + at least 50x1
            if(num < 8)
            {
                boolean res;
                res = checkIfJtagToDormant(num);
                if(false == res)
                {
                    if(500 == numBitsMissing)
                    {
                        // did not match - will not match -> error
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
                        // might be a match - we need more bits
                        return numBitsMissing;
                    }
                }
                else
                {
                    // we found something
                    return 0;
                }
            }
            else if(num < 50)
            {
                boolean res;
                res = checkIfJtagToDormant(num);
                if(false == res)
                {
                    res = checkIfDormantToSwd(num);
                }
                if(false == res)
                {
                    if(500 == numBitsMissing)
                    {
                        // did not match - will not match -> error
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
                        // might be a match - we need more bits
                        return numBitsMissing;
                    }
                }
                else
                {
                    // we found something
                    return 0;
                }
            }
            else
            {
                boolean res;
                res = checkIfJtagToDormant(num);
                if(false == res)
                {
                    res = checkIfDormantToSwd(num);
                }
                if(false == res)
                {
                    res = checkIfSwdToDormant(num);
                }
                if(false == res)
                {
                    res = checkIfLineReset(num);
                }
                if(false == res)
                {
                    if(500 == numBitsMissing)
                    {
                        // did not match - will not match -> error
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
                        // might be a match - we need more bits
                        return numBitsMissing;
                    }
                }
                else
                {
                    // we found something
                    return 0;
                }
            }
        }
    }

    private boolean checkIfSwdToDormant(int num)
    {
        // SWD to dormant = at least 50 bits 1 + (16bit) 0011 1101 1100 0111
        if((num + 16) > bits.size())
        {
            int bitsMissing = (num + 16) - bits.size();
            if(bitsMissing < numBitsMissing)
            {
                numBitsMissing = bitsMissing;
            }
            return false;
        }
        if(
                (0 == bits.get(num + 0))
             && (0 == bits.get(num + 1))
             && (1 == bits.get(num + 2))
             && (1 == bits.get(num + 3))

             && (1 == bits.get(num + 4))
             && (1 == bits.get(num + 5))
             && (0 == bits.get(num + 6))
             && (1 == bits.get(num + 7))

             && (1 == bits.get(num + 8))
             && (1 == bits.get(num + 9))
             && (0 == bits.get(num + 10))
             && (0 == bits.get(num + 11))

             && (0 == bits.get(num + 12))
             && (1 == bits.get(num + 13))
             && (1 == bits.get(num + 14))
             && (1 == bits.get(num + 15))
             )
         {
             // SWD to dormant
             for(int i = 0; i < (16 + num); i++)
             {
                 bits.remove(0); // bits then shift forward
             }
             SwdPacket res = new SwdToDormant();
             resStream.add(res);
             return true;
         }
        else
        {
            return false;
        }
    }

    private boolean checkIfDormantToSwd(int num)
    {
        // dormant to SWD = at least 8x1 + (140 bit) 0100 1001  1100 1111  1001 0000  0100 0110
        //                                           1010 1001  1011 0100  1010 0001  0110 0001
        //                                           1001 0111  1111 0101  1011 1011  1100 0111
        //                                           0100 0101  0111 0000  0011 1101  1001 1000
        //                                           0000 0101  1000 + at least 50x1
        if((num + 190) > bits.size())
        {
            int bitsMissing = (num + 190) - bits.size();
            if(bitsMissing < numBitsMissing)
            {
                numBitsMissing = bitsMissing;
            }
            return false;
        }
        if(
             (0 == bits.get(num +  0)) && (1 == bits.get(num +  1)) && (0 == bits.get(num +  2)) && (0 == bits.get(num +  3))
          && (1 == bits.get(num +  4)) && (0 == bits.get(num +  5)) && (0 == bits.get(num +  6)) && (1 == bits.get(num +  7))
          && (1 == bits.get(num +  8)) && (1 == bits.get(num +  9)) && (0 == bits.get(num + 10)) && (0 == bits.get(num + 11))
          && (1 == bits.get(num + 12)) && (1 == bits.get(num + 13)) && (1 == bits.get(num + 14)) && (1 == bits.get(num + 15))
          && (1 == bits.get(num + 16)) && (0 == bits.get(num + 17)) && (0 == bits.get(num + 18)) && (1 == bits.get(num + 19))
          && (0 == bits.get(num + 20)) && (0 == bits.get(num + 21)) && (0 == bits.get(num + 22)) && (0 == bits.get(num + 23))
          && (0 == bits.get(num + 24)) && (1 == bits.get(num + 25)) && (0 == bits.get(num + 26)) && (0 == bits.get(num + 27))
          && (0 == bits.get(num + 28)) && (1 == bits.get(num + 29)) && (1 == bits.get(num + 30)) && (0 == bits.get(num + 31))
          && (1 == bits.get(num + 32)) && (0 == bits.get(num + 33)) && (1 == bits.get(num + 34)) && (0 == bits.get(num + 35))
          && (1 == bits.get(num + 36)) && (0 == bits.get(num + 37)) && (0 == bits.get(num + 38)) && (1 == bits.get(num + 39))
          && (1 == bits.get(num + 40)) && (0 == bits.get(num + 41)) && (1 == bits.get(num + 42)) && (1 == bits.get(num + 43))
          && (0 == bits.get(num + 44)) && (1 == bits.get(num + 45)) && (0 == bits.get(num + 46)) && (0 == bits.get(num + 47))
          && (1 == bits.get(num + 48)) && (0 == bits.get(num + 49)) && (1 == bits.get(num + 50)) && (0 == bits.get(num + 51))
          && (0 == bits.get(num + 52)) && (0 == bits.get(num + 53)) && (0 == bits.get(num + 54)) && (1 == bits.get(num + 55))
          && (0 == bits.get(num + 56)) && (1 == bits.get(num + 57)) && (1 == bits.get(num + 58)) && (0 == bits.get(num + 59))
          && (0 == bits.get(num + 60)) && (0 == bits.get(num + 61)) && (0 == bits.get(num + 62)) && (1 == bits.get(num + 63))
          && (1 == bits.get(num + 64)) && (0 == bits.get(num + 65)) && (0 == bits.get(num + 66)) && (1 == bits.get(num + 67))
          && (0 == bits.get(num + 68)) && (1 == bits.get(num + 69)) && (1 == bits.get(num + 70)) && (1 == bits.get(num + 71))
          && (1 == bits.get(num + 72)) && (1 == bits.get(num + 73)) && (1 == bits.get(num + 74)) && (1 == bits.get(num + 75))
          && (0 == bits.get(num + 76)) && (1 == bits.get(num + 77)) && (0 == bits.get(num + 78)) && (1 == bits.get(num + 79))
          && (1 == bits.get(num + 80)) && (0 == bits.get(num + 81)) && (1 == bits.get(num + 82)) && (1 == bits.get(num + 83))
          && (1 == bits.get(num + 84)) && (0 == bits.get(num + 85)) && (1 == bits.get(num + 86)) && (1 == bits.get(num + 87))
          && (1 == bits.get(num + 88)) && (1 == bits.get(num + 89)) && (0 == bits.get(num + 90)) && (0 == bits.get(num + 91))
          && (0 == bits.get(num + 92)) && (1 == bits.get(num + 93)) && (1 == bits.get(num + 94)) && (1 == bits.get(num + 95))
          && (0 == bits.get(num + 96)) && (1 == bits.get(num + 97)) && (0 == bits.get(num + 98)) && (0 == bits.get(num + 99))
          && (0 == bits.get(num +100)) && (1 == bits.get(num +101)) && (0 == bits.get(num +102)) && (1 == bits.get(num +103))
          && (0 == bits.get(num +104)) && (1 == bits.get(num +105)) && (1 == bits.get(num +106)) && (1 == bits.get(num +107))
          && (0 == bits.get(num +108)) && (0 == bits.get(num +109)) && (0 == bits.get(num +110)) && (0 == bits.get(num +111))
          && (0 == bits.get(num +112)) && (0 == bits.get(num +113)) && (1 == bits.get(num +114)) && (1 == bits.get(num +115))
          && (1 == bits.get(num +116)) && (1 == bits.get(num +117)) && (0 == bits.get(num +118)) && (1 == bits.get(num +119))
          && (1 == bits.get(num +120)) && (0 == bits.get(num +121)) && (0 == bits.get(num +122)) && (1 == bits.get(num +123))
          && (1 == bits.get(num +124)) && (0 == bits.get(num +125)) && (0 == bits.get(num +126)) && (0 == bits.get(num +127))
          && (0 == bits.get(num +128)) && (0 == bits.get(num +129)) && (0 == bits.get(num +130)) && (0 == bits.get(num +131))
          && (0 == bits.get(num +132)) && (1 == bits.get(num +133)) && (0 == bits.get(num +134)) && (1 == bits.get(num +135))
          && (1 == bits.get(num +136)) && (0 == bits.get(num +137)) && (0 == bits.get(num +138)) && (0 == bits.get(num +139))
             )
        {
            int nextNum = num + 140;
            while(nextNum < bits.size())
            {
                if(1 == bits.get(nextNum))
                {
                    nextNum++;
                }
                else
                {
                    break;
                }
            }
            if(nextNum == bits.size())
            {
                // just a bunch of 1's. This can go on for ever. We need an 0 to say anything.
                numBitsMissing = 1;  // one 0 would be enough
                return false;
            }
            if((nextNum -num -140) > 49)
            {
                // Dormant to SWD
                for(int i = 0; i < nextNum; i++)
                {
                    bits.remove(0); // bits then shift forward
                }
                SwdPacket res = new DormantToSwd();
                resStream.add(res);
                return true;
            }
            else
            {
                // less than 50 bits
                return false;
            }
        }
        else
        {
            // wrong bit sequence
            return false;
        }
    }

    private boolean checkIfLineReset(int num)
    {
        //  - Line Reset at least 50x1 and 2x0
        if((num + 2) > bits.size())
        {
            int bitsMissing = (num + 2) - bits.size();
            if(bitsMissing < numBitsMissing)
            {
                numBitsMissing = bitsMissing;
            }
            return false;
        }
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
             SwdPacket res = new LineReset();
             resStream.add(res);
             return true;
         }
        else
        {
            return false;
        }
    }

    private boolean checkIfJtagToDormant(int num)
    {
        //  - JTAG to dormant = at least 5x1 + (31bit) 010 1110 1110 1110 1110 1110 1110 0110
        if((num + 31) > bits.size())
        {
            int bitsMissing = (num + 31) - bits.size();
            if(bitsMissing < numBitsMissing)
            {
                numBitsMissing = bitsMissing;
            }
            return false;
        }
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
            return true;
        }
        else
        {
            return false;
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
            isDP = true;
        }
        else
        {
            isDP = false;
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
            if(12 + 34 > bits.size())
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
                        // little endian
                        if(1 == bits.get(12 + i))
                        {
                            data = data + ((long)(1) << i);
                        }
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
                        // little endian
                        if(1 == bits.get(13 + i))
                        {
                            data = data + ((long)(1) << i);
                        }
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
        else if(  (1 == bits.get(9))
               && (1 == bits.get(10))
               && (1 == bits.get(11))
               )
        {
            if(12 + 34 > bits.size())
            {
                // not enough bits to decode packet
                return (12 + 34) - bits.size();
            }
            // ACK: Target idle
            // this is OK for the TARGETSEL packet
            if(  (true == isDP)
              && (false == isRead)
              && (3 == a2a3)
              && (parity == 0) )
            {
                // TARGETSEL command
                OkPacket res = new OkPacket();
                res.setisDp(isDP);
                res.setisRead(isRead);
                res.setA2A3(a2a3);
                res.setRequestParity(parity);
                // write
                long data = 0;
                for(int i = 0; i < 32; i++)
                {
                    data = (data * 2) + bits.get(13 +i);
                }
                res.setData(data);
                res.setDataParity(bits.get(13 + 32));
                for(int i = 0; i < (12 + 34); i++)
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
        return 0;
    }

}
