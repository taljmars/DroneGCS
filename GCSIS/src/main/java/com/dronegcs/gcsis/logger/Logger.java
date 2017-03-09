package logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Date;
import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import environment.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@ComponentScan(basePackages = "environment")
@Component
public class Logger {
	
	private final String LOG_ENTRY_SUFFIX = ".html";
	
	private PrintWriter writer = null;

	@Autowired @NotNull(message = "Internal Error: Failed to get environment")
	private Environment environment;

	private static int called;
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singleton");

		try {
			File logDir = Environment.getRunningEnvLogDirectory();
			writer = new PrintWriter(logDir + Environment.DIR_SEPERATOR + "log" + LOG_ENTRY_SUFFIX, "UTF-8");
			System.out.println(logDir + Environment.DIR_SEPERATOR + "log" + LOG_ENTRY_SUFFIX);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(getClass().getName() + " Failed to open log file, log will not be available");
			writer = null;
			return;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			writer = null;
			return;
		} catch (URISyntaxException e) {
			e.printStackTrace();
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

	

	int recordNumber = 0;
	private void log(String str){
		if (writer == null)
			return;
		
		writer.println(str);
		recordNumber++;
	}
	
	public void LogDesignedMessege(String msg) {
		log(msg);
	}
	
	public void LogGeneralMessege(String msg) {
		System.out.println(msg);
		Date date = new Date();
		Timestamp ts = new Timestamp(date.getTime());
		
		String modmsg = "<font color=\"black\">" + "[" + ts.toString() + "] " + msg + "</font>" + "<br/>";
		log(modmsg);
	}
	
	public void LogErrorMessege(String msg) {
		System.err.println(msg);
		Date date = new Date();
		Timestamp ts = new Timestamp(date.getTime());
		
		String modmsg = "<font color=\"red\">" + "[" + ts.toString() + "] " + msg + "</font>" + "<br/>";
		log(modmsg);
	}
	
	protected void finalize() throws Throwable {
		try {
			System.out.println("Finalize of Sub Class");
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
		System.err.println(message);
    }

	public void LogOutgoingMessage(String message) {
		System.err.println(message);
	}

	public void LogAlertMessage(String message) {
		System.err.println(message);

	}

	public void LogAlertMessage(String message, Exception e) {
		System.err.println(message);

	}

	public static enum Type {
		GENERAL,
		ERROR,
		INCOMING,
		OUTGOING
	};
	
	public static String generateDesignedMessege(String cmd, Type t, boolean no_date)
	{
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
		for (int i = 0 ; i < lines.length ; i++ ){
			if (lines[i].length() == 0)
				continue;

			switch (t) {
				case GENERAL:
					newcontent = ("<font color=\"black\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				case OUTGOING:
					newcontent = ("<font color=\"blue\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				case INCOMING:
					newcontent = ("<font color=\"green\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				case ERROR:
					newcontent = ("<font color=\"red\">" + ts_string + " " + lines[i] + "</font>" + "<br/>");
					break;
				default:
					newcontent = ("<font color=\"red\">" + ts_string + " Unrecognized: " + lines[i] + "</font>" + "<br/>");
					break;
			}
		}
		
		return newcontent;
	}
}
