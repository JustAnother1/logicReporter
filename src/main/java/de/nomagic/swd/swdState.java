package de.nomagic.swd;

import java.io.PrintStream;

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

    public swdState()
    {
        out = null;
        curLineStatus = lineState.UNKNOWN;
        lastLineStatus = lineState.UNKNOWN;
        SELECT = -1;
        memApAddr = -1;
        memAp = new MemoryAccessPortDecoder();
    }

    public void reportTo(PrintStream out)
    {
        this.out = out;
        memAp.reportTo(out);
    }

    public void add(SwdPacket nextPacket)
    {
        if(null == nextPacket)
        {
            return;
        }


        if(nextPacket instanceof Disconnect)
        {
            curLineStatus = lineState.SWD_NOT_CONNECTED;
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
        else if(nextPacket instanceof OkPacket)
        {
            curLineStatus = lineState.SWD;
            OkPacket okp = (OkPacket)nextPacket;
            okp.setSELECT(SELECT);
            SELECT = okp.getUpdatedSELECT();
            memAp.setSELECT(SELECT);
        }
        else if(nextPacket instanceof SwdToDormant)
        {
            curLineStatus = lineState.DORMANT;
        }
        else if(nextPacket instanceof WaitPacket)
        {
            curLineStatus = lineState.SWD;
        }

        nextPacket.reportTo(out);

        if(curLineStatus != lastLineStatus)
        {
            if(null != out)
            {
                out.println("state change: " + lastLineStatus + " -> " + curLineStatus);
            }
            lastLineStatus = curLineStatus;
        }

        // decode read /write AP
        if(nextPacket instanceof OkPacket)
        {
            OkPacket okp = (OkPacket)nextPacket;
            if(false == okp.getIsDp())
            {
                // AP packet
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
                memAp.add(okp);
            }
            else
            {
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

    }

}
