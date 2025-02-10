package de.nomagic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import de.nomagic.spi.SpiReporter;
import de.nomagic.swd.SwdReporter;

public class ReporterMain
{
    private Configuration cfg = new Configuration();

    public ReporterMain(String[] args)
    {
        startLogging(args);
    }

    private void startLogging(final String[] args)
    {
        int numOfV = 0;
        for(int i = 0; i < args.length; i++)
        {
            if(true == "-v".equals(args[i]))
            {
                numOfV ++;
            }
        }

        // configure Logging
        switch(numOfV)
        {
        case 0: setLogLevel("warn"); break;
        case 1: setLogLevel("debug");break;
        case 2:
        default:
            setLogLevel("trace");
            System.out.println("Build from " + getCommitID());
            break;
        }
    }

    private void setLogLevel(String LogLevel)
    {
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        try
        {
            final JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(context);
            context.reset();
            final String logCfg =
            "<configuration>" +
              "<appender name='STDOUT' class='ch.qos.logback.core.ConsoleAppender'>" +
                "<encoder>" +
                  "<pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>" +
                "</encoder>" +
              "</appender>" +
              "<root level='" + LogLevel + "'>" +
                "<appender-ref ref='STDOUT' />" +
              "</root>" +
            "</configuration>";
            ByteArrayInputStream bin;
            try
            {
                bin = new ByteArrayInputStream(logCfg.getBytes("UTF-8"));
                configurator.doConfigure(bin);
            }
            catch(UnsupportedEncodingException e)
            {
                // A system without UTF-8 ? - No chance to do anything !
                e.printStackTrace();
                System.exit(1);
            }
        }
        catch (JoranException je)
        {
          // StatusPrinter will handle this
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(context);
    }

    public static String getCommitID()
    {
        try
        {
            final InputStream s = ReporterMain.class.getResourceAsStream("/git.properties");
            final BufferedReader in = new BufferedReader(new InputStreamReader(s));

            String commitId = "";
            StringBuilder comment = new StringBuilder();
            String line = in.readLine();
            while(null != line)
            {
                if(true == line.startsWith("git.commit.id.full="))
                {
                    commitId = line.substring(line.indexOf('=') + 1);
                }
                else if(true == line.startsWith("git.dirty=true"))
                {
                    comment.append(" dirty ");
                }
                else if(true == line.startsWith("git.build.time="))
                {
                    comment.append(" " + line.substring(line.indexOf('=') + 1));
                }
                line = in.readLine();
            }
            return commitId + comment.toString();
        }
        catch( Exception e )
        {
            return e.toString();
        }
    }

    public void printHelp()
    {
        System.out.println("Logic Reporter");
        System.out.println("Parameters:");
        System.out.println("-h                           : print this message.");

        System.out.println("  SWD:");
        System.out.println("-swclk <logic analyzer file> : SWCLK channel file that will be reported on (required))");
        System.out.println("-swdio <logic analyzer file> : SWDIO channel file that will be reported on (required))");
        System.out.println("-regtrans <file name>        : add a register translation file");
        System.out.println("-report_edges                : report all detected edges");
        System.out.println("-report_bits                 : report all detected bits");
        System.out.println("-report_DP                   : report all Debug Port packets");
        System.out.println("-report_AP                   : report all Access Port packets");

        System.out.println("  SPI:");
        System.out.println("-clk <logic analyzer file>   : CLK channel file that will be reported on (required)");
        System.out.println("-ncs <logic analyzer file>   : /CS channel file that will be reported on (required)");
        System.out.println("-mosi <logic analyzer file>  : MOSI(IO 0) channel file that will be reported on (required)");
        System.out.println("-miso <logic analyzer file>  : MISO(IO 1) channel file that will be reported on (required)");
        System.out.println("-io2 <logic analyzer file>   : IO 2 channel file that will be reported on (QSPI only)");
        System.out.println("-io3 <logic analyzer file>   : IO 3 channel file that will be reported on (QSPI only)");
        System.out.println("-mode <num>                  : set the SPI mode");
        System.out.println("         mode CPOL CPHA write bit                             read bit");
        System.out.println("         0    0    0    falling clock, and when /CS activates rising clock");
        System.out.println("         1    0    1    rising clock                          falling clock");
        System.out.println("         2    1    0    rising clock, and when /CS activates  falling clock");
        System.out.println("         3    1    1    falling clock                         rising clock ");

        System.out.println("-v                           : verbose output for even more messages use -v -v");
    }

    public boolean parseCommandLineParameters(final String[] args)
    {
        for(int i = 0; i < args.length; i++)
        {
            if(true == args[i].startsWith("-"))
            {
                if(true == "-h".equals(args[i]))
                {
                    return false;
                }
                else if(true == "-swclk".equals(args[i]))
                {
                    i++;
                    cfg.add_Channel(Channel.SWD_CLK, args[i]);
                }
                else if(true == "-swdio".equals(args[i]))
                {
                    i++;
                    cfg.add_Channel(Channel.SWD_IO, args[i]);
                }
                else if(true == "-clk".equals(args[i]))
                {
                    i++;
                    cfg.add_Channel(Channel.SPI_CLK, args[i]);
                }
                else if(true == "-ncs".equals(args[i]))
                {
                    i++;
                    cfg.add_Channel(Channel.SPI_nCS, args[i]);
                }
                else if(true == "-miso".equals(args[i]))
                {
                    i++;
                    cfg.add_Channel(Channel.SPI_MISO, args[i]);
                }
                else if(true == "-mosi".equals(args[i]))
                {
                    i++;
                    cfg.add_Channel(Channel.SPI_MOSI, args[i]);
                }
                else if(true == "-io2".equals(args[i]))
                {
                    i++;
                    cfg.add_Channel(Channel.SPI_IO2, args[i]);
                }
                else if(true == "-io3".equals(args[i]))
                {
                    i++;
                    cfg.add_Channel(Channel.SPI_IO3, args[i]);
                }
                else if(true == "-regtrans".equals(args[i]))
                {
                    i++;
                    cfg.addRegisterTranslationFileName(args[i]);
                }
                else if(true == "-report_edges".equals(args[i]))
                {
                    cfg.setReportEdges(true);
                }
                else if(true == "-report_bits".equals(args[i]))
                {
                    cfg.setReportBits(true);
                }
                else if(true == "-report_DP".equals(args[i]))
                {
                    cfg.setReportDebugPortPackets(true);
                }
                else if(true == "-report_AP".equals(args[i]))
                {
                    cfg.setReportAccessPortPackets(true);
                }
                else if(true == "-mode".equals(args[i]))
                {
                    i++;
                    int val = Integer.parseInt(args[i]);
                    cfg.setSpiMode(val);
                }
                else if(true == "-v".equals(args[i]))
                {
                    // already handled -> ignore
                }
                else
                {
                    System.err.println("Invalid Parameter : " + args[i]);
                    return false;
                }
            }
            else
            {
                System.err.println("Invalid Parameter : " + args[i]);
                return false;
            }
        }
        return cfg.isValid();
    }

    private boolean processFile()
    {
        try {
            if(cfg.isSWD())
            {
                SwdReporter rep = new SwdReporter(cfg);
                return rep.reportTo(System.out);
            }
            else if(cfg.isSPI())
            {
                SpiReporter rep = new SpiReporter(cfg);
                return rep.reportTo(System.out);
            }
            else
            {
                System.out.println("No protocol to analyze!");
                return false;
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args)
    {
        ReporterMain rm = new ReporterMain(args);
        if(false == rm.parseCommandLineParameters(args))
        {
            rm.printHelp();
            return;
        }
        else
        {
            if(true == rm.processFile())
            {
                System.out.println("Done!");
            }
            else
            {
                System.out.println("Failed!");
            }
        }
    }

}
