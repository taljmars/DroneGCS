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
}
