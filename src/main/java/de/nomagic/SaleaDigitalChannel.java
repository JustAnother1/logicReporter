package de.nomagic;

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
public class SaleaDigitalChannel
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private boolean valid;
    private boolean initiallyHigh;
    private long transitions;
    private double sampleTime;
    private InputStream in;

    private boolean curHigh;
    private double nextTime;
    private boolean nextHigh;

    public SaleaDigitalChannel(InputStream in)
    {
        this.in = in;

        if(null == in)
        {
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
                valid = false;
                return;
            }
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
            log.debug("begin time: {}", start_time);

            byte[] end_time = in.readNBytes(8);
            double d_end_time = bytesToDouble(end_time);
            log.debug("end time: {}", d_end_time);

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
            sampleTime = 0.0;
            if(true == initiallyHigh)
            {
                log.debug("initially High");
                curHigh = true;
                nextHigh = false;
            }
            else
            {
                log.debug("initially Low");
                curHigh = false;
                nextHigh = true;
            }

            byte[] readTimeBuf = in.readNBytes(8);
            double readTime = bytesToDouble(readTimeBuf);
            nextTime = readTime;

            /* count transitions
            long countedTransitions = 0;
            double lastSampleTime = 0;
            while(7 < in.available())
            {
                byte[] sample_time = in.readNBytes(8);
                ByteBuffer sample_buffer = ByteBuffer.wrap(sample_time);
                sample_buffer.order(ByteOrder.LITTLE_ENDIAN);
                double d_sample_time = sample_buffer.getDouble();
                // log.debug("sample time: {}", d_sample_time);
                if(lastSampleTime < d_sample_time)
                {
                    // OK
                }
                else
                {
                    log.error("sample time jumped from {} to {} !", lastSampleTime, d_sample_time);
                    valid = false;
                }
                lastSampleTime = d_sample_time;
                countedTransitions++;
            }
            log.debug("counted transitions: {}", countedTransitions);
            */
        }
        catch (IOException e)
        {
            e.printStackTrace();
            valid = false;
            return;
        }
        valid = true;
    }

    public boolean isValid()
    {
        return valid;
    }

    public boolean isInitiallyHigh()
    {
        return initiallyHigh;
    }

    public long getNumberEdges()
    {
        return transitions;
    }

    public double getTimeOfEdgeAfter(double nowTime) throws IOException
    {
        if(sampleTime >= nowTime)
        {
            return sampleTime;
        }
        else
        {
            // search for sample after provided time
            while(7 < in.available())
            {
                byte[] sample_time = in.readNBytes(8);
                double d_sample_time = bytesToDouble(sample_time);

                if(sampleTime < d_sample_time)
                {
                    // OK
                }
                else
                {
                    log.error("sample time jumped from {} to {} !", sampleTime, d_sample_time);
                    valid = false;
                    System.exit(1);
                }
                sampleTime = d_sample_time;
                if(sampleTime < nowTime)
                {
                    // we found the edge after the requested time
                    return sampleTime;
                }
            }
            log.error("reached end of file !", sampleTime);
            return sampleTime;
        }
    }

    public boolean isHighAt(double nowTime) throws IOException
    {
        while(nowTime > nextTime)
        {
            if(7 < in.available())
            {
                byte[] readTimeBuf = in.readNBytes(8);
                double readTime = bytesToDouble(readTimeBuf);
                // next -> cur
                curHigh = nextHigh;
                // read is new next
                nextTime = readTime;
                nextHigh = !curHigh;
            }
            else
            {
                // no next edge
                nextTime = nowTime;
            }
        }
        return curHigh;
    }


    private double bytesToDouble(byte[] data)
    {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        double res = buffer.getDouble();
        return res;
    }

}
