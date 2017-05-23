package com.generic_tools.logger;

import com.generic_tools.environment.Environment;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.Date;

public class Logger {
    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Logger.class);

    private final String LOG_ENTRY_SUFFIX = ".html";
    private PrintWriter writer = null;
    private Environment environment;
    private int recordNumber = 0;

    public Logger(Environment environment) {
        this.environment = environment;
        init();
    }

    private static int called;

    private void init() {
        if (called++ > 1)
            throw new RuntimeException("Not a Singleton");

        try {
            //File logDir = environment.getRunningEnvLogDirectory();
            File logDir = new File(System.getProperty("user.dir"));
            String logFile = logDir + File.separator + "log" + LOG_ENTRY_SUFFIX;
            writer = new PrintWriter(logFile, "UTF-8");
            LOGGER.debug("Create log file {}", logFile);
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            LOGGER.error("Failed to open log file, log will not be available", getClass().getName(), e);
            writer = null;
            return;
        }

        Timestamp ts = new Timestamp(new Date().getTime());

        writer.println("<html>");
        writer.println("<title>");
        writer.println("QuadCopter Logbook");
        writer.println("</title>");
        writer.println("<body>");
        writer.println("<h1>QuadCopter Flight Record - " + ts.toString() + "</h1>");
        writer.println("<h3>Legend:</h3>");
        writer.println("<font color=\"black\">Black\t - General messeges</font><br/>");
        writer.println("<font color=\"green\">Green\t - Quadcopter messeges</font><br/>");
        writer.println("<font color=\"blue\">Blue\t  - GroundStation messeges</font><br/>");
        writer.println("<h1></h1>");
        writer.println("<h3>Log:</h3>");
    }

    private void log(String str) {
        if (writer == null)
            return;

        writer.println(str);
        recordNumber++;
    }

    public void LogDesignedMessege(String msg) {
        log(msg);
    }

    public void LogGeneralMessege(String msg) {
        Date date = new Date();
        Timestamp ts = new Timestamp(date.getTime());

        String modmsg = "<font color=\"black\">" + "[" + ts.toString() + "] " + msg + "</font>" + "<br/>";
        log(modmsg);
    }

    public void LogErrorMessege(String msg) {
        LOGGER.error(msg);
        Date date = new Date();
        Timestamp ts = new Timestamp(date.getTime());

        String modmsg = "<font color=\"red\">" + "[" + ts.toString() + "] " + msg + "</font>" + "<br/>";
        log(modmsg);
    }

    protected void finalize() throws Throwable {
        try {
            LOGGER.info("Finalize of Sub Class");
            if (writer != null) {
                writer.println("<h4>Total - " + recordNumber + " Records</h4>");
                writer.println("</body>");
                writer.println("</html>");
                writer.close();
            }
        } finally {
            super.finalize();
        }
    }

    public void close() {
        try {
            finalize();
        } catch (Throwable e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void LogIncomingMessage(String message) {
        LOGGER.info("Incoming : {}", message);
    }

    public void LogOutgoingMessage(String message) {
        LOGGER.info("Outgoing : {}", message);
    }

    public void LogAlertMessage(String message) {
        LOGGER.info("Alert : {}", message);
    }

    public void LogAlertMessage(String message, Exception e) {
        LOGGER.info("Alert : {}", message, e);
    }

    public enum Type {
        GENERAL,
        ERROR,
        INCOMING,
        OUTGOING
    }

    public static String generateDesignedMessege(String cmd, Type t, boolean no_date) {
        String newcontent = "";

        String ts_string = "";
        if (!no_date) {
            Date date = new Date();
            Timestamp ts = new Timestamp(date.getTime());
            ts_string = "[" + ts.toString() + "]";
        }
        /*
         * Currently i am converting NL char to space and comma sep.
		 */
        cmd = cmd.replace("\n", ",");
        cmd = cmd.trim();
        String[] lines = cmd.split("\n");
        for (String line : lines) {
            if (line.length() == 0)
                continue;
            switch (t) {
                case GENERAL:
                    newcontent = ("<font color=\"black\">" + ts_string + " " + line + "</font>" + "<br/>");
                    break;
                case OUTGOING:
                    newcontent = ("<font color=\"blue\">" + ts_string + " " + line + "</font>" + "<br/>");
                    break;
                case INCOMING:
                    newcontent = ("<font color=\"green\">" + ts_string + " " + line + "</font>" + "<br/>");
                    break;
                case ERROR:
                    newcontent = ("<font color=\"red\">" + ts_string + " " + line + "</font>" + "<br/>");
                    break;
                default:
                    newcontent = ("<font color=\"red\">" + ts_string + " Unrecognized: " + line + "</font>" + "<br/>");
                    break;
            }
        }

        return newcontent;
    }
}
