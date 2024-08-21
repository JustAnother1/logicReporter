package de.nomagic.logic;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValueDecoder
{
    private final Logger log = LoggerFactory.getLogger(this.getClass().getName());

    private HashMap<Long,String> longNames = new HashMap<Long,String>();
    private HashMap<Long,String> shortNames = new HashMap<Long,String>();

    private HashMap<Long, ArrayList<BitFieldRule>> readBitRules = new HashMap<Long,ArrayList<BitFieldRule>>();
    private HashMap<Long,ArrayList<BitFieldRule>> writeBitRules = new HashMap<Long,ArrayList<BitFieldRule>>();

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
            return getShortNameFor(value);
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
        long registerAddress = 0;
        int num = 0;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while (line != null)
            {
                if(line.contains("#"))
                {
                    // remove comments
                    String[] res = line.split("#");
                    line = res[0];
                }
                if(line.startsWith("0x"))
                {
                    String[] parts = line.split(",");
                    if(2 == parts.length)
                    {
                        registerAddress = decode(parts[0]);
                        shortNames.put(registerAddress, parts[1]);
                        num ++;
                    }
                    else if(3 == parts.length)
                    {
                        registerAddress= decode(parts[0]);
                        shortNames.put(registerAddress, parts[1]);
                        longNames.put(registerAddress, parts[2]);
                        num++;
                    }
                    else
                    {
                        // ignore this line
                    }
                }
                else if(line.startsWith("R,"))
                {
                    // read
                    String[] parts = line.substring(2).split(",");
                    BitFieldRule aRule = new BitFieldRule(parts);
                    if(aRule.isValid())
                    {
                        ArrayList<BitFieldRule> rules = readBitRules.get(registerAddress);
                        if(null == rules)
                        {
                            rules = new ArrayList<BitFieldRule>();
                        }
                        rules.add(aRule);
                        readBitRules.put(registerAddress, rules);
                    }
                }
                else if(line.startsWith("W,"))
                {
                    // write
                    String[] parts = line.substring(2).split(",");
                    BitFieldRule aRule = new BitFieldRule(parts);
                    if(aRule.isValid())
                    {
                        ArrayList<BitFieldRule> rules = writeBitRules.get(registerAddress);
                        if(null == rules)
                        {
                            rules = new ArrayList<BitFieldRule>();
                        }
                        rules.add(aRule);
                        writeBitRules.put(registerAddress, rules);
                    }
                }
                else if(line.startsWith("B,"))
                {
                    // read and write
                    String[] parts = line.substring(2).split(",");
                    BitFieldRule aRule = new BitFieldRule(parts);
                    if(aRule.isValid())
                    {
                        // read
                        ArrayList<BitFieldRule> rules = readBitRules.get(registerAddress);
                        if(null == rules)
                        {
                            rules = new ArrayList<BitFieldRule>();
                        }
                        rules.add(aRule);
                        readBitRules.put(registerAddress, rules);
                        // write
                        rules = writeBitRules.get(registerAddress);
                        if(null == rules)
                        {
                            rules = new ArrayList<BitFieldRule>();
                        }
                        rules.add(aRule);
                        writeBitRules.put(registerAddress, rules);
                    }
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

    public String parseData(Direction dir, long address, long value)
    {
        if(Direction.READ == dir)
        {
            ArrayList<BitFieldRule> rules = readBitRules.get(address);
            if(null != rules)
            {
                StringBuffer sb = new StringBuffer();
                for(int i = 0; i < rules.size(); i++)
                {
                    BitFieldRule curRule = rules.get(i);
                    String res = curRule.reportOn(value);
                    sb.append(res);
                }
                return sb.toString();
            }
            else
            {
                return "";
            }
        }
        else if(Direction.WRITE == dir)
        {
            ArrayList<BitFieldRule> rules = writeBitRules.get(address);
            if(null != rules)
            {
                StringBuffer sb = new StringBuffer();
                for(int i = 0; i < rules.size(); i++)
                {
                    BitFieldRule curRule = rules.get(i);
                    String res = curRule.reportOn(value);
                    sb.append(res);
                }
                return sb.toString();
            }
            else
            {
                return "";
            }
        }
        else
        {
            return "ERROR: invalid direction !";
        }
    }
}
