/**
 * 
 */
package Communication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

/**
 * @author Daniel Thertell
 * @version 0.1
 * @since Mar 16, 2016
 */
public class CommunicationServer {
	private Vector<Socket> connections = new Vector<Socket>();
	private Vector<ObjectInputStream > commIn = new Vector<ObjectInputStream>();
	private Vector<ObjectOutputStream> commOut = new Vector<ObjectOutputStream>();
	private boolean running = true;
	
	
	CommunicationServer(){
		System.out.println("Connections starting up");
		connectionThread.start();
		System.out.println("Incoming connection thread started");
		dataThread.start();
		System.out.println("Data Transfer thread started");
		System.out.println("\n-----READY FOR CONNECTION-----\n");
	}
	
	public void stop(){
		running = false;
		while(connections.size()> 0){
			try {
				commOut.get(0).write("**Disconnect".getBytes());
				commOut.get(0).flush();
				commOut.get(0).close();
				System.out.println(connections.get(0).getInetAddress() + "'s output closed");
				commIn.get(0).close();
				System.out.println(connections.get(0).getInetAddress() + "'s input closed");
				connections.get(0).close();
				connections.remove(0);
				System.out.println("Sucessfully removed a connection");
			} catch (IOException e) {
				System.out.println("Failed to close");
				e.printStackTrace();
			}
		}
		
	}
	
	
	//thread used to add new connections to server.
	Thread connectionThread = new Thread(){
		public void run(){
			ServerSocket input = null;
			while (running) {
				try {
					Socket comm = null;
					input = new ServerSocket(8800);
					comm = input.accept();
					connections.addElement(comm);
					commIn.addElement((ObjectInputStream) comm.getInputStream());
					commOut.addElement((ObjectOutputStream) comm.getOutputStream());
					System.out.println("Connection from: " + comm.getInetAddress());
				} catch (IOException e) {
					e.printStackTrace();
					running = false;
					System.out.println("\n~~~~~ServerSocket Crashed~~~~~");
				}
			}
		}
	};
	
	
	//thread used to accept data from connections
	Thread dataThread = new Thread(){
		public void run(){
			while(running){
				if (connections.size() > 0){
					System.out.println("DATA TRANSFER SHOULD HAPPEN HERE!");
				}
			}
		}
	};
}
