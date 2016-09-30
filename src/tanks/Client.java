package tanks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;

/**
 * @author Bradley Rawson Jr
 */
public class Client {
	private String ip = "localhost";
	private int port = 25565;
	
	private boolean online = false;
	private String name = "User";
	
	private PrintWriter writer;
	
	public Client(String clientName) {
		name = clientName;
	}
	
	// Some getters and setters
	public String getName() {
		return name;
	}
	
	public void setPort(String number) {
		port = Integer.parseInt(number);
	}
	
	public String getPort() {
		return ""+port;
	}
	
	public void setIP(String address) {
		ip = address;
	}
	
	public String getIP() {
		return ip;
	}
	// end getters and setters
	
	public void start() {
		online = true;
		connect();
	}
	
	public void stop() {
		writer.write(name + " quit");
		online = false;
	}
	
	public void write(String data) {
		if(writer == null) {
			System.out.println("Writer is null");
		} else {
			writer.write(name + " " + data);
		}
	}
	
	/**
	 * 
	 */
	private void connect() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try(Socket cs = new Socket(ip,port)) {
				try(BufferedReader br = new BufferedReader(new InputStreamReader(cs.getInputStream()))) { // "br" is used to read messages from the server
				try(PrintWriter pr = new PrintWriter(cs.getOutputStream())) {	// "pr" is used to write messages to the server
					writer = pr;
					write("hi");
					
					if(writer == null) {
						System.out.println("null");
					}
					while(online) {
						String data = br.readLine();
						if(data != null) {
							System.out.println(data);
							String[] splitData = data.split(" ");
							
							switch(splitData[1]) {	// The first word in the message contains the status code which is interpreted here
								case "closing":
									online = false;
									break;
								
								case "message":
									break;
								
								default:
									handleTanks(data);
									break;
							}
						}
					}
				}}} catch(Exception ex) {
					System.out.println("An exception was thrown by the client.");
					ex.printStackTrace();
				}
			}
		}).start();
	}
	
	private HashMap<String,Tank> tanks = new HashMap<String,Tank>();
	
	private void handleTanks(String data) {
		String[] splitData = data.split(" ");
		if(splitData[1].equals("quit")) {
			tanks.get(splitData[0]).delete();
			tanks.remove(splitData[0]);
		} else {
			if(splitData[1].equals("message")) {
				System.out.println(splitData[2]);
				// TODO: make this message print somewhere useful
			} else {
				if(!tanks.containsKey(splitData[0])) {
					tanks.put(splitData[0], new Tank(splitData[0],splitData[2]));
				} else {
					tanks.get(splitData[0]).setPosition(splitData[2]);;
				}
			}
		}
		
	}

}
