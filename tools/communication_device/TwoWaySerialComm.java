package communication_device;
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

import javax.annotation.PostConstruct;
import javax.swing.JOptionPane;

import org.springframework.stereotype.Component;

import logger.Logger;

@Component("twoWaySerialComm")
public class TwoWaySerialComm {
	
	private SerialPort serialPort = null;
	
	public InputStream in = null;
    public OutputStream out = null;
    
    private static String portName = "COM9";
    //private static String portName = "COM8";
    
    static int called;
	@PostConstruct
	public void init() {
		if (called++ > 1)
			throw new RuntimeException("Not a Singletone");
	}
    
    public void connect() {
    	boolean portSelected = false;
    	
    	Logger.LogGeneralMessege("Radio communication manager created");
    	
    	while (!portSelected) {
    		try {
				connect(portName);
				Logger.LogGeneralMessege("Radio communication manager started successfully");
			}
    		catch (NoSuchPortException e) {
    			Object[] possibilities = listPorts();
    			if (possibilities.length != 0) {
	    			String s = (String)JOptionPane.showInputDialog(
	    			                    null, "Port not found, please select a different port:\n",
	    			                    "Port Selection",JOptionPane.PLAIN_MESSAGE,null,possibilities,"");
	    			if ((s != null) && (s.length() > 0)) {
	    				portName = s.substring(0, s.indexOf(" "));
	    				continue;
	    			}
    			}
    			else {
    				JOptionPane.showMessageDialog(null, " Port not found and there are no other port to use");
    			}
    			Logger.LogErrorMessege(portName + " port was not found");
    			Logger.close();
				System.exit(-1);
    		}
    		catch (PortInUseException e) {
    			Logger.LogErrorMessege(portName + " port is in use");
    			Logger.close();
    			Object[] possibilities = listPorts();
    			if (possibilities.length != 0) {
	    			String s = (String)JOptionPane.showInputDialog(
	    			                    null, "Port is in use, please select a different port:\n",
	    			                    "Port Selection",JOptionPane.PLAIN_MESSAGE,null,possibilities,"");
	    			if ((s != null) && (s.length() > 0)) {
	    				portName = s.substring(0, s.indexOf(" "));
	    				continue;
	    			}
    			}
    			else {
    				JOptionPane.showMessageDialog(null, " Port is in use and there are no other port to use");
    			}
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
 
    public void connect( String portName ) throws Exception {
        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier( portName );
        if( portIdentifier.isCurrentlyOwned() ) {
            Logger.LogErrorMessege("Port " + portName + " is currently in use");
			throw new PortInUseException();
        }
        
        int timeout = 2000;
        CommPort commPort = portIdentifier.open( this.getClass().getName(), timeout );
 
    	if( commPort instanceof SerialPort ) {
    		serialPort = ( SerialPort )commPort;
    		serialPort.setSerialPortParams( 57600,
    										//112500,
    										SerialPort.DATABITS_8,
    										SerialPort.STOPBITS_1,
    										SerialPort.PARITY_NONE );
 
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
    
    public String read() {
    	try {
	    	int BUFF_SIZE = 1024;
	        byte[] buffer = new byte[BUFF_SIZE];
	        //int len = this.in.read(buffer);
	        int i = 0;
	        int b = '\n';
	        
	        while ((b = this.in.read()) != '\n' && b != -1) {        	
	            if (i == BUFF_SIZE - 1){
	                buffer[i] = '\0';
	                System.err.println("Buffer Oversize!");
	                break;
	            }
	            buffer[i++] = (byte) b;
	        }
	        
	        if (i == 0)
	        	return "";
	        
	        //buffer[i++] = '\n';
	        buffer[i-1] = '\0';
	
	        //String str = new String( buffer, 0, i-1 ); 
	        
	    	return new String( buffer, 0, i-1 );
    	}
    	catch (AccessDeniedException e) {
    		Logger.LogErrorMessege("Failed to access device, check connectivity");
			Logger.close();
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
    	
		return null;
    }
    
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
			Logger.close();
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
 
    public void write(String msg) {
    	try {
    		
    		if (msg != null && this.out != null)
				this.out.write( (msg + "\n").getBytes() );
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
  
    @SuppressWarnings("unchecked")
	static Object[] listPorts()
    {
        java.util.Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
        ArrayList<String> ans = new ArrayList<String>();
        while ( portEnum.hasMoreElements() ) {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            ans.add(portIdentifier.getName()  +  " - " +  getPortTypeName(portIdentifier.getPortType()));
        }
        System.out.println(ans);
        return ans.toArray();
    }
    
    static String getPortTypeName ( int portType )
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