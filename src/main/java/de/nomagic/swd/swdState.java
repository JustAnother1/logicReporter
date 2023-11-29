package de.nomagic.swd;

import java.io.PrintStream;

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

public class swdState
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

    public swdState()
    {
        out = null;
        curLineStatus = lineState.UNKNOWN;
        lastLineStatus = lineState.UNKNOWN;
    }

    public void reportTo(PrintStream out)
    {
        this.out = out;
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
        }
        else if(nextPacket instanceof OkPacket)
        {
            curLineStatus = lineState.SWD;
        }
        else if(nextPacket instanceof SwdToDormant)
        {
            curLineStatus = lineState.DORMANT;
        }
        else if(nextPacket instanceof WaitPacket)
        {
            curLineStatus = lineState.SWD;
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

}
