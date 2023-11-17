package de.nomagic;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaleaeSalFile
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());
    private HashMap<Integer, SaleaDigitalChannel> digitalChannels = new HashMap<Integer, SaleaDigitalChannel>();

    private boolean valid;
    private ZipFile zip = null;

    public SaleaeSalFile(String fileName)
    {
        if(null == fileName)
        {
            valid = false;
        }

        try
        {
            zip = new ZipFile(fileName);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements())
            {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if(true == name.startsWith("analog-"))
                {
                    log.info("ignoring analogue channel file " + name  + " !");
                }
                else if(true == name.startsWith("digital-"))
                {
                    log.debug("adding digital channel file " + name + " !");
                    // filename is something like: "digital-4.bin"
                    String number = name.substring(name.indexOf('-') + 1, name.length() - 4);
                    log.debug("number : {}", number);
                    Integer ChannelNumber = Integer.decode(number);
                    InputStream in = zip.getInputStream(entry);
                    SaleaDigitalChannel channel = new SaleaDigitalChannel(in);
                    if(true == channel.isValid())
                    {
                        digitalChannels.put(ChannelNumber, channel);
                        log.info("found channel {}", ChannelNumber);
                    }
                    else
                    {
                        log.error("found invalid channel {}", ChannelNumber);
                    }
                }
                else if(true == name.startsWith("meta.json"))
                {
                    log.info("ignoring meta file " + name + " !");
                }
                else
                {
                    log.warn("Unknown file " + name + " found !");
                }
            }
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

    public void close()
    {
        if(null != zip)
        {
            try
            {
                zip.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            zip = null;
        }
    }

}
