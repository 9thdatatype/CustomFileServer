/**
 * 
 */
package Communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author Daniel Thertell
 * @version 0.1
 * @since Mar 16, 2016
 */
public class CommunicationConnect {
	private Socket comm = null;
	private ObjectOutputStream oOut;
	private ObjectInputStream oIn;
	
	
	CommunicationConnect(String ip){
		System.out.println("Connecting to: " + ip);
		try {
			comm = new Socket(ip,8800);
			initilize();
		} catch (UnknownHostException e) {
			System.out.println("Host not found!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Socket crashed");
			e.printStackTrace();
		}
		
	}
	
	
	private void initilize() throws IOException{
		oOut = (ObjectOutputStream) comm.getOutputStream();
		oIn = (ObjectInputStream) comm.getInputStream();
		System.out.println("Connection Sucessfull!");
		readData.start();
	}
	
	Thread readData = new Thread(){
		public void run(){
			System.out.println("Reading data!");
			
		}
	};
}


