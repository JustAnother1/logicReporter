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
                buf.append(dpReg.toString());
                buf.append(" data=" + String.format("0x%08X", data) );
                buf.append(parseDpRead());
            }
            else
            {
                long id = (SELECT & 0xff000000)>>24;
                int ap_Addr = (a2a3 * 4) + (int)(SELECT & 0xff0);
                buf.append("AP(" + id + "):");
                buf.append(apReg.toString());
                buf.append(String.format("(0x%X) ", ap_Addr) );
                buf.append("(a2a3 = " + a2a3 + ")");
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
                buf.append(parseApRead());
            }
        }
        else
        {
            buf.append("Writing ");
            if(true == isDP)
            {
                buf.append("DP:");
                buf.append(dpReg.toString());
                buf.append(" data=" + String.format("0x%08X", data) );
                buf.append(parseDpWrite());
            }
            else
            {
                long id = (SELECT & 0xff000000)>>24;
                int ap_Addr = (a2a3 * 4) + (int)(SELECT & 0xff0);
                buf.append("AP(" + id + "):");
                buf.append(apReg.toString());
                buf.append(String.format("(0x%X) ", ap_Addr) );
                buf.append("(a2a3 = " + a2a3 + ")");
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
                buf.append(parseApWrite());
            }
        }

        out.println(buf.toString());
    }

    private String parseApWrite()
    {
        long help;
        StringBuffer buf = new StringBuffer();
        switch(apReg)
        {
        case CSW:
            if(0 != (data & 0x80000000))
            {
                buf.append("\r\nDebug access enabled");
            }
            else
            {
                buf.append("\r\nDebug access disabled. really? -> ignore");
            }

            help = ((data>>24) & 0x7f);
            buf.append("\r\nProt: " + help);

            if(0 != (data & 0x800000))
            {
                buf.append("\r\nSecure access enabled");
            }

            help = ((data>>8) & 0xf);
            buf.append("\r\nMode: " + help);
            if(0 != (data & 0x80))
            {
                buf.append("\r\nTransfere in progress");
            }
            if(0 != (data & 0x40))
            {
                buf.append("\r\nMem-AP is enabled");
            }

            help = ((data>>4) & 0x3);
            switch((int)help)
            {
            case 0: buf.append("\r\nAddress auto increment disabled"); break;
            case 1: buf.append("\r\nAddress auto increment enabled"); break;
            case 2: buf.append("\r\nAddress auto increment as packets"); break;
            case 3: buf.append("\r\nINVALID: reserved value!"); break;
            }

            help = (data & 0x7);
            buf.append("\r\nSize: ");
            switch((int)help)
            {
            case 0: buf.append("8 bits"); break;
            case 1: buf.append("16 bits"); break;
            case 2: buf.append("32 bits"); break;
            case 3: buf.append("64 bits"); break;
            case 4: buf.append("128 bits"); break;
            case 5: buf.append("256 bits"); break;
            default: buf.append("RESERVED"); break;
            }
            break;

        case T0TR:
            help = ((data>>28) & 0xf);
            buf.append("\r\nT7: " + help);

            help = ((data>>24) & 0xf);
            buf.append("\r\nT6: " + help);

            help = ((data>>20) & 0xf);
            buf.append("\r\nT5: " + help);

            help = ((data>>16) & 0xf);
            buf.append("\r\nT4: " + help);

            help = ((data>>12) & 0xf);
            buf.append("\r\nT3: " + help);

            help = ((data>>8) & 0xf);
            buf.append("\r\nT2: " + help);

            help = ((data>>4) & 0xf);
            buf.append("\r\nT1: " + help);

            help = (data & 0xf);
            buf.append("\r\nT0: " + help);
            break;

        case BD0:
        case BD1:
        case BD2:
        case BD3:
        case DRW:
        case MBT:
        case TAR:
            break;

        case IDR:
        case BASE:
        case BASE1:
        case CFG:
        case CFG1:
            buf.append("\r\nERROR: writing to a read only register!");
            break;

        case UNKNOWN:
        default:
            buf.append("\r\nERROR: unknown register!");
            break;
        }
        return buf.toString();
    }

    private String parseApRead()
    {
        long help;
        StringBuffer buf = new StringBuffer();
        switch(apReg)
        {
        case IDR:
            help = ((data>>28) & 0xf);
            buf.append("\r\nREVISION: " + help);

            help = ((data>>17) & 0x7ff);
            buf.append("\r\nDESIGNER: " + help);

            help = ((data>>13) & 0xf);
            buf.append("\r\nClass: ");
            switch((int)help)
            {
            case 0: buf.append("JTAG (no class defined)"); break;
            case 1: buf.append("COM Access port");break;
            case 8: buf.append("Memory Access Port"); break;
            default: buf.append("invalid CLASS: " + help); break;
            }


            help = ((data>>4) & 0xf);
            buf.append("\r\nVARIANT: " + help);

            help = (data & 0xf);
            switch((int)help)
            {
            case 0: buf.append("\r\nType: see class"); break;
            case 1: buf.append("\r\nAMBA AHB3 bus");break;
            case 2: buf.append("\r\nAMBA APB2 or APB3 bus");break;
            case 4: buf.append("\r\nAMBA AXI3 or AXI4 bus with optional ACE-Lite support");break;
            case 5: buf.append("\r\nAMBA AHB5 bus");break;
            case 6: buf.append("\r\nAMBA APB4 and APB5 bus");break;
            case 7: buf.append("\r\nAMBA AX15 bus");break;
            case 8: buf.append("\r\nAMBA AHB5 with enhanced HPROT bus");break;
            default: buf.append("\r\ninvalid TYPE: " + help); break;
            }
            break;

        case BASE:
            help = ((data>>12) & 0xfffff);
            buf.append("\r\nVARIANT: " + help);
            break;

        case CFG:
            if(0 != (data & 0x04))
            {
                buf.append("\r\nsupport for data items bigger than 32bit");
            }
            if(0 != (data & 0x02))
            {
                buf.append("\r\nsupport for addresses with more than 32bit");
            }
            if(0 != (data & 0x01))
            {
                buf.append("\r\nsupport for Big Endian");
            }
            break;

        case CFG1:
            help = ((data>>4) & 0xf);
            buf.append("\r\nTAG0GRAN: " + help);
            help = (data & 0xf);
            buf.append("\r\nTAG0SIZE: " + help);
            break;

        case CSW:
            if(0 != (data & 0x80000000))
            {
                buf.append("\r\nDebug access enabled");
            }
            else
            {
                buf.append("\r\nDebug access disabled. really? -> ignore");
            }

            help = ((data>>24) & 0x7f);
            buf.append("\r\nProt: " + help);

            if(0 != (data & 0x800000))
            {
                buf.append("\r\nSecure access enabled");
            }

            help = ((data>>8) & 0xf);
            buf.append("\r\nMode: " + help);
            if(0 != (data & 0x80))
            {
                buf.append("\r\nTransfere in progress");
            }
            if(0 != (data & 0x40))
            {
                buf.append("\r\nMem-AP is enabled");
            }

            help = ((data>>4) & 0x3);
            switch((int)help)
            {
            case 0: buf.append("\r\nAddress auto increment disabled"); break;
            case 1: buf.append("\r\nAddress auto increment enabled"); break;
            case 2: buf.append("\r\nAddress auto increment as packets"); break;
            case 3: buf.append("\r\nINVALID: reserved value!"); break;
            }

            help = (data & 0x7);
            buf.append("\r\nSize: ");
            switch((int)help)
            {
            case 0: buf.append("8 bits"); break;
            case 1: buf.append("16 bits"); break;
            case 2: buf.append("32 bits"); break;
            case 3: buf.append("64 bits"); break;
            case 4: buf.append("128 bits"); break;
            case 5: buf.append("256 bits"); break;
            default: buf.append("RESERVED"); break;
            }
            break;

        case T0TR:
            help = ((data>>28) & 0xf);
            buf.append("\r\nT7: " + help);

            help = ((data>>24) & 0xf);
            buf.append("\r\nT6: " + help);

            help = ((data>>20) & 0xf);
            buf.append("\r\nT5: " + help);

            help = ((data>>16) & 0xf);
            buf.append("\r\nT4: " + help);

            help = ((data>>12) & 0xf);
            buf.append("\r\nT3: " + help);

            help = ((data>>8) & 0xf);
            buf.append("\r\nT2: " + help);

            help = ((data>>4) & 0xf);
            buf.append("\r\nT1: " + help);

            help = (data & 0xf);
            buf.append("\r\nT0: " + help);
            break;

        case BASE1:
        case BD0:
        case BD1:
        case BD2:
        case BD3:
        case DRW:
        case MBT:
        case TAR:
            break;

        case UNKNOWN:
        default:
            buf.append("\r\nERROR: unknown register!");
            break;
        }
        return buf.toString();
    }

    private String parseDpWrite()
    {
        long help;
        StringBuffer buf = new StringBuffer();
        switch(dpReg)
        {
        case ABORT:
            if(0 != (data & 0x1))
            {
                buf.append("\r\nHost tries to abort the current opperation (to many waits received)");
            }
            if(0 != (data & 0x2))
            {
                buf.append("\r\nclear CTRL/STAT.STICKYCMP");
            }
            if(0 != (data & 0x4))
            {
                buf.append("\r\nclear CTRL/STAT.STICKYERR");
            }
            if(0 != (data & 0x8))
            {
                buf.append("\r\nclear CTRL/STAT.WDATAERR");
            }
            if(0 != (data & 0x10))
            {
                buf.append("\r\nclear CTRL/STAT.STICKORUN");
            }
            break;

        case CTRL_STAT:
            help = ((data>>30) & 0x3);
            switch((int)help)
            {
            case 0: buf.append("\r\nPower off"); break;
            case 1: buf.append("\r\nrequest Power on"); break;
            case 2: buf.append("\r\nINVALID Power !"); break;
            case 3: buf.append("\r\nPower on"); break;
            }

            help = ((data>>28) & 0x3);
            switch((int)help)
            {
            case 0: buf.append("\r\nDebug off"); break;
            case 1: buf.append("\r\nrequest Debug on"); break;
            case 2: buf.append("\r\nINVALID Debug !"); break;
            case 3: buf.append("\r\nDebug on"); break;
            }
            help = ((data>>26) & 0x3);
            switch((int)help)
            {
            case 0: break; // no reset requested or acked.
            case 1: buf.append("\r\nrequest Reset"); break;
            case 2: buf.append("\r\nINVALID Reset !"); break;
            case 3: buf.append("\r\nReset request acknowleadged"); break;
            }

            if(0 != ((data>>12) & 0xfff))
            {
                buf.append("\r\nTransaction counter: " + ((data>>12) & 0xfff));
            }
            help = ((data>>8) & 0xf);
            if(0 != help)
            {
                buf.append("\r\nMasklane: include ");
                if(0 != (help & 0x1))
                {
                    buf.append("byte 0 ");
                }
                if(0 != (help & 0x2))
                {
                    buf.append("byte 1 ");
                }
                if(0 != (help & 0x4))
                {
                    buf.append("byte 2 ");
                }
                if(0 != (help & 0x8))
                {
                    buf.append("byte 3 ");
                }
                buf.append("in comparison ");
            }
            if(0 != (data & 0x80))
            {
                buf.append("\r\nWDATAERR=1");
            }
            if(0 != (data & 0x40))
            {
                buf.append("\r\nREADOK=1");
            }
            if(0 != (data & 0x20))
            {
                buf.append("\r\nSTICKYERR=1");
            }
            if(0 != (data & 0x10))
            {
                buf.append("\r\nSTICKYCMP=1");
            }
            help = ((data>>2) & 0x3);
            switch((int)help)
            {
            case 0: break; // normal mode
            case 1: buf.append("\r\npushed-verify mode"); break;
            case 2: buf.append("\r\npushed-compare mode"); break;
            case 3: buf.append("\r\nINVALID: reserved value!"); break;
            }

            if(0 != (data & 0x2))
            {
                buf.append("\r\nSTICKYORUN=1 (overrun detected)");
            }
            if(0 != (data & 0x1))
            {
                buf.append("\r\noverrun detection enabled");
            }
            break;

        case DLCR:
            buf.append("\r\nTurnaround tristate for ");
            help = ((data>>8) & 0x3);
            switch((int)help)
            {
            case 0: buf.append("1 bit"); break;
            case 1: buf.append("2 bit"); break;
            case 2: buf.append("3 bit"); break;
            case 3: buf.append("4 bit"); break;
            }
            break;

        case SELECT:
            help = ((data>>24) & 0xff);
            buf.append("\r\nAPSEL: " + help);

            help = ((data>>4) & 0xf);
            buf.append("\r\nAPBANKSEL: " + help);

            help = (data & 0xf);
            buf.append("\r\nDPBANKSEL: " + help);
            break;

        case TARGETSEL:
            help = ((data>>28) & 0xf);
            buf.append("\r\nTINSTANCE: " + help);

            help = ((data>>12) & 0xffff);
            buf.append("\r\nTPARTNO: " + help);

            help = ((data>>1) & 0x7ff);
            buf.append("\r\nTDESIGNER: " + help);
            break;


        case DLPIDR:
        case DPIDR:
        case EVENTSTAT:
        case RDBUFF:
        case RESEND:
        case TARGETID:
            buf.append("\r\nERROR: writing to a read only register!");
            break;


        case UNKNOWN:
        default:
            buf.append("\r\nERROR: unknown register!");
            break;
        }
        return buf.toString();
    }

    private String parseDpRead()
    {
        long help;
        StringBuffer buf = new StringBuffer();
        switch(dpReg)
        {
        case CTRL_STAT:
            help = ((data>>30) & 0x3);
            switch((int)help)
            {
            case 0: buf.append("\r\nPower off"); break;
            case 1: buf.append("\r\nrequest Power on"); break;
            case 2: buf.append("\r\nINVALID Power !"); break;
            case 3: buf.append("\r\nPower on"); break;
            }

            help = ((data>>28) & 0x3);
            switch((int)help)
            {
            case 0: buf.append("\r\nDebug off"); break;
            case 1: buf.append("\r\nrequest Debug on"); break;
            case 2: buf.append("\r\nINVALID Debug !"); break;
            case 3: buf.append("\r\nDebug on"); break;
            }
            help = ((data>>28) & 0x3);
            switch((int)help)
            {
            case 0: break; // no reset requested or acked.
            case 1: buf.append("\r\nrequest Reset"); break;
            case 2: buf.append("\r\nINVALID Reset !"); break;
            case 3: buf.append("\r\nReset request acknowleadged"); break;
            }

            if(0 != ((data>>12) & 0xfff))
            {
                buf.append("\r\nTransaction counter: " + ((data>>12) & 0xfff));
            }
            help = ((data>>8) & 0xf);
            if(0 != help)
            {
                buf.append("\r\nMasklane: include ");
                if(0 != (help & 0x1))
                {
                    buf.append("byte 0 ");
                }
                if(0 != (help & 0x2))
                {
                    buf.append("byte 1 ");
                }
                if(0 != (help & 0x4))
                {
                    buf.append("byte 2 ");
                }
                if(0 != (help & 0x8))
                {
                    buf.append("byte 3 ");
                }
                buf.append("in comparison ");
            }
            if(0 != (data & 0x80))
            {
                buf.append("\r\nWDATAERR=1");
            }
            if(0 != (data & 0x40))
            {
                buf.append("\r\nREADOK=1");
            }
            if(0 != (data & 0x20))
            {
                buf.append("\r\nSTICKYERR=1");
            }
            if(0 != (data & 0x10))
            {
                buf.append("\r\nSTICKYCMP=1");
            }
            help = ((data>>2) & 0x3);
            switch((int)help)
            {
            case 0: break; // normal mode
            case 1: buf.append("\r\npushed-verify mode"); break;
            case 2: buf.append("\r\npushed-compare mode"); break;
            case 3: buf.append("\r\nINVALID: reserved value!"); break;
            }

            if(0 != (data & 0x2))
            {
                buf.append("\r\nSTICKYORUN=1 (overrun detected)");
            }
            if(0 != (data & 0x1))
            {
                buf.append("\r\noverrun detection enabled");
            }
            break;

        case DLCR:
            buf.append("\r\nTurnaround tristate for ");
            help = ((data>>8) & 0x3);
            switch((int)help)
            {
            case 0: buf.append("1 bit"); break;
            case 1: buf.append("2 bit"); break;
            case 2: buf.append("3 bit"); break;
            case 3: buf.append("4 bit"); break;
            }
            break;

        case DLPIDR:
            buf.append("\r\nTINSTANCE = " + ((data>>28) & 0xf) + " (TargetId)");
            help = (data & 0xf);
            buf.append("\r\nSWD protocol version ");
            if(1 == help)
            {
                buf.append("2");
            }
            else
            {
                buf.append("invalid(" + help + ")");
            }
            break;

        case DPIDR:
            help = ((data>>28) & 0xf);
            buf.append("\r\nRevision: " + help);

            help = ((data>>20) & 0xff);
            buf.append("\r\nPartNo: " + help);

            if(0 != (data & 0x10000))
            {
                buf.append("\r\nMinimal Debug Port (limited functionality)");
            }

            help = ((data>>12) & 0xf);
            buf.append("\r\nVersion: " + help);

            help = ((data>>1) & 0x7ff);
            buf.append("\r\nDesigner: " + help);
            break;

        case EVENTSTAT:
            if(0 != (data & 0x1))
            {
                buf.append("\r\nThere is no event requirering attention.");
            }
            else
            {
                buf.append("\r\nThere is an event requirering attention.");
            }
            break;

        case SELECT:
            help = ((data>>24) & 0xff);
            buf.append("\r\nAPSEL: " + help);

            help = ((data>>4) & 0xf);
            buf.append("\r\nAPBANKSEL: " + help);

            help = (data & 0xf);
            buf.append("\r\nDPBANKSEL: " + help);
            break;

        case TARGETID:
            help = ((data>>28) & 0xf);
            buf.append("\r\nTREVISION: " + help);

            help = ((data>>12) & 0xffff);
            buf.append("\r\nTPARTNO: " + help);

            help = ((data>>1) & 0x7ff);
            buf.append("\r\nTDESIGNER: " + help);
            break;

        case RDBUFF:
        case RESEND:
            break;

        case ABORT:
        case TARGETSEL:
            buf.append("\r\nERROR: reading from a write only register!");
            break;

        case UNKNOWN:
        default:
            buf.append("\r\nERROR: unknown register!");
            break;
        }
        return buf.toString();
    }

}
