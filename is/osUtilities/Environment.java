package osUtilities;

import java.io.File;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Environment {
	
	private static final String LOG_MAIN_DIRECTORY = "Logs";
	private static final String LOG_ENTRY_PREFIX = "quadlog_";
	
	private static final String CACHE_MAIN_DIRECTORY = "Cache";
	
	public static final String DIR_SEPERATOR = "//";
	
	private static Date dateTimestemp = new Date();
	
	public static File getRunningEnvDirectory() throws URISyntaxException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM_dd_hhmmss");
		String dateAsString = simpleDateFormat.format(dateTimestemp);
		File file = new File(getBaseEnvDirectory() + DIR_SEPERATOR + LOG_ENTRY_PREFIX + dateAsString);
		if (!file.exists())
			file.mkdirs();
		
		return file;
	}
	
	private static File getBaseEnvDirectory() throws URISyntaxException {
		File file = new File(Environment.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		file = new File(file.getParent().toString() + DIR_SEPERATOR + LOG_MAIN_DIRECTORY);
		if (!file.exists())
			file.mkdirs();
		
		return file;
	}

	public static File getRunningEnvCacheDirectory() throws URISyntaxException {
		File file = new File(Environment.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		file = new File(file.getParent().toString() + DIR_SEPERATOR + CACHE_MAIN_DIRECTORY);
		if (!file.exists())
			file.mkdirs();
		
		return file;
	}
	
	public static File getRunningEnvBaseDirectory() throws URISyntaxException {
		File file = new File(Environment.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		file = new File(file.getParent().toString() + DIR_SEPERATOR);
		if (!file.exists())
			throw new RuntimeException("Running directory wasn't found");
		
		return file;
	}
}
