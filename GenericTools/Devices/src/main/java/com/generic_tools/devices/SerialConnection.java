package com.generic_tools.devices;

public interface SerialConnection {
	
	public boolean connect();
	
	public boolean disconnect();
	
	public Object[] listPorts();

	public void write(String val);

	public void write(byte[] buffer);

	public int read(byte[] readData, int length);

	public void setPortName(String port_name);

	public void setBaud(Integer baud);

	public Object[] baudList();

	public Object getDefaultBaud();
}
