package flashsystem.io;

import flashsystem.S1Packet;
import flashsystem.X10FlashException;
import java.io.IOException;

import org.system.Device;

import win32lib.DeviceCHangedListener;

import linuxlib.JUsb;

public class USBFlashLinux {
	
	private static int lastflags;
	private static byte[] lastreply;
	
	public static void open() throws IOException {
		try {
			if (Device.getLastConnected().getPid().equals("ADDE"))
				DeviceCHangedListener.usbwatch.pause(true);
			readReply();
			if (lastreply == null) throw new IOException("Unable to read from device");
			
		}catch (Exception e) {
			if (lastreply == null) throw new IOException("Unable to read from device");
		}
	}
	
	public static boolean write(S1Packet p) throws IOException,X10FlashException {
		sleep(60);
		JUsb.write(p);
		sleep(60);
		readReply();
		return true;
	}

	private static void sleep(int len) {
		try {
			Thread.sleep(len);
		}
		catch (Exception e) {}
	}
	
    public static  void readReply() throws X10FlashException, IOException
    {
    	S1Packet p = JUsb.read();
    	if (p!=null) {
    		lastreply = p.getDataArray();
    		lastflags = p.getFlags();
    	}
    	else {
    		lastreply = null;
    	}
    }

    public static int getLastFlags() {
    	return lastflags;
    }
    
    public static byte[] getLastReply() {
    	return lastreply;
    }

}