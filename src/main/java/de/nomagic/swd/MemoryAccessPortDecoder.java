package de.nomagic.swd;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

import de.nomagic.logic.ValueDecoder;
import de.nomagic.swd.packets.OkPacket;

public class MemoryAccessPortDecoder
{
    private PrintStream out;
    private long TAR = 0;
    private long CSW = 0;
    private boolean addressAutoIncrement = false;
    private HashMap<Long, Long> memoryReadMap = new HashMap<Long, Long>();
    private HashMap<Long, Long> memoryWriteMap = new HashMap<Long, Long>();
    private final ValueDecoder valDec;
    private Vector<AP_Transfer> activeTransfers = new Vector<AP_Transfer>();

    public MemoryAccessPortDecoder(ValueDecoder valDec)
    {
        this.valDec = valDec;
    }

    public void reportTo(PrintStream out)
    {
        this.out = out;
    }

    public void add(OkPacket okp)
    {
        AP_Register reg = okp.getAPRegType();
        boolean read = okp.getIsRead();
        AP_Transfer t;
        if(true == read)
        {
            // reading
            switch(reg)
            {
            case DRW:
                if(true == activeTransfers.isEmpty())
                {
                    // first transfer -> ignore data
                }
                else
                {
                    t = activeTransfers.elementAt(0);
                    activeTransfers.remove(0);
                    t.setData(okp.getData());
                    reportTransfer(t);
                }
                t = new AP_Transfer();
                t.setToReadTransfer();
                t.setPacketNumber(okp.getNumber());
                t.setAddress(TAR);
                activeTransfers.add(t);
                if(true == addressAutoIncrement)
                {
                    TAR = TAR + 4;
                }
                break;

            case BD0:
                if(true == activeTransfers.isEmpty())
                {
                    // first transfer -> ignore data
                }
                else
                {
                    t = activeTransfers.elementAt(0);
                    activeTransfers.remove(0);
                    t.setData(okp.getData());
                    reportTransfer(t);
                }
                t = new AP_Transfer();
                t.setToReadTransfer();
                t.setPacketNumber(okp.getNumber());
                t.setAddress(TAR);
                activeTransfers.add(t);
                break;

            case BD1:
                if(true == activeTransfers.isEmpty())
                {
                    // first transfer -> ignore data
                }
                else
                {
                    t = activeTransfers.elementAt(0);
                    activeTransfers.remove(0);
                    t.setData(okp.getData());
                    reportTransfer(t);
                }
                t = new AP_Transfer();
                t.setToReadTransfer();
                t.setPacketNumber(okp.getNumber());
                t.setAddress(TAR + 4);
                activeTransfers.add(t);
                break;

            case BD2:
                if(true == activeTransfers.isEmpty())
                {
                    // first transfer -> ignore data
                }
                else
                {
                    t = activeTransfers.elementAt(0);
                    activeTransfers.remove(0);
                    t.setData(okp.getData());
                    reportTransfer(t);
                }
                t = new AP_Transfer();
                t.setToReadTransfer();
                t.setPacketNumber(okp.getNumber());
                t.setAddress(TAR + 8);
                activeTransfers.add(t);
                break;

            case BD3:
                if(true == activeTransfers.isEmpty())
                {
                    // first transfer -> ignore data
                }
                else
                {
                    t = activeTransfers.elementAt(0);
                    activeTransfers.remove(0);
                    t.setData(okp.getData());
                    reportTransfer(t);
                }
                t = new AP_Transfer();
                t.setToReadTransfer();
                t.setPacketNumber(okp.getNumber());
                t.setAddress(TAR + 12);
                activeTransfers.add(t);
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
            // writing
            switch(reg)
            {
            case CSW: CSW = okp.getData();
                      if(0 == (CSW & ~0x10))
                      {
                          addressAutoIncrement = false;
                      }
                      else
                      {
                          addressAutoIncrement = true;
                      }
                      break;

            case TAR: TAR = okp.getData(); break;

            case DRW: t = new AP_Transfer();
                      t.setToWriteTransfer();
                      t.setPacketNumber(okp.getNumber());
                      t.setData(okp.getData());
                      t.setAddress(TAR);
                      reportTransfer(t);
                      if(true == addressAutoIncrement)
                      {
                          TAR = TAR + 4;
                      }
                      break;

            case BD0: t = new AP_Transfer();
                      t.setToWriteTransfer();
                      t.setPacketNumber(okp.getNumber());
                      t.setData(okp.getData());
                      t.setAddress(TAR);
                      reportTransfer(t);
                      break;

            case BD1: t = new AP_Transfer();
                      t.setToWriteTransfer();
                      t.setPacketNumber(okp.getNumber());
                      t.setData(okp.getData());
                      t.setAddress(TAR + 4);
                      reportTransfer(t);
                      break;

            case BD2: t = new AP_Transfer();
                      t.setToWriteTransfer();
                      t.setPacketNumber(okp.getNumber());
                      t.setData(okp.getData());
                      t.setAddress(TAR + 8);
                      reportTransfer(t);
                      break;

            case BD3: t = new AP_Transfer();
                      t.setToWriteTransfer();
                      t.setPacketNumber(okp.getNumber());
                      t.setData(okp.getData());
                      t.setAddress(TAR + 12);
                      reportTransfer(t);
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
        if(true == activeTransfers.isEmpty())
        {
            // no finished transfer -> ignore data
        }
        else
        {
            AP_Transfer t = activeTransfers.elementAt(0);
            activeTransfers.remove(0);
            t.setData(okp.getData());
            reportTransfer(t);
        }
    }

    private void reportTransfer(AP_Transfer t)
    {
        if(t.isWrite())
        {
            reportWrite(t.getPacketNumber(), t.getAddress(), t.getData());
        }
        else
        {
            reportRead(t.getPacketNumber(), t.getAddress(), t.getData());
        }
    }

    private void reportRead(long number, long address, long data)
    {
        String Address = String.format("0x%08X", address);
        String desc = valDec.getShortNameFor(address);
        if(0 < desc.length())
        {
            Address = Address + " = " + desc;
        }
        desc = valDec.getLongNameFor(address);
        if(0 < desc.length())
        {
            Address = Address + " (" + desc + ")";
        }
        out.println(number + ": read  " + String.format("0x%08X", data) + " from " + Address);
        memoryReadMap.put(address, data);
    }

    private void reportWrite(long number, long address, long data)
    {
        String Address = String.format("0x%08X", address);
        String desc = valDec.getShortNameFor(address);
        if(0 < desc.length())
        {
            Address = Address + " = " + desc;
        }
        desc = valDec.getLongNameFor(address);
        if(0 < desc.length())
        {
            Address = Address + " (" + desc + ")";
        }
        out.println(number + ": wrote " + String.format("0x%08X", data) + "  to  " + Address);
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
            String desc = valDec.getLongNameFor(addr);
            if(0 < desc.length())
            {
                out.println(String.format("0x%08X (", addr) + desc + String.format(") = 0x%08X", val));
            }
            else
            {
                out.println(String.format("0x%08X", addr) + String.format(" = 0x%08X", val));
            }
        }
    }
}
