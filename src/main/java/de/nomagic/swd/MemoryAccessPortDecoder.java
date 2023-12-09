package de.nomagic.swd;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import de.nomagic.logic.ValueDecoder;
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
    private HashMap<Long, Long> memoryReadMap = new HashMap<Long, Long>();
    private HashMap<Long, Long> memoryWriteMap = new HashMap<Long, Long>();
    private final ValueDecoder valDec;

    public MemoryAccessPortDecoder(ValueDecoder valDec)
    {
        this.valDec = valDec;
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
                         reportRead(okp.getNumber(), ActiveAddress, okp.getData());
                         ActiveAddress = TAR;
                     }
                     else
                     {
                         readActive = true;
                         ActiveAddress = TAR;
                     }
                     break;

            case BD0:if(true == readActive)
                     {
                         reportRead(okp.getNumber(), ActiveAddress, okp.getData());
                         ActiveAddress = TAR;
                     }
                     else
                     {
                         readActive = true;
                         ActiveAddress = TAR;
                     }
                     break;

            case BD1:if(true == readActive)
                     {
                         reportRead(okp.getNumber(), ActiveAddress, okp.getData());
                         ActiveAddress = TAR + 4;
                     }
                     else
                     {
                         readActive = true;
                         ActiveAddress = TAR + 4;
                     }
                     break;

            case BD2:if(true == readActive)
                     {
                         reportRead(okp.getNumber(), ActiveAddress, okp.getData());
                         ActiveAddress = TAR + 8;
                     }
                     else
                     {
                         readActive = true;
                         ActiveAddress = TAR + 8;
                     }
                     break;

            case BD3:if(true == readActive)
                     {
                         reportRead(okp.getNumber(), ActiveAddress, okp.getData());
                         ActiveAddress = TAR + 12;
                     }
                     else
                     {
                         readActive = true;
                         ActiveAddress = TAR + 12;
                     }
                     break;

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
                          reportWrite(okp.getNumber(), ActiveAddress, okp.getData());
                          ActiveAddress = TAR;
                      }
                      else
                      {
                          writeActive = true;
                          ActiveAddress = TAR;
                      }
                      break;
            case BD0: if(true == writeActive)
                      {
                          reportWrite(okp.getNumber(), ActiveAddress, okp.getData());
                          ActiveAddress = TAR;
                      }
                      else
                      {
                          writeActive = true;
                          ActiveAddress = TAR;
                      }
                      break;
            case BD1: if(true == writeActive)
                      {
                          reportWrite(okp.getNumber(), ActiveAddress, okp.getData());
                          ActiveAddress = TAR;
                      }
                      else
                      {
                          writeActive = true;
                          ActiveAddress = TAR;
                      }
                      break;
            case BD2: if(true == writeActive)
                      {
                          reportWrite(okp.getNumber(), ActiveAddress, okp.getData());
                          ActiveAddress = TAR;
                      }
                      else
                      {
                          writeActive = true;
                          ActiveAddress = TAR;
                      }
                      break;
            case BD3: if(true == writeActive)
                      {
                          reportWrite(okp.getNumber(), ActiveAddress, okp.getData());
                          ActiveAddress = TAR;
                      }
                      else
                      {
                          writeActive = true;
                          ActiveAddress = TAR;
                      }
                      break;

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
        out.println("not yet implemented ! ");
    }

    public void addRdBuff(OkPacket okp)
    {
        if(true == readActive)
        {
            reportRead(okp.getNumber(), ActiveAddress, okp.getData());
            readActive = false;
        }
        if(true == writeActive)
        {
            reportWrite(okp.getNumber(), ActiveAddress, okp.getData());
            writeActive = false;
        }
        ActiveAddress = 0;
    }

    private void reportRead(long number, long address, long data)
    {
        String Address = String.format("0x%08X", address);
        String desc = valDec.getShortNameFor(address);
        if( null != desc)
        {
            Address = Address + " = " + desc;
        }
        desc = valDec.getLongNameFor(address);
        if( null != desc)
        {
            Address = Address + " (" + desc + ")";
        }
        out.println(number + ": read " + String.format("0x%08X", data) + " from " + Address);
        memoryReadMap.put(address, data);
    }

    private void reportWrite(long number, long address, long data)
    {
        String Address = String.format("0x%08X", address);
        String desc = valDec.getShortNameFor(address);
        if( null != desc)
        {
            Address = Address + " = " + desc;
        }
        desc = valDec.getLongNameFor(address);
        if( null != desc)
        {
            Address = Address + " (" + desc + ")";
        }
        out.println(number + ": wrote " + String.format("0x%08X", data) + " to " + Address);
        memoryWriteMap.put(address, data);
    }

    public void printMemoryMap(PrintStream pOut)
    {
        out.println("read:");
        printMemoryReadMap(pOut);
        out.println("wrote:");
        printMemoryWriteMap(pOut);
    }

    private void printMemoryReadMap(PrintStream pOut)
    {
        SortedSet<Long> keys = new TreeSet<Long>(memoryReadMap.keySet());
        Iterator<Long> it = keys.iterator();
        while(it.hasNext())
        {
            Long addr = it.next();
            Long val = memoryReadMap.get(addr);
            out.println(String.format("0x%08X", addr) + String.format(" = 0x%08X", val));
        }
    }

    private void printMemoryWriteMap(PrintStream pOut)
    {
        SortedSet<Long> keys = new TreeSet<Long>(memoryWriteMap.keySet());
        Iterator<Long> it = keys.iterator();
        while(it.hasNext())
        {
            Long addr = it.next();
            Long val = memoryWriteMap.get(addr);
            out.println(String.format("0x%08X", addr) + String.format(" = 0x%08X", val));
        }
    }
}
