package com.os_utilities;

import java.io.File;
import java.net.URISyntaxException;

public class Environment {

	private static final String CACHE_MAIN_DIRECTORY = "cache";
	
	public static final String DIR_SEPERATOR = "//";

	private static String externalBaseDirectory;

	public static File getRunningEnvCacheDirectory() throws URISyntaxException {
		
		File file = getRunningEnvBaseDirectory();
		file = new File(file.toString() + DIR_SEPERATOR + CACHE_MAIN_DIRECTORY);
		if (!file.exists()) {
			System.err.println("Creating cache directory '" + file.toString() + "'");
			file.mkdirs();
		}
		
		return file;
	}
	
	public static File getRunningEnvBaseDirectory() throws URISyntaxException {
		if (externalBaseDirectory != null && !externalBaseDirectory.isEmpty())
			return new File(externalBaseDirectory);
		String property = "java.io.tmpdir";
		String externalBaseDirectory = System.getProperty(property);
		File file = new File(externalBaseDirectory);
		if (!file.exists())
			throw new RuntimeException("Running directory wasn't found");
		
		return file;
	}
}
