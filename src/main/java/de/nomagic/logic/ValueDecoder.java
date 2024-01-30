package de.nomagic.logic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueDecoder
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private HashMap<Long,String> longNames = new HashMap<Long,String>();
    private HashMap<Long,String> shortNames = new HashMap<Long,String>();

    public ValueDecoder()
    {
    }

    public String getShortNameFor(long value)
    {
        String res = shortNames.get(value);
        if(null == res)
        {
            return "";
        }
        else
        {
            return res;
        }
    }

    public String getLongNameFor(long value)
    {
        String res = longNames.get(value);
        if(null == res)
        {
            return "";
        }
        else
        {
            return res;
        }
    }

    private long decode(String value)
    {
        long res = Long.decode(value);
        return res;
    }

    public void readTransationsFrom(String filename)
    {
        int num = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null)
            {
                String[] parts = line.split(",");
                if(2 == parts.length)
                {
                    long val = decode(parts[0]);
                    shortNames.put(val, parts[1]);
                    num ++;
                }
                else if(3 == parts.length)
                {
                    long val = decode(parts[0]);
                    shortNames.put(val, parts[1]);
                    longNames.put(val, parts[2]);
                    num++;
                }
                else
                {
                    // ignore this line
                }

                line = reader.readLine();
            }
            reader.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        log.trace("read {} translations from {}", num, filename);
    }
}
