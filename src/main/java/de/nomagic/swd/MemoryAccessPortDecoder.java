package de.nomagic.swd;

import java.io.PrintStream;

import de.nomagic.swd.packets.OkPacket;

public class MemoryAccessPortDecoder
{
    private PrintStream out;
    private long SELECT;
    private long TAR = 0;
    private long CSW = 0;
    private long ActiveAddress = 0;
    private boolean readActive = false;
    private boolean writeActive = false;

    public MemoryAccessPortDecoder()
    {
    }

    public void reportTo(PrintStream out)
    {
        this.out = out;
    }

    public void setSELECT(long SELECT)
    {
        this.SELECT = SELECT;
    }

    public void add(OkPacket okp)
    {
        AP_Register reg = okp.getAPRegType();
        boolean read = okp.getIsRead();
        if(true == read)
        {
            switch(reg)
            {
            case DRW:if(true == readActive)
                     {
                         out.println("wrote " + String.format("0x%08X", okp.getData()) + " to " + String.format("0x%08X", ActiveAddress));
                         ActiveAddress = TAR;
                     }
                     else
                     {
                         readActive = true;
                         ActiveAddress = TAR;
                     }
                     break;
            case BD0:// TODO
            case BD1:// TODO
            case BD2:// TODO
            case BD3:// TODO
            default: break;
            /*
            case CSW:break; // ignore for now
            case TAR:break; // ignore for now
            case MBT: break; // ignore for now
            case T0TR: break; // ignore for now
            case CFG1: break; // ignore for now
            case BASE1: break; // ignore for now
            case CFG: break; // ignore for now
            case BASE: break; // ignore for now
            case IDR: break; // ignore for now
            */
            }
        }
        else
        {
            switch(reg)
            {
            case CSW: CSW = okp.getData(); break;
            case TAR: TAR = okp.getData(); break;
            case DRW: if(true == writeActive)
                      {
                          out.println("Read " + String.format("0x%08X", okp.getData()) + " from " + String.format("0x%08X", ActiveAddress));
                          ActiveAddress = TAR;
                      }
                      else
                      {
                          writeActive = true;
                          ActiveAddress = TAR;
                      }
                      break;
            case BD0:// TODO
            case BD1:// TODO
            case BD2:// TODO
            case BD3:// TODO
            default: break;
            /*
            case MBT: break; // ignore for now
            case T0TR: break; // ignore for now
            case CFG1: break; // ignore for now
            case BASE1: break; // ignore for now
            case CFG: break; // ignore for now
            case BASE: break; // ignore for now
            case IDR: break; // ignore for now
            */
            }
        }

    }

    public void addResend(OkPacket okp)
    {
        // TODO
    }

    public void addRdBuff(OkPacket okp)
    {
        if(true == readActive)
        {
            out.println("read " + String.format("0x%08X", okp.getData()) + " from " + String.format("0x%08X", ActiveAddress));
            readActive = false;
        }
        if(true == writeActive)
        {
            out.println("wrote " + String.format("0x%08X", okp.getData()) + " to " + String.format("0x%08X", ActiveAddress));
            writeActive = false;
        }
        ActiveAddress = 0;
    }

}
