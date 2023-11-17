package de.nomagic;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

public class ReporterMain
{

    private String signalFile = null;

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
        System.out.println("Printer Controller for Pacemaker");
        System.out.println("Parameters:");
        System.out.println("-h                         : print this message.");
        System.out.println("-s <logic analyzer file>   : file that will be reported on (required))");
        System.out.println("-v                         : verbose output for even more messages use -v -v");
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
                else if(true == "-s".equals(args[i]))
                {
                    i++;
                    signalFile = args[i];
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
                return false;
            }
        }
        if(null == signalFile)
        {
            // providing a signal file is required.
            return false;
        }
        // OK
        return true;
    }

    private boolean processFile()
    {
        System.out.println("Reading " + signalFile + " ...");
        SaleaeSalFile dataSource = new SaleaeSalFile(signalFile);
        if(false == dataSource.isValid())
        {
            System.err.println("File is not valid !");
            return false;
        }

        return true;
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
