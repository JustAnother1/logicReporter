package de.nomagic;

import java.io.IOException;
import java.io.InputStream;

public class SaleaDigitalChannel
{

    private boolean valid;

    public SaleaDigitalChannel(InputStream in)
    {
        if(null == in)
        {
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
                valid = false;
                return;
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

}
