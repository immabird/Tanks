package tanks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

/** 
 * The main job of the server is to relay data between all of the clients during the game
 * The server is also responsible for organizing the users
 */
public class Server {
	
	/**
	 * Keeps track of the state of the server
	 */
	private boolean isOn = false;
	
	/**
	 * Stores all the PrintWriters of the clients for mass communication
	 */
	private LinkedList<PrintWriter> writers = new LinkedList<PrintWriter>();
	
	/**
	 * Provides access the the isOn variable
	 * 
	 * @return The servers state
	 */
	public boolean isOn() {
		return isOn;
	}
	
	/**
	 * Creates a thread for the server to run on and retrieves the port which
	 * it passes along to the welcome socket
	 * 
	 * @param port
	 */
	public void start(int port) {
		if (!writers.isEmpty()) {
			writers.clear();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				welcomeSocket(port);
			}
		}).start();
	}
	
	/**
	 * Creates a thread for the server to run on and retrieves the port which
	 * it passes along to the welcome socket
	 */
	public void start() {
		if (!writers.isEmpty()) {
			writers.clear();
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				welcomeSocket(1650);
			}
		}).start();
	}
	
	/**
	 * Stops the server.
	 */
	public void stop() {
		messageAll(null,"server closing");
		isOn = false;
	}

	/**
	 * Starts the welcome socket which waits for clients to try and connect
	 */
	private void welcomeSocket(int port) {
		try(ServerSocket ss = new ServerSocket(port)) { // Creates server socket "ss"
			isOn = true;
			while(isOn) {
				try(Socket cs = ss.accept()) {	// Accepts new client
					handleClient(cs);
				} catch(Exception ex) {
					System.out.println("An error has occured while trying to accept a client");
					ex.printStackTrace();
				}
			}
		} catch(Exception ex) {
			isOn = false;
			System.out.println("An exception was thrown by the server.");
			ex.printStackTrace();
		}
	}
	
	/**
	 * Each client is sent to this method to be taken care of
	 *  
	 * @param Socket
	 */
	private void handleClient(Socket cs) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try(BufferedReader br = new BufferedReader(new InputStreamReader(cs.getInputStream()))) { // "br" is used to read messages from the client
				try(PrintWriter pr = new PrintWriter(cs.getOutputStream())) {	// "pr" is used to write messages to the client
					boolean online = true;	// Determines whether the client is still active
					writers.add(pr);
					
					while(online && isOn) {
						String data = br.readLine();
						String[] splitData = data.split(" ");
						
						switch(splitData[1]) {	// The second word in the message contains the status code which is interpreted here
							case "quit":	// Signals the client is leaving
								messageAll(pr, splitData[0] + " quit");
								online = false;
								break;
									
							case "data": // Sends the clients positional data to the other players
								messageAll(pr,data);
								break;
						}
					}
						
					writers.remove(pr); // Removes the client from the "mailing list" after their session has terminated
				}} catch(Exception ex) {
					System.out.println("An exception was thrown by a client.");
					ex.printStackTrace();
				}
			}
		}).start();
	}
	
	/**
	 * Messages all other clients on the server with positional data
	 * and messages
	 * 
	 * @param PrintWriter
	 * @param String
	 */
	private void messageAll(PrintWriter self,String data) {
		for (PrintWriter pr : writers) { // Prints the message to all the clients except for the sender
			if(!pr.equals(self)) {
				pr.write(data);
			}
		}
	}
	
}
