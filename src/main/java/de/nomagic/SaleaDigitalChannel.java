package de.nomagic;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jdk.internal.org.jline.utils.Log;

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

    public SaleaDigitalChannel(InputStream in)
    {
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

            byte[] begin_time = in.readNBytes(8);
            ByteBuffer buffer = ByteBuffer.wrap(begin_time);
            log.debug("Buffer default byte order: {}", buffer.order());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            double start_time = buffer.getDouble();
            log.debug("begin time: {} ({})", start_time, begin_time);


            byte[] end_time = in.readNBytes(8);
            ByteBuffer e_buffer = ByteBuffer.wrap(end_time);
            e_buffer.order(ByteOrder.LITTLE_ENDIAN);
            double d_end_time = e_buffer.getDouble();
            log.debug("end time: {} ({})", d_end_time, end_time);

            byte[] num_transitions = in.readNBytes(8);
            ByteBuffer n_buffer = ByteBuffer.wrap(num_transitions);
            n_buffer.order(ByteOrder.LITTLE_ENDIAN);
            long transitions = n_buffer.getLong();
            log.debug("transitions: {} ({})", transitions, num_transitions);

            long countedTransitions = 0;
            double lastSampleTime = 0;
            // count transitions
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

}
