package de.nomagic.spi;

import java.util.Vector;

public class SpiTransfer
{
    private final double startTime;
    private final Vector<Integer> MosiBits = new Vector<Integer>(); // IO0
    private final Vector<Integer> MisoBits = new Vector<Integer>(); // IO1
    private final Vector<Integer> io2Bits = new Vector<Integer>();
    private final Vector<Integer> io3Bits = new Vector<Integer>();
    private final boolean bitOrderIsMsbFirst = true;

    public SpiTransfer(double startTime)
    {
        this.startTime = startTime;
    }

    @Override
    public String toString()
    {
        int numBits = MisoBits.size();
        String misoString = bits2String(MisoBits);
        String mosiString = bits2String(MosiBits);

        String res = "Spi [startTime=" + startTime  + "] " + numBits + " bits " + numBits/8 + " bytes\r\n"
                + "MOSI : " + mosiString + "\r\n"
                + "MISO : " + misoString;
        if(io2Bits.size() > 0)
        {
            String io2String = bits2String(io2Bits);
            res = res + "\r\n" + "IO2 : " + io2String;
        }
        if(io3Bits.size() > 0)
        {
            String io2String = bits2String(io3Bits);
            res = res + "\r\n" + "IO3 : " + io2String;
        }
        return res;
    }

    public void addMisoBit(int val)
    {
        MisoBits.add(val);
    }

    public void addMosiBit(int val)
    {
        MosiBits.add(val);
    }

    public void addIo2Bit(int val)
    {
        io2Bits.add(val);
    }

    public void addIo3Bit(int val)
    {
        io3Bits.add(val);
    }

    private String bits2String(Vector<Integer> bits)
    {
        int pos = 0;
        int[] byteBits = new int[8];
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < bits.size(); i++)
        {
            byteBits[pos] = bits.get(i);
            pos++;
            if(8 == pos)
            {
                if(true == bitOrderIsMsbFirst)
                {
                    char hex = bits2char(byteBits[3], byteBits[2], byteBits[1], byteBits[0]);
                    sb.append(hex);
                    hex = bits2char(byteBits[7], byteBits[6], byteBits[5], byteBits[4]);
                    sb.append(hex);
                }
                else
                {
                    char hex = bits2char(byteBits[4], byteBits[5], byteBits[6], byteBits[7]);
                    sb.append(hex);
                    hex = bits2char(byteBits[0], byteBits[1], byteBits[2], byteBits[3]);
                    sb.append(hex);
                }
                sb.append(" ");
                pos = 0;
            }
        }
        if(0 == pos)
        {
            // exactly a multiple of 8 -> we are done here
        }
        else if(pos < 5)
        {
            for(int i = pos; i < 5; i++)
            {
                byteBits[i] = 0;
            }
            if(true == bitOrderIsMsbFirst)
            {
                char hex = bits2char(byteBits[3], byteBits[2], byteBits[1], byteBits[0]);
                sb.append(hex);
            }
            else
            {
                char hex = bits2char(byteBits[0], byteBits[1], byteBits[2], byteBits[3]);
                sb.append(hex);
            }
            sb.append(" ");
        }
        else
        {
            for(int i = pos; i < 8; i++)
            {
                byteBits[i] = 0;
            }
            if(true == bitOrderIsMsbFirst)
            {
                char hex = bits2char(byteBits[3], byteBits[2], byteBits[1], byteBits[0]);
                sb.append(hex);
                hex = bits2char(byteBits[7], byteBits[6], byteBits[5], byteBits[4]);
                sb.append(hex);
            }
            else
            {
                char hex = bits2char(byteBits[4], byteBits[5], byteBits[6], byteBits[7]);
                sb.append(hex);
                hex = bits2char(byteBits[0], byteBits[1], byteBits[2], byteBits[3]);
                sb.append(hex);
            }
            sb.append(" ");
        }
        return sb.toString();
    }

    private char bits2char(int lowest, int second, int third, int highest)
    {
        int val = 0;
        if(1 == lowest)
        {
            val = val + 1;
        }
        if(1 == second)
        {
            val = val + 2;
        }
        if(1 == third)
        {
            val = val + 4;
        }
        if(1 == highest)
        {
            val = val + 8;
        }
        switch(val)
        {
        case  0: return '0';
        case  1: return '1';
        case  2: return '2';
        case  3: return '3';
        case  4: return '4';
        case  5: return '5';
        case  6: return '6';
        case  7: return '7';
        case  8: return '8';
        case  9: return '9';
        case 10: return 'a';
        case 11: return 'b';
        case 12: return 'c';
        case 13: return 'd';
        case 14: return 'e';
        case 15: return 'f';
        default: return 'x';
        }
    }

}
