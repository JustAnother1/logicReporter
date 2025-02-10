package de.nomagic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Vector;

import de.nomagic.logic.SaleaDigitalChannel;
import de.nomagic.logic.SampleSource;

public class Configuration
{
    private boolean valid = true;
    // SWD:
    private SampleSource swclk = null;
    private SampleSource swdio = null;
    private int spiMode = -1;
    // SPI:
    private SampleSource clk = null;
    private SampleSource ncs = null;
    private SampleSource miso = null;
    private SampleSource mosi = null;
    private SampleSource io2 = null;
    private SampleSource io3 = null;
    private boolean reportEdges = false;
    private boolean reportBits = false;
    private boolean reportDP = false;
    private boolean reportAP = false;
    Vector<String> translationFiles = new Vector<String>();

    public Configuration()
    {
    }

    public void add_Channel(Channel name, String fileName)
    {
        switch(name)
        {
        case SWD_CLK:
            swclk = addChannelFile("SWCLK", fileName);
            break;

        case SWD_IO:
            swdio = addChannelFile("SWDIO", fileName);
            break;

        case SPI_CLK:
            clk = addChannelFile("clk", fileName);
            break;

        case SPI_MISO:
            miso = addChannelFile("miso", fileName);
            break;

        case SPI_MOSI:
            mosi = addChannelFile("mosi", fileName);
            break;

        case SPI_IO2:
            io2 = addChannelFile("io2", fileName);
            break;

        case SPI_IO3:
            io3 = addChannelFile("io3", fileName);
            break;

        case SPI_nCS:
            ncs = addChannelFile("/cs", fileName);
            break;
        }
    }

    private SampleSource addChannelFile(String ChannelName, String FileName)
    {
        try
        {
            System.out.println(ChannelName + " :");
            System.out.println("Reading " + FileName + " ...");
            File swdFile = new File(FileName);
            FileInputStream swclkFin = new FileInputStream(swdFile);
            SaleaDigitalChannel channel = new SaleaDigitalChannel(swclkFin, swdFile.length());
            if(false == channel.isValid())
            {
                System.err.println(ChannelName + " File is not valid !");
                valid = false;
            }
            else
            {
                return channel;
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            valid = false;
        }
        return null;
    }

    public boolean isValid()
    {
        if(false == valid)
        {
            // some already detected error
            return false;
        }

        if(false == isSWD())
        {
            if(false == isSPI())
            {
                System.err.println("ERROR: Configuration information is missing !");
                return false;
            }
            // else is SPI -> OK
        }
        // else is SWD -> OK

        // all checks passed
        return true;
    }

    public boolean isSWD()
    {
        if((null == swdio) || (null == swclk))
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public boolean isSPI()
    {
        if((null == clk) || (null == ncs) || (null == miso) || (null == mosi))
        {
            // providing a signal file is required.
            return false;
        }

        if((0 > spiMode) || (3< spiMode))
        {
            // invalid SPI mode
            return false;
        }
        // all checks passed
        return true;
    }

    public void setReportEdges(boolean val)
    {
        reportEdges = val;
    }

    public void setReportBits(boolean val)
    {
        reportBits = val;
    }

    public void setReportDebugPortPackets(boolean val)
    {
        reportDP = val;
    }

    public void setReportAccessPortPackets(boolean val)
    {
        reportAP = val;
    }


    public SampleSource get_channel(Channel name)
    {
        switch(name)
        {
        case SPI_CLK:
            return clk;

        case SPI_MISO:
            return miso;

        case SPI_MOSI:
            return mosi;

        case SPI_IO2:
            return io2;

        case SPI_IO3:
            return io3;

        case SPI_nCS:
            return ncs;

        case SWD_CLK:
            return swclk;

        case SWD_IO:
            return swdio;
        }
        return null;
    }

    public boolean shallReportEdgeLevel()
    {
        return reportEdges;
    }

    public boolean shallReportBitValues()
    {
        return reportBits;
    }

    public boolean shallReportDP()
    {
        return reportDP;
    }

    public boolean shallReportAP()
    {
        return reportAP;
    }

    public void addRegisterTranslationFileName(String fileName)
    {
        translationFiles.add(fileName);
    }

    public String[] getRegisterTranslationFileNames()
    {
        if(translationFiles.isEmpty())
        {
            return new String[0];
        }
        else
        {
            return translationFiles.toArray(new String[0]);
        }
    }

    public void setSpiMode(int val)
    {
        spiMode = val;
    }

    public int getSpiMode()
    {
        return spiMode;
    }

}
