package com.generic_tools.csv;

import java.util.List;

public interface CSV {

	public void addEntry(List<Object> asList);

	public void open(List<Object> list);

	public void close();

}
