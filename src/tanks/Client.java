package tanks;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Queue;

class Client {

	private String ip;
	private int port;
	public Client(String anIp, int aPort) {
		ip = anIp;
		port = aPort;
	}
	
	private boolean connectedToServer = false; // Keeps track of the connection with the server
	public boolean isConnected() {
		return connectedToServer;
	}
	
	Queue<Package> packages;
	public Package getNextPackage() {
		return packages.remove();
	}
	
	public void start() {
		connect(); // Starts the connection to the server
	}
	
	public void stop() {
		connectedToServer = false;
	}
	
	private ObjectOutputStream write;
	public void write(Package data) {
		int i = 0;
		while(!connectedToServer && i < 500) {
			try {
				Thread.sleep(10); // The thread tries to wait for a connection to the server
			} catch(Exception ex) {
				System.out.println("Thread sleep failed. Not signifigant error.");
			}
			i++;
		}
		try {
			write.writeObject(data); // Sends a package to the server
		} catch(Exception ex) {
			System.out.println("An attempt to write the server has failed.");
			ex.printStackTrace();
		}
	}
	
	private void connect() {
		new Thread(new Runnable() {
			public void run() {
				try(Socket clientSocket = new Socket(ip,port) // Connects the the server
				;ObjectInputStream read = new ObjectInputStream(clientSocket.getInputStream()) // Opens input stream
				;ObjectOutputStream aWriter = new ObjectOutputStream(clientSocket.getOutputStream())) { // Opens output stream
					write = aWriter;
					connectedToServer = true; // Client is offically connected
					Package data;
					while(connectedToServer) { // Reads in the data from the server
						data = (Package)read.readObject();
						packages.add(data);
					}
				} catch(Exception ex) {
					connectedToServer = false;
					System.out.println("The connection the the server has failed.");
					ex.printStackTrace();
				}
			}
		}).start();
	}
}
