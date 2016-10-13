package tools.antenna_device;
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

import tools.logger.Logger;

/**
 * TwoWaySerialComm have the ability to send and receive packets using USB serial device.
 * 
 * @author taljmars
 *
 */
@Component("twoWaySerialComm")
public class TwoWaySerialComm {
	
	private static String PORT_NAME = "COM9";
	private final static int BAUD_RATE = 57600;
	//private final static int BAUD_RATE = 112500;
	
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
    
    /**
     * This function try to connect the default port defined. 
     */
    public void connect() {
    	boolean portSelected = false;
    	
    	Logger.LogGeneralMessege("Radio communication manager created");
    	
    	while (!portSelected) {
    		try {
				connect(PORT_NAME);
				Logger.LogGeneralMessege("Radio communication manager started successfully");
			}
    		catch (NoSuchPortException e) {
    			Logger.LogErrorMessege(PORT_NAME + " port was not found");
    			Object[] possibilities = listPorts();
    			if (possibilities.length != 0) {
	    			String s = (String)JOptionPane.showInputDialog(
	    			                    null, "Port not found, please select a different port:\n",
	    			                    "Port Selection",JOptionPane.PLAIN_MESSAGE,null,possibilities,"");
	    			if ((s != null) && (s.length() > 0)) {
	    				PORT_NAME = s.substring(0, s.indexOf(" "));
	    				continue;
	    			}
    			}
    			else {
    				JOptionPane.showMessageDialog(null, " Port not found and there are no other port to use");
    			}
    			Logger.close();
				System.exit(-1);
    		}
    		catch (PortInUseException e) {
    			Logger.LogErrorMessege(PORT_NAME + " port is in use");
    			Object[] possibilities = listPorts();
    			if (possibilities.length != 0) {
	    			String s = (String)JOptionPane.showInputDialog(
	    			                    null, "Port is in use, please select a different port:\n",
	    			                    "Port Selection",JOptionPane.PLAIN_MESSAGE,null,possibilities,"");
	    			if ((s != null) && (s.length() > 0)) {
	    				PORT_NAME = s.substring(0, s.indexOf(" "));
	    				continue;
	    			}
    			}
    			else {
    				JOptionPane.showMessageDialog(null, " Port is in use and there are no other port to use");
    			}
    			Logger.close();
				System.exit(-1);
    		}
    		catch (Exception e) {
    			Logger.LogErrorMessege("Unexpected Error:\n");
    			Logger.LogErrorMessege(e.getMessage());
    			Logger.close();
    			JOptionPane.showMessageDialog(null, "Unexpected Error:\n" + e.getMessage());
				System.exit(-1);
			}
	    	
	    	portSelected = true;
    	}
    }
 
    /**
     * This function try to connect to a specific port 
     * 
     * @param portName
     * @throws Exception
     */
    private void connect( String portName ) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier( portName );
        if( portIdentifier.isCurrentlyOwned() ) {
            Logger.LogErrorMessege("Port " + portName + " is currently in use");
			throw new PortInUseException();
        }
        
        int timeout = 2000;
        CommPort commPort = portIdentifier.open( this.getClass().getName(), timeout );
 
    	if( commPort instanceof SerialPort ) {
    		serialPort = ( SerialPort )commPort;
    		serialPort.setSerialPortParams( BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE );
    		in = serialPort.getInputStream();
    		out = serialPort.getOutputStream(); 
    	} 
    	else {
			JOptionPane.showMessageDialog(null, getClass().getName() + " Only serial ports are handled");
			Logger.LogErrorMessege("Port " + portName + " is currently in use");
    		Logger.LogErrorMessege("Only serial ports are handled");
			Logger.close();
			throw new PortUnreachableException("Only serial ports are handled");
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
  
    /**
     * get available USB port with devices connected to the machine
     * 
     * @return String array of available ports
     */
    @SuppressWarnings("unchecked")
	private String[] listPorts()
    {
        Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        ArrayList<String> ans = new ArrayList<String>();
        while ( portEnum.hasMoreElements() ) {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            ans.add(portIdentifier.getName()  +  " - " +  getPortTypeName(portIdentifier.getPortType()));
        }
        return (String[]) ans.toArray();
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

	/**
	 * @return output stream of the USB device
	 */
	public OutputStream getOutputStream() {
		return out;
	}
}