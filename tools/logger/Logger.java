package logger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JOptionPane;

public class Logger {
	
	private static Logger logger = null;
	private PrintWriter writer = null;
	
	private Logger() {
		Date date = new Date();
		
		try {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM_dd_hhmmss");
				String dateAsString = simpleDateFormat.format(date);
				System.out.println(dateAsString);
		
				writer = new PrintWriter(System.getProperty("user.dir") + "\\quadlog_" + dateAsString + ".html", "UTF-8");
				System.out.println(System.getProperty("user.dir") + "\\quadlog_" + dateAsString + ".html");
		} catch (FileNotFoundException e) {
				e.printStackTrace();
				System.err.println(getClass().getName() + " Failed to open log file, log will not be available");
		    	JOptionPane.showMessageDialog(null, "Failed to open log file, log will not be available");
				writer = null;
		} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(null, "Failed to open log file, due to the following error:" + e.getMessage() + "\nlog will not be available");
				writer = null;
		}
		
		Timestamp ts = new Timestamp(date.getTime());
		
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
	
	private static Logger get() {
		if (logger == null)
			logger = new Logger();
		
		return logger;
	}
	
	int recordNumber = 0;
	private void log(String str){
		if (writer == null)
			return;
		
		writer.println(str);
		recordNumber++;
	}
	
	public static void LogDesignedMessege(String msg) {
		get().log(msg);
	}
	
	public static void LogGeneralMessege(String msg) {
		System.out.println(msg);
		Date date = new Date();
		Timestamp ts = new Timestamp(date.getTime());
		
		String modmsg = "<font color=\"black\">" + "[" + ts.toString() + "] " + msg + "</font>" + "<br/>";
		get().log(modmsg);
	}
	
	public static void LogErrorMessege(String msg) {
		System.err.println(msg);
		Date date = new Date();
		Timestamp ts = new Timestamp(date.getTime());
		
		String modmsg = "<font color=\"red\">" + "[" + ts.toString() + "] " + msg + "</font>" + "<br/>";
		get().log(modmsg);
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
	
	public static void close() {
		try {
			get().finalize();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
