package de.nomagic.logic;

import java.util.HashMap;

public class BitFieldRule
{
    final boolean valid;
    final int lowestBitIndex;
    final int numBits;
    final String fieldName;
    final HashMap<Long, String> description = new HashMap<Long, String>();

    // Format is like this:
    // parts[0] : lowestBitIndex = int
    // parts[1] : numBits = int
    // parts[2] : fieldName = String
    // parts[3 + N*2] = description.key = int
    // parts[4 + N*2] = description.value = String

    public BitFieldRule(String[] parts)
    {
        if(parts.length < 3)
        {
            valid = false;
            lowestBitIndex = 0;
            numBits = 0;
            fieldName = "";
            return;
        }
        lowestBitIndex = Integer.decode(parts[0]);
        numBits = Integer.decode(parts[1]);
        fieldName = parts[2].trim();
        int numDescriptions = (parts.length -3)/2;
        for(int i = 0; i < numDescriptions; i++)
        {
            long key = Long.decode(parts[3 + i*2]);
            description.put(key, parts[4 + i*2].trim());
        }
        valid = true;
    }

    public String reportOn(long value)
    {
        // mask off other data
        value = value >> lowestBitIndex;
        value = value & ((int)Math.pow(2, numBits) -1);
        String res = description.get(value);
        if(null == res)
        {
            // no description for the value
            res = "" + value;
        }
        if(10 > value)
        {
            return String.format("0x%08x %15s (%d): %s\r\n", (value<<lowestBitIndex), fieldName, value, res);
        }
        else
        {
            return String.format("0x%08x %15s (%d | 0x%x): %s\r\n", (value<<lowestBitIndex), fieldName, value, value, res);
        }
    }

    public boolean isValid()
    {
        return valid;
    }

}
