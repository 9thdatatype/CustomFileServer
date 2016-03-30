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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
	private ExecutorService  execute = null;
	private ExecutorService  execute2 = null;
	private ServerSocket input = null;
	private int connectionCounter = 0;
	
	Callable<Object> connectionTask = new Callable<Object>(){

		@Override
		public Object call() throws Exception {
			return input.accept();
		}
		};
		
	Callable<Object> dataTask = new Callable<Object>(){

		@Override
		public Object call() throws Exception {
			byte[] inputArray = null;
			commIn.get(connectionCounter).read(inputArray);
			return inputArray.toString();
		}
		};
	
	
	CommunicationServer(){
		System.out.println("Connections starting up");
		execute = Executors.newCachedThreadPool();
		execute2 = Executors.newCachedThreadPool();
		System.out.println("Pool Initilized");
		connectionThread.start();
		System.out.println("Incoming connection thread started");
		dataThread.start();
		System.out.println("Data Transfer thread started");

		System.out.println("\n-----READY FOR CONNECTION-----\n");
	}
	
	public void stopServer(){
		running = false;
		
		try {
			dataThread.join();
			connectionThread.join();
			System.out.println("Threads safely closed");
			execute.shutdown();
			execute2.shutdown();
		} catch (InterruptedException e1) {
			System.out.println("Failed to properly close threads");
			e1.printStackTrace();
			dataThread.stop();
			connectionThread.stop();
			System.out.println("Brutily killed threads.");
		}
		
		while(connections.size()> 0){
			try {
				commOut.get(0).write("**Close**".getBytes());
				commOut.get(0).flush();
				commOut.get(0).close();
				System.out.println(connections.get(0).getInetAddress() + "'s output closed");
				commIn.get(0).close();
				System.out.println(connections.get(0).getInetAddress() + "'s input closed");
				connections.get(0).close();
				connections.remove(0);
				System.out.println("Sucessfully removed a connection");
			} catch (IOException e) {
				System.out.println("Failed to close connection to: " + 
				connections.get(0).getInetAddress());
				
				e.printStackTrace();
			}
			
		}
		System.out.println("All Connections Closed");
	}
	
	
	//thread used to add new connections to server.
	private Thread connectionThread = new Thread(){
		public void run(){
			//System.out.println(execute == null);
			//System.out.println(connectionTask == null);
			Future<Object> addConnect = execute.submit(connectionTask);
			while (running) {
				try {
					Socket comm = null;
					input = new ServerSocket(8800);
					comm = (Socket) addConnect.get(5, TimeUnit.SECONDS);
					connections.addElement(comm);
					commIn.addElement((ObjectInputStream) comm.getInputStream());
					commOut.addElement((ObjectOutputStream) comm.getOutputStream());
					System.out.println("Connection from: " + comm.getInetAddress());
				} catch (IOException e) {
					e.printStackTrace();
					running = false;
					System.out.println("\n~~~~~ServerSocket Crashed~~~~~");
				} catch (InterruptedException e) {
					//e.printStackTrace();
				} catch (ExecutionException e) {
					//e.printStackTrace();
				} catch (TimeoutException e) {
					addConnect.cancel(true);
					//e.printStackTrace();
				}
				System.out.println("Connect is running");
			}
		}
	};
	
	//thread used to accept data from connections
	private Thread dataThread = new Thread(){
		public void run(){
			Future<Object> readInfo = execute2.submit(connectionTask);
			String data = null;
			while(running){
				if (connections.size() > 0){
					try {
						data = (String) readInfo.get(1, TimeUnit.SECONDS);
					} catch (InterruptedException e) {
						//e.printStackTrace();
					} catch (ExecutionException e) {
						//e.printStackTrace();
					} catch (TimeoutException e) {
						readInfo.cancel(true);
						//e.printStackTrace();
					}
					System.out.println(data);
				}
				System.out.println("Data is Running");
			}
		}
	};
}
