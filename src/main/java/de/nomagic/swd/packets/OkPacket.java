package de.nomagic.swd.packets;

import java.io.PrintStream;

import de.nomagic.logic.ValueDecoder;
import de.nomagic.swd.AP_Register;
import de.nomagic.swd.DP_Register;

public class OkPacket extends RequestPacket
{
    private final long number;
    private boolean isProcessed;
    private long data;
    private int dataParity;
    private long SELECT = -1;
    private AP_Register apReg = AP_Register.UNKNOWN;
    private DP_Register dpReg = DP_Register.UNKNOWN;
    private ValueDecoder valDec = null;

    // TODO check that parity is correct

    public OkPacket(long packetNumber)
    {
        number = packetNumber;
        isProcessed = false;
    }

    protected String specificReport()
    {
        return ",ACK=OK,data=" + String.format("0x%08X", data) + ", dpararty=" + dataParity;
    }

    @Override
    public void reportTo(PrintStream out)
    {
        if(false == isProcessed)
        {
            process();
        }
        StringBuffer buf = new StringBuffer();
        if(true == isRead)
        {
            buf.append("Reading ");
            if(true == isDP)
            {
                buf.append("DP:");
                switch(a2a3)
                {
                case 0: switch((int)(SELECT & 0xf))
                        {
                        default: buf.append("???"); break;
                        case  0: buf.append("DPIDR"); break;
                        case  1: buf.append("DPIDR1"); break;
                        case  2: buf.append("BASEPTR0"); break;
                        case  3: buf.append("BASEPTR1"); break;
                        }
                        break;

                case 1: switch((int)(SELECT & 0xf))
                        {
                        default: buf.append("???"); break;
                        case  0: buf.append("CTRL/STAT"); break;
                        case  1: buf.append("DLCR"); break;
                        case  2: buf.append("TARGETID"); break;
                        case  3: buf.append("DLPIDR"); break;
                        case  4: buf.append("EVENTSTAT"); break;
                        }
                        break;

                case 2:buf.append("RESEND"); break;
                case 3:buf.append("RDBUFF"); break;
                }
            }
            else
            {
                long id = (SELECT & 0xff000000)>>24;
                int ap_Addr = (a2a3 * 4) + (int)(SELECT & 0xff0);
                buf.append("AP(" + id + "):");
                switch(ap_Addr)
                {
                case 0   : buf.append(" CSW");   break;
                case 4   : // fall through
                case 8   : buf.append(" TAR");   break;
                case 0xc : buf.append(" DRW");   break;
                case 0x10: buf.append(" BD0");   break;
                case 0x14: buf.append(" BD1");   break;
                case 0x18: buf.append(" BD2");   break;
                case 0x1c: buf.append(" BD3");   break;
                case 0x20: buf.append(" MBT");   break;
                case 0x30: buf.append(" T0TR");  break;
                case 0xe0: buf.append(" CFG1");  break;
                case 0xf0: buf.append(" BASE1"); break;
                case 0xf4: buf.append(" CFG");   break;
                case 0xf8: buf.append(" BASE");  break;
                case 0xfc: buf.append(" IDR");   break;
                }
                buf.append(String.format("(0x%X) ", ap_Addr) );
                buf.append("(a2a3 = " + a2a3 + ")");
            }
        }
        else
        {
            buf.append("Writing ");
            if(true == isDP)
            {
                buf.append("DP:");
                switch(a2a3)
                {
                case 0: buf.append("ABORT"); break;
                case 1: switch((int)(SELECT & 0xf))
                        {
                        default: buf.append("???"); break;
                        case  0: buf.append("CTRL/STAT"); break;
                        case  1: buf.append("DLCR"); break;
                        case  5: buf.append("SELECT1"); break;
                        }
                        break;

                case 2: buf.append("SELECT"); break;
                case 3: buf.append("TARGETSEL"); break;
                }
            }
            else
            {
                long id = (SELECT & 0xff000000)>>24;
                int ap_Addr = (a2a3 * 4) + (int)(SELECT & 0xff0);
                buf.append("AP(" + id + "):");
                switch(ap_Addr)
                {
                case 0   : buf.append(" CSW");   break;
                case 4   : // fall through
                case 8   : buf.append(" TAR");   break;
                case 0xc : buf.append(" DRW");   break;
                case 0x10: buf.append(" BD0");   break;
                case 0x14: buf.append(" BD1");   break;
                case 0x18: buf.append(" BD2");   break;
                case 0x1c: buf.append(" BD3");   break;
                case 0x20: buf.append(" MBT");   break;
                case 0x30: buf.append(" T0TR");  break;
                case 0xe0: buf.append(" CFG1");  break;
                case 0xf0: buf.append(" BASE1"); break;
                case 0xf4: buf.append(" CFG");   break;
                case 0xf8: buf.append(" BASE");  break;
                case 0xfc: buf.append(" IDR");   break;
                }
                buf.append(String.format("(0x%X) ", ap_Addr) );
                buf.append("(a2a3 = " + a2a3 + ")");
            }
        }

        buf.append(" data=" + String.format("0x%08X", data) );
        if(AP_Register.TAR == apReg)
        {
            String desc = valDec.getShortNameFor(data);
            if(null != desc)
            {
                buf.append(" = " + desc);
            }
            desc = valDec.getLongNameFor(data);
            if(null != desc)
            {
                buf.append(" (" + desc + ")");
            }
        }

        out.println(buf.toString());
    }


    public void process()
    {
        if(true == isRead)
        {
            if(true == isDP)
            {
                switch(a2a3)
                {
                case 0: switch((int)(SELECT & 0xf))
                        {
                        default: dpReg = DP_Register.UNKNOWN; break;
                        case  0: dpReg = DP_Register.DPIDR; break;
                        case  1: dpReg = DP_Register.DPIDR1; break;
                        case  2: dpReg = DP_Register.BASEPTR0; break;
                        case  3: dpReg = DP_Register.BASEPTR1; break;
                        }
                        break;

                case 1: switch((int)(SELECT & 0xf))
                        {
                        default: dpReg = DP_Register.UNKNOWN; break;
                        case  0: dpReg = DP_Register.CTRL_STAT; break;
                        case  1: dpReg = DP_Register.DLCR; break;
                        case  2: dpReg = DP_Register.TARGETID; break;
                        case  3: dpReg = DP_Register.DLPIDR; break;
                        case  4: dpReg = DP_Register.EVENTSTAT; break;
                        }
                        break;

                case 2: dpReg = DP_Register.RESEND; break;
                case 3: dpReg = DP_Register.RDBUFF; break;
                }
            }
            else
            {
                int ap_Addr = (a2a3 * 4) + (int)(SELECT & 0xff0);
                switch(ap_Addr)
                {
                case 0   : apReg = AP_Register.CSW;   break;
                case 4   : // fall through
                case 8   : apReg = AP_Register.TAR;   break;
                case 0xc : apReg = AP_Register.DRW;   break;
                case 0x10: apReg = AP_Register.BD0;   break;
                case 0x14: apReg = AP_Register.BD1;   break;
                case 0x18: apReg = AP_Register.BD2;   break;
                case 0x1c: apReg = AP_Register.BD3;   break;
                case 0x20: apReg = AP_Register.MBT;   break;
                case 0x30: apReg = AP_Register.T0TR;  break;
                case 0xe0: apReg = AP_Register.CFG1;  break;
                case 0xf0: apReg = AP_Register.BASE1; break;
                case 0xf4: apReg = AP_Register.CFG;   break;
                case 0xf8: apReg = AP_Register.BASE;  break;
                case 0xfc: apReg = AP_Register.IDR;   break;
                }

            }
        }
        else
        {
            if(true == isDP)
            {
                switch(a2a3)
                {
                case 0: dpReg = DP_Register.ABORT; break;
                case 1: switch((int)(SELECT & 0xf))
                        {
                        default: dpReg = DP_Register.UNKNOWN; break;
                        case  0: dpReg = DP_Register.CTRL_STAT; break;
                        case  1: dpReg = DP_Register.DLCR; break;
                        case  5: dpReg = DP_Register.SELECT1; break;
                        }
                        break;

                case 2: dpReg = DP_Register.SELECT; break;
                case 3: dpReg = DP_Register.TARGETSEL; break;
                }
            }
            else
            {
                int ap_Addr = (a2a3 * 4) + (int)(SELECT & 0xff0);
                switch(ap_Addr)
                {
                case 0   : apReg = AP_Register.CSW;   break;
                case 4   : // fall through
                case 8   : apReg = AP_Register.TAR;   break;
                case 0xc : apReg = AP_Register.DRW;   break;
                case 0x10: apReg = AP_Register.BD0;   break;
                case 0x14: apReg = AP_Register.BD1;   break;
                case 0x18: apReg = AP_Register.BD2;   break;
                case 0x1c: apReg = AP_Register.BD3;   break;
                case 0x20: apReg = AP_Register.MBT;   break;
                case 0x30: apReg = AP_Register.T0TR;  break;
                case 0xe0: apReg = AP_Register.CFG1;  break;
                case 0xf0: apReg = AP_Register.BASE1; break;
                case 0xf4: apReg = AP_Register.CFG;   break;
                case 0xf8: apReg = AP_Register.BASE;  break;
                case 0xfc: apReg = AP_Register.IDR;   break;
                }
            }
        }
        isProcessed = true;
    }

    public AP_Register getAPRegType()
    {
        if(false == isProcessed)
        {
            process();
        }
        return apReg;
    }

    public DP_Register getDPRegType()
    {
        if(false == isProcessed)
        {
            process();
        }
        return dpReg;
    }

    public void setData(long data)
    {
        this.data = data;
    }

    public long getData()
    {
        return data;
    }

    public void setDataParity(Integer val)
    {
        this.dataParity = val;
    }

    public void setSELECT(long val)
    {
        this.SELECT = val;
    }

    public long getUpdatedSELECT()
    {
        if((false == isRead) && (true == isDP) && (2 == a2a3))
        {
            // write to SELECT Register
            return data;
        }
        else
        {
            return SELECT;
        }
    }

    public long getNumber()
    {
        return number;
    }

    public void addDecoder(ValueDecoder valDec)
    {
        this.valDec = valDec;
    }

}
