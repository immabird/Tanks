package tanks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author Bradley Rawson Jr
 */
public class Client {
	private String ip = "localhost";
	private int port = 1650;
	
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
		writer.write(name + " " + data);
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
					
					while(online) {
						String data = br.readLine();
						String[] splitData = data.split(" ");
						
						switch(splitData[1]) {	// The first word in the message contains the status code which is interpreted here
							case "closing":
								online = false;
								break;
								
							case "quit":
								
								break;
								
							case "data":
								
								break;
						}
					}
				}}} catch(Exception ex) {
					System.out.println("An exception was thrown by the client.");
					ex.printStackTrace();
				}
			}
		}).start();
	}

}
