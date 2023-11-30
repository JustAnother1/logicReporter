package de.nomagic.logic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//  little endian

// byte[8] identifier;  // <SALEAE>
// int32 version;      // 0
// int32 type;         // 0
// uint32 initial_state;
// double begin_time;
// double end_time;
// uint64 num_transitions;
// for each transition in num_transitions
//     double transition_time;
// https://support.saleae.com/faq/technical-faq/binary-export-format-logic-2
public class SaleaDigitalChannel extends SampleSource
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private long transitions;
    private InputStream in;
    private double endTimeSeconds;

    public SaleaDigitalChannel(InputStream in)
    {
        super();
        this.in = in;

        if(null == in)
        {
            // no data source
            log.error("InputStream is NULL !");
            valid = false;
            return;
        }
        try
        {
            // <SALEAE>
            byte[] identifier = in.readNBytes(8);
            if(    (identifier[0] != '<')
                || (identifier[1] != 'S')
                || (identifier[2] != 'A')
                || (identifier[3] != 'L')
                || (identifier[4] != 'E')
                || (identifier[5] != 'A')
                || (identifier[6] != 'E')
                || (identifier[7] != '>')
                )
            {
                // invalid identifier - not a Saleae file?
                log.error("Identifier is {}!", identifier);
                valid = false;
                return;
            }

            // Version = 0
            byte[] version = in.readNBytes(4);
            if(    (version[0] != 0)
                || (version[1] != 0)
                || (version[2] != 0)
                || (version[3] != 0)
                )
            {
                // wrong file format version - can not decode data
                log.error("Version is {}!", version);
                valid = false;
                return;
            }

            // Type = 0 digital, 1 = analog
            byte[] type = in.readNBytes(4);
            if(    (type[0] != 0)
                || (type[1] != 0)
                || (type[2] != 0)
                || (type[3] != 0)
                )
            {
                // wrong data type - can not decode data
                log.error("Type is {}!", type);
                valid = false;
                return;
            }

            byte[] initialState = in.readNBytes(4);
            log.debug("initial State: {}", initialState[0]);
            if(    ((initialState[0] != 0) &&(initialState[0] != 1))
                || (initialState[1] != 0)
                || (initialState[2] != 0)
                || (initialState[3] != 0)
                )
            {
                // invalid initial state - corrupted data ?
                valid = false;
                return;
            }

            boolean initiallyHigh;
            if(0 == initialState[0])
            {
                initiallyHigh = false;
            }
            else if(1 == initialState[0])
            {
                initiallyHigh = true;
            }
            else
            {
                valid = false;
                return;
            }

            byte[] begin_time = in.readNBytes(8);
            double start_time = bytesToDouble(begin_time);
            log.debug("begin time: {} seconds", start_time);

            byte[] end_time = in.readNBytes(8);
            endTimeSeconds = bytesToDouble(end_time);
            log.debug("end time: {} seconds", endTimeSeconds);

            byte[] num_transitions = in.readNBytes(8);
            ByteBuffer n_buffer = ByteBuffer.wrap(num_transitions);
            n_buffer.order(ByteOrder.LITTLE_ENDIAN);
            transitions = n_buffer.getLong();
            log.debug("transitions: {}", transitions);

            if(1 > transitions)
            {
                valid = false;
                return;
            }

            initializeSampleQueue(initiallyHigh);
        }
        catch (IOException e)
        {
            // can not read sample data
            e.printStackTrace();
            valid = false;
            return;
        }
        // everything worked out OK
        valid = true;
    }

    public long getNumberEdges()
    {
        return transitions;
    }

    @Override
    protected double getNextEndTime()
    {
        byte[] readTimeBuf;
        try
        {
            readTimeBuf = in.readNBytes(8);
            if(8 == readTimeBuf.length)
            {
                double readTime = bytesToDouble(readTimeBuf);
                return readTime;
            }
            else
            {
                if(0 != readTimeBuf.length)
                {
                    log.trace("read {} bytes", readTimeBuf.length);
                }
                return endTimeSeconds;
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return endTimeSeconds;
        }
    }

    private double bytesToDouble(byte[] data)
    {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        double res = buffer.getDouble();
        return res;
    }

}
