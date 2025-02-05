package de.nomagic.swd;

import java.io.PrintStream;

import de.nomagic.Configuration;
import de.nomagic.PacketSequence;
import de.nomagic.logic.ValueDecoder;
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

public class swdState implements PacketSequence
{
    private PrintStream out;

    enum lineState {
        UNKNOWN,
        JTAG,
        DORMANT,
        SWD,
        SWD_NOT_CONNECTED,
        ERROR,
    }

    private lineState curLineStatus;
    private lineState lastLineStatus;
    private long SELECT;
    private MemoryAccessPortDecoder memAp;
    private int memApAddr;
    private boolean reportDP = false;
    private boolean reportAP = false;
    private long numPackets = 0;
    private long numOpPackets = 0;
    private long numApPackets = 0;
    private long numDpPackets = 0;

    public swdState(ValueDecoder valDec)
    {
        out = null;
        curLineStatus = lineState.UNKNOWN;
        lastLineStatus = lineState.UNKNOWN;
        SELECT = -1;
        memApAddr = -1;
        memAp = new MemoryAccessPortDecoder(valDec);
    }

    public void reportTo(PrintStream out)
    {
        this.out = out;
        memAp.reportTo(out);
    }

    public void setConfiguration(Configuration cfg)
    {
        if(null == cfg)
        {
            return;
        }
        boolean val = cfg.shallReportDP();
        if(true == val)
        {
            reportDP = true;
        }
        else
        {
            reportDP = false;
        }
        val = cfg.shallReportAP();
        if(true == val)
        {
            reportAP = true;
        }
        else
        {
            reportAP = false;
        }
    }

    public void add(SwdPacket nextPacket)
    {
        if(null == nextPacket)
        {
            return;
        }
        boolean shouldBeReported = true;
        numPackets++;

        if(nextPacket instanceof OkPacket)
        {
            numOpPackets++;
            curLineStatus = lineState.SWD;
            OkPacket okp = (OkPacket)nextPacket;
            okp.setSELECT(SELECT);
            okp.process();
            if(false == okp.getIsDp())
            {
                // AP packet
                memAp.add(okp);
                numApPackets++;
                if(false == reportAP)
                {
                    shouldBeReported = false;
                }
                if(memApAddr != ((SELECT&0xff000000)>>24))
                {
                    int previousAP = memApAddr;
                    memApAddr = (int)((SELECT & 0xff000000) >>24);
                    if(-1 == previousAP)
                    {
                        // first access to AP
                        out.println("using Address Port " + memApAddr);
                    }
                    else
                    {
                        out.println("chnaging from AP " + previousAP + "to Address Port " + memApAddr);
                    }
                }
            }
            else
            {
                // DP packet
                numDpPackets++;
                if(false == reportDP)
                {
                    shouldBeReported = false;
                }
                SELECT = okp.getUpdatedSELECT();
                DP_Register dpReg = okp.getDPRegType();
                if(DP_Register.RESEND == dpReg)
                {
                    memAp.addResend(okp);
                }
                else if(DP_Register.RDBUFF == dpReg)
                {
                    memAp.addRdBuff(okp);
                }
            }
        }
        else if(nextPacket instanceof DormantToSwd)
        {
            curLineStatus = lineState.SWD;
        }
        else if(nextPacket instanceof FaultPacket)
        {
            curLineStatus = lineState.ERROR;
        }
        else if(nextPacket instanceof IdleBits)
        {
            curLineStatus = lineState.SWD;
        }
        else if(nextPacket instanceof InvalidBits)
        {
            curLineStatus = lineState.ERROR;
        }
        else if(nextPacket instanceof JtagToDormant)
        {
            curLineStatus = lineState.DORMANT;
        }
        else if(nextPacket instanceof JtagToSwd)
        {
            curLineStatus = lineState.SWD;
        }
        else if(nextPacket instanceof LineReset)
        {
            curLineStatus = lineState.SWD;
            SELECT = 0;
        }
        else if(nextPacket instanceof Disconnect)
        {
            curLineStatus = lineState.SWD_NOT_CONNECTED;
        }
        else if(nextPacket instanceof SwdToDormant)
        {
            curLineStatus = lineState.DORMANT;
        }
        else if(nextPacket instanceof WaitPacket)
        {
            curLineStatus = lineState.SWD;
        }

        if(true == shouldBeReported)
        {
            nextPacket.reportTo(out);
        }

        if(curLineStatus != lastLineStatus)
        {
            if(null != out)
            {
                out.println("state change: " + lastLineStatus + " -> " + curLineStatus);
            }
            lastLineStatus = curLineStatus;
        }
    }

    public void printSummary()
    {
        out.println("found " + numPackets   + " SWD Packets");
        out.println("found " + numOpPackets + " OK Packets");
        out.println("found " + numDpPackets + " DP Packets");
        out.println("found " + numApPackets + " AP Packets");
        out.println("accessed Memory:");
        memAp.printMemoryMap(out);
    }

}
