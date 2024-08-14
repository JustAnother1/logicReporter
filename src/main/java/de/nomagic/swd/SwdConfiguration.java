package de.nomagic.swd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Vector;

import de.nomagic.logic.SaleaDigitalChannel;
import de.nomagic.logic.SampleSource;

public class SwdConfiguration
{
    private boolean valid = true;
    private boolean reportEdges = false;
    private boolean reportBits = false;
    private boolean reportDP = false;
    private boolean reportAP = false;
    private SampleSource swclk = null;
    private SampleSource swdio = null;
    Vector<String> translationFiles = new Vector<String>();

    public SwdConfiguration()
    {
    }

    public void add_SWDIO(String swdioFile)
    {
        try
        {
            System.out.println("SWDIO: ");
            System.out.println("Reading " + swdioFile + " ...");
            File swdFile = new File(swdioFile);
            FileInputStream swdioFin = new FileInputStream(swdFile);
            SaleaDigitalChannel swdioChannel = new SaleaDigitalChannel(swdioFin, swdFile.length());
            if(false == swdioChannel.isValid())
            {
                System.err.println("SWDIO File is not valid !");
                valid = false;
            }
            else
            {
                swdio = swdioChannel;
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            valid = false;
        }
    }

    public void add_SWCLK(String swclkFile)
    {
        try
        {
            System.out.println("SWCLK: ");
            System.out.println("Reading " + swclkFile + " ...");
            File swdFile = new File(swclkFile);
            FileInputStream swclkFin = new FileInputStream(swdFile);
            SaleaDigitalChannel swclkChannel = new SaleaDigitalChannel(swclkFin, swdFile.length());
            if(false == swclkChannel.isValid())
            {
                System.err.println("SWCLK File is not valid !");
                valid = false;
            }
            else
            {
                swclk = swclkChannel;
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            valid = false;
        }
    }

    public boolean isValid()
    {
        if(false == valid)
        {
            // some already detected error
            return false;
        }
        if((null == swdio) || (null == swclk))
        {
            // providing a signal file is required.
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

    public SampleSource get_SWCLK()
    {
        return swclk;
    }

    public SampleSource get_SWDIO()
    {
        return swdio;
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

}
