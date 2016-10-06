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
	
	private String name;
	private String ip;
	private int port;
	public Client(String aName, String anIp, int aPort) {
		name = aName;
		ip = anIp;
		port = aPort;
	}
	
	public String getName() {
		return name;
	}
	
	private boolean online = false;
	public void start() {
		
	}
	
	public void stop() {
		online = false;
	}
	
	public void write(String data) {
	
	}
	
	/**
	 * 
	 */
	private void connect() {
		new Thread(new Runnable() {
			public void run() {
				try(Socket cs = new Socket(ip,port)) {
				} catch(Exception ex) {
					System.out.println("");
					ex.printStackTrace();
				}
			}
		}).start();
	}
	
	private HashMap<String,Tank> tanks = new HashMap<String,Tank>();
	private void handleTanks(Tank tank) {
			
	}

}
