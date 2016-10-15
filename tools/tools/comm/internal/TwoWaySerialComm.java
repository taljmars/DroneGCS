package tools.comm.internal;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.PortUnreachableException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.annotation.PostConstruct;
import javax.swing.JOptionPane;

import org.springframework.stereotype.Component;

import tools.comm.SerialConnection;
import tools.logger.Logger;

/**
 * TwoWaySerialComm have the ability to send and receive packets using USB serial device.
 * 
 * @author taljmars
 *
 */

@Component("twoWaySerialComm")
public class TwoWaySerialComm implements SerialConnection {
	
	private String PORT_NAME = null;// = "COM9";
	//private final static int BAUD_RATE = 57600;
	private int BAUD_RATE;// = 115200;
	
	private SerialPort serialPort;
	
	private InputStream in;
	private OutputStream out;
    
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
    	Logger.LogGeneralMessege("Radio communication manager created");
    	
		try {
			if (PORT_NAME == null)
				return false;
			
			if (!connect(PORT_NAME))
				return false;
		}
		catch (NoSuchPortException e) {
			Logger.LogErrorMessege("'" + PORT_NAME + "' port was not found");
		}
		catch (PortInUseException e) {
			Logger.LogErrorMessege("'" + PORT_NAME + "' port is in use");
		}
		catch (Exception e) {
			Logger.LogErrorMessege("Unexpected Error:");
			Logger.LogErrorMessege(e.getMessage());
			JOptionPane.showMessageDialog(null, "Unexpected Error:\n" + e.getMessage());
		}
		
		Logger.LogGeneralMessege("Radio communication manager started successfully");
    	
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
            Logger.LogErrorMessege("Port " + portName + " is currently in use");
			throw new PortInUseException();
        }
        
        int timeout = 2000;
        CommPort commPort = portIdentifier.open( this.getClass().getName(), timeout );
 
    	if( commPort instanceof SerialPort ) {
    		Logger.LogDesignedMessege("Going to connect to port '" + PORT_NAME + "' with baud rate '" + BAUD_RATE + "'");
    		serialPort = ( SerialPort )commPort;
    		serialPort.setSerialPortParams( BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE );
    		in = serialPort.getInputStream();
    		out = serialPort.getOutputStream();
    	} 
    	else {
			JOptionPane.showMessageDialog(null, getClass().getName() + " Only serial ports are handled");
			Logger.LogErrorMessege("Port " + portName + " is currently in use");
    		Logger.LogErrorMessege("Only serial ports are handled");
			throw new PortUnreachableException("Only serial ports are handled");
    	}
    	
    	return true;
    }
    
	@Override
	public boolean disconnect() {
		try {
			Logger.LogErrorMessege("Disconnected");
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
			Logger.LogErrorMessege("Failed to disconnect");
			Logger.LogErrorMessege(e.getMessage());
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
    		Logger.LogErrorMessege("Failed to access device, check connectivity");
			JOptionPane.showMessageDialog(null, getClass().getName() + " Failed to access device, check connectivity");
			System.exit(-1);
    	}
    	catch (IOException e) {
    		Logger.LogErrorMessege("Failed to read from device");
			Logger.close();
			JOptionPane.showMessageDialog(null, getClass().getName() + " Failed to read from device");
    		System.exit(-1);
    	}
    	catch (Exception e) {
    		Logger.LogErrorMessege("Unexpected Error:");
    		Logger.LogErrorMessege(e.getMessage());
			Logger.close();
			JOptionPane.showMessageDialog(null, getClass().getName() + " Unexpected Error:\n" + e.getMessage());
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
    		Logger.LogErrorMessege("Failed to access device, check connectivity");
    		Logger.close();
    		JOptionPane.showMessageDialog(null, getClass().getName() + " Failed to access device, check connectivity");
			System.exit(-1);
    	}
    	catch (IOException e) {
    		Logger.LogErrorMessege("Failed to write to device");
			Logger.close();
			JOptionPane.showMessageDialog(null, getClass().getName() + " Failed to write to device");
    		System.exit(-1);
    	}
    	catch (Exception e) {
    		Logger.LogErrorMessege("Unexpected Error:");
    		Logger.LogErrorMessege(e.getMessage());
			Logger.close();
			JOptionPane.showMessageDialog(null, getClass().getName() + " Unexpected Error:\n" + e.getMessage());
    		System.exit(-1);
    	}
    }
    
	@Override
	public void write(byte[] buffer) {
		try {
			out.write(buffer);
		} catch (IOException e) {
			Logger.LogErrorMessege("Failed to write messeges");
			Logger.LogErrorMessege(e.getMessage());
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
}