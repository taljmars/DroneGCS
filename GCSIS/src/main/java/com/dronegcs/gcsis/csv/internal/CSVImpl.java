package csv.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import csv.CSV;

public class CSVImpl implements CSV {
	
	private final String CSV_SEPERATOR = ",";	
	private final String fileName;
	private PrintWriter writer = null;

	public CSVImpl(String file_full_path) {
		fileName = file_full_path;
	}

	@Override
	public void addEntry(List<Object> entryValue) {
		for (int i = 0 ; i < entryValue.size() - 1 ; i++) {
			writer.print(entryValue.get(i).toString() + CSV_SEPERATOR);
		}
		
		writer.println(entryValue.get(entryValue.size() - 1));
	}
	
	@Override
	public void close() {
		if (writer != null) {
			System.out.println("Closing CSV file '" + fileName + "'");
			writer.close();
		}
	}

	@Override
	public void open(List<Object> list) {
		try {				
			File csvFile = new File(fileName);
			File csvDir = csvFile.getParentFile();
			if (!csvDir.exists())
				csvDir.mkdir();
		
			writer = new PrintWriter(csvFile, "UTF-8");
			addEntry(list);
			System.out.println("New CSV file '" + csvFile + "'");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println(getClass().getName() + " Failed to open log file, log will not be available");
			writer = null;
			return;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			writer = null;
			return;
		}
	}

}
