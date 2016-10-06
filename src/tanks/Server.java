package tanks;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

/** 
 * The main job of the server is to relay data between all of the clients during the game
 * The server is also responsible for organizing the users
 */
public class Server {
	
	private int port; // Stores the port number that the server will start on.
	public Server(int thePort) {
		port = thePort; // Initializes the port
	}
	
	private boolean serverIsOnline = false; // Keeps the sate of the server
	public boolean isOn() {
		return serverIsOnline; // Returns the state of the server
	}
	
	public void start() {
		welcomeSocket(); // Starts the welcome socket
	}
	
	public void stop() {
		messageAllClients(null,null); // Tells all the clients that the server is closing
		serverIsOnline = false; // Sets the state of the server to off
	}
	
	private LinkedList<ObjectOutputStream> writers = new LinkedList<ObjectOutputStream>(); // Stores the PrintWriters of all the clients
	private void messageAllClients(ObjectOutputStream client,Package clientsData) {
		try {
			for (ObjectOutputStream writer: writers) { // Prints the message to all the clients except for the sender
				if(!writer.equals(client)) { // Does not send the message back to the sender.
					writer.writeObject(clientsData); // Sends the message
				}
			}
		} catch(Exception ex) {
			System.out.println("Failed to send the package to all the clients.");
			ex.printStackTrace();
		}
	}

	private void welcomeSocket() {
		new Thread(new Runnable() {
			public void run() {
				try(ServerSocket welcomeSocket = new ServerSocket(port)) { // Creates welcome socket
					serverIsOnline = true; // Server has officially started
					while(serverIsOnline) {
						clientSockets(welcomeSocket.accept()); // Accepts new clients
					}
				} catch(Exception ex) { // What to do if an exception happens
					serverIsOnline = false; // Signals that the server has shutdown
					System.out.println("The welcome socket has failed.");
					ex.printStackTrace();
				}
			}
		}).start();
	}
	
	private void clientSockets(Socket clientSocket) {
		new Thread(new Runnable() {
			public void run() {
				try(ObjectInputStream read = new ObjectInputStream(clientSocket.getInputStream())
				;ObjectOutputStream write = new ObjectOutputStream(clientSocket.getOutputStream())) {
					boolean clientIsOnline = true;	// Determines whether the client is still active
					writers.add(write); 
					while(clientIsOnline && serverIsOnline) {
						Package data = (Package)read.readObject();
						// TODO Implement more checks in the Package class for the server
						messageAllClients(write,data);
					}
					writers.remove(write); // Removes the client from the "mailing list" after their session has terminated
				} catch(Exception ex) {
					System.out.println("One of the clients threads has crashed.");
					ex.printStackTrace();
				}
				try {
					clientSocket.close();
				} catch (Exception ex) {
					System.out.println("Could not close a client socket that is no longer in use.");
					ex.printStackTrace();
				}
			}
		}).start();
	}
}
