package tanks;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

class Client extends Application{
	//Where the server is
	private String ip;
	private int port;
	//The client's name
	private String name;
	//Hashmap of all other client tanks on the server
	private HashMap<String, Tank> tanks = new HashMap<>();
	//This Client's Tank
	private Tank myTank;
	// Keeps track of the connection with the server
	private boolean connectedToServer = false;
	//Stuff for sending/receiving from the server
	private Queue<Package> packages = new ConcurrentLinkedQueue<>();
	private ObjectOutputStream write;
	
	/**Makes a new Client given the String, IP, and the client's name
	 * @param anIp
	 * @param aPort
	 */
	public Client(String anIp, int aPort, String name) {
		//Connecting to the server
		ip = anIp;
		port = aPort;
		this.name = name;
		start();
		
		//Starting up GUI
		try {
			start(new Stage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isConnected() {
		return connectedToServer;
	}
	
	public Package getNextPackage() {
		return packages.remove();
	}
	
	public void start() {
		connect(); // Starts the connection to the server
	}
	
	public void stop() {
		connectedToServer = false;
	}
	
	public void write() {
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
			Package data = new Package(myTank);
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
	
	private void updateOtherPlayers(){
		new Thread(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
			}
			
		}).start();
	}

	/**Makes a new Tank for this client*/
	private void makeNewTank(){
		myTank = new Tank("myTank");
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				myTank.requestFocus();
			}
		});
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		Pane pane = new Pane();
		pane.setPrefSize(GUI_SETTINGS.GAME_WINDOW_SIZE, GUI_SETTINGS.GAME_WINDOW_SIZE);
		makeNewTank();
		pane.getChildren().add(myTank);
		
		Scene scene = new Scene(new AnchorPane(pane));
		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
