package is.devices.internal;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import is.gui.services.DialogManagerSvc;
import is.logger.Logger;
import is.springConfig.AppConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.PortUnreachableException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import is.devices.SerialConnection;

/**
 * TwoWaySerialComm have the ability to send and receive packets using USB serial device.
 * 
 * @author taljmars
 *
 */

@ComponentScan("logger")
@ComponentScan("gui.services")
@Component
public class TwoWaySerialComm implements SerialConnection {

	private String PORT_NAME = null;// = "COM9";
	//private final static int BAUD_RATE = 57600;
	private int BAUD_RATE;// = 115200;

	private SerialPort serialPort;

	private InputStream in;
	private OutputStream out;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get dialog manager")
	private DialogManagerSvc dialogManagerSvc;
	
	@Autowired @NotNull(message = "Internal Error: Failed to get logger")
	private Logger logger;

	private static int called;
	/**
	 * Verify it is indeed a singletone 
	 */
	@PostConstruct
	private void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}

	@Override
	public void setBaud(Integer boud) {
		BAUD_RATE = boud;
	}

	@Override
	public void setPortName(String port_name) {
		PORT_NAME = port_name.substring(0, port_name.indexOf(" "));;
	}

	/**
	 * This function try to connect the default port defined. 
	 */
	public boolean connect() {
		logger.LogGeneralMessege("Radio communication manager created");

		try {
			if (PORT_NAME == null)
				return false;

			if (!connect(PORT_NAME))
				return false;
		}
		catch (NoSuchPortException e) {
			logger.LogErrorMessege("'" + PORT_NAME + "' port was not found");
		}
		catch (PortInUseException e) {
			logger.LogErrorMessege("'" + PORT_NAME + "' port is in use");
		}
		catch (Exception e) {
			logger.LogErrorMessege("Unexpected Error:");
			logger.LogErrorMessege(e.getMessage());
			dialogManagerSvc.showErrorMessageDialog("Unexpected Error", e);
		}

		logger.LogGeneralMessege("Radio communication manager started successfully");

		return true;
	}

	/**
	 * This function try to connect to a specific port 
	 * 
	 * @param portName
	 * @throws Exception
	 */
	private boolean connect( String portName ) throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier( portName );
		if( portIdentifier.isCurrentlyOwned() ) {
			logger.LogErrorMessege("Port " + portName + " is currently in use");
			throw new PortInUseException();
		}

		int timeout = 2000;
		CommPort commPort = portIdentifier.open( this.getClass().getName(), timeout );

		if( commPort instanceof SerialPort ) {
			logger.LogDesignedMessege("Going to connect to port '" + PORT_NAME + "' with baud rate '" + BAUD_RATE + "'");
			serialPort = ( SerialPort )commPort;
			serialPort.setSerialPortParams( BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE );
			in = serialPort.getInputStream();
			out = serialPort.getOutputStream();
		} 
		else {
			dialogManagerSvc.showAlertMessageDialog(getClass().getName() + " Only serial ports are handled");
			logger.LogErrorMessege("Port " + portName + " is currently in use");
			logger.LogErrorMessege("Only serial ports are handled");
			throw new PortUnreachableException("Only serial ports are handled");
		}

		return true;
	}

	@Override
	public boolean disconnect() {
		try {
			logger.LogErrorMessege("Disconnected");
			if (out != null)
				out.close();
			out = null;

			if (in != null)
				in.close();
			in = null;

			if (serialPort != null) {
				serialPort.disableReceiveFraming();
				serialPort.disableReceiveThreshold();
				serialPort.disableReceiveTimeout();
				serialPort.removeEventListener();
				serialPort.close();
			}
			serialPort = null;
			return true;
		} catch (IOException e) {
			logger.LogErrorMessege("Failed to disconnect");
			logger.LogErrorMessege(e.getMessage());
			return false;
		}
	}

	/**
	 * read byte after byte from the USB device
	 * 
	 * @param readData 	- buffer for reading data
	 * @param len		- size of the buffer
	 * @return
	 */
	public int read(byte[] readData, int len) {
		try {
			int i = 0;
			int b = '\n';

			while ((b = this.in.read()) != '\n' && b != -1) {
				if (i == len) {
					throw new Exception("Buffer Overflow");
				}
				readData[i++] = (byte) b;
			}

			return i;
		}
		catch (AccessDeniedException e) {
			logger.LogErrorMessege("Failed to access device, check connectivity");
			dialogManagerSvc.showErrorMessageDialog(getClass().getName() + " Failed to access device, check connectivity", e);
			System.exit(-1);
		}
		catch (IOException e) {
			logger.LogErrorMessege("Failed to read from device");
			logger.close();
			dialogManagerSvc.showErrorMessageDialog(getClass().getName() + " Failed to read from device", e);
			System.exit(-1);
		}
		catch (Exception e) {
			logger.LogErrorMessege("Unexpected Error:");
			logger.LogErrorMessege(e.getMessage());
			logger.close();
			dialogManagerSvc.showErrorMessageDialog(getClass().getName() + " Unexpected Error", e);
			System.exit(-1);
		}

		return -1;
	}

	/**
	 * write text byte after byte to the USB device
	 * 
	 * @param text
	 */
	public void write(String text) {
		try {

			if (text != null && this.out != null)
				this.out.write( (text + "\n").getBytes() );
		}
		catch (AccessDeniedException e) {
			logger.LogErrorMessege("Failed to access device, check connectivity");
			logger.close();
			dialogManagerSvc.showErrorMessageDialog(getClass().getName() + " Failed to access device, check connectivity", e);
			System.exit(-1);
		}
		catch (IOException e) {
			logger.LogErrorMessege("Failed to write to device");
			logger.close();
			dialogManagerSvc.showErrorMessageDialog(getClass().getName() + " Failed to write to device", e);
			System.exit(-1);
		}
		catch (Exception e) {
			logger.LogErrorMessege("Unexpected Error:");
			logger.LogErrorMessege(e.getMessage());
			logger.close();
			dialogManagerSvc.showErrorMessageDialog(getClass().getName() + " Unexpected Error", e);
			System.exit(-1);
		}
	}

	@Override
	public void write(byte[] buffer) {
		try {
			out.write(buffer);
		} catch (IOException e) {
			logger.LogErrorMessege("Failed to write messeges");
			logger.LogErrorMessege(e.getMessage());
		}
	}

	/**
	 * get available USB port with devices connected to the machine
	 * 
	 * @return String array of available ports
	 */
	@SuppressWarnings("unchecked")
	public Object[] listPorts()
	{
		Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
		ArrayList<String> ans = new ArrayList<String>();
		while ( portEnum.hasMoreElements() ) {
			CommPortIdentifier portIdentifier = portEnum.nextElement();
			ans.add(portIdentifier.getName()  +  " - " +  getPortTypeName(portIdentifier.getPortType()));
		}
		return ans.toArray();
	}

	/**
	 * get the port type name
	 * 
	 * @param portType id
	 * @return port type name
	 */
	private String getPortTypeName ( int portType )
	{
		switch ( portType )
		{
		case CommPortIdentifier.PORT_I2C:
			return "I2C";
		case CommPortIdentifier.PORT_PARALLEL:
			return "Parallel";
		case CommPortIdentifier.PORT_RAW:
			return "Raw";
		case CommPortIdentifier.PORT_RS485:
			return "RS485";
		case CommPortIdentifier.PORT_SERIAL:
			return "Serial";
		default:
			return "unknown type";
		}
	}

	@Override
	public Object[] baudList() {
		Object[] oblist = new Object[]{57600, 115200};// Arrays.asList(57600, 115200).toArray();
		return oblist;
	}

	@Override
	public Object getDefaultBaud() {
		return AppConfig.DebugMode ? 115200 : 57600;
	}
}