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
	//GUI's stage
	private Stage stage;
	//GUI's pane
	private Pane pane;
	
	/**Makes a new Client given the String, IP, and the client's name
	 * @param anIp  The IP address of the Server
	 * @param aPort  The Port that the server is using
	 * @param name  The name of the Client*/
	public Client(String anIp, int aPort, String name) {
		//Connecting to the server
		ip = anIp;
		port = aPort;
		this.name = name;
		/*UNCOMMENT THIS TO HAVE THE NETWORKING ACTUALLY WORK
		 *I'M GOING TO FORGET TO DO THIS
		 *NEEDS MORE CAPITAL LETTERS SO I CAN SEE IT!!!!!!!!!!!!!!*/
		connect();
		
		//Starting up GUI
		try {
			start(new Stage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**@return true if the client is connected to the server*/
	public boolean isConnected() {
		return connectedToServer;
	}
	
	/**@return the next package from the Queue*/
	private Package getNextPackage() {
		return packages.remove();
	}
	
	/**Disconnects from the server and closes the GUI*/
	public void stop() {
		connectedToServer = false;
		stage.close();
	}
	
	/**Sends a Package to the Server*/
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
	
	/**Sends the Client's Tank to the Server*/
	public void writeTank(){
		write(new Package(myTank));
	}
	
	/**Connects to the server and keeps listening for packages*/
	private void connect() {
		new Thread(new Runnable() {
			public void run() {
				try(
						 Socket clientSocket = new Socket(ip,port) // Connects the the server
						;ObjectInputStream read = new ObjectInputStream(clientSocket.getInputStream()) // Opens input stream
						;ObjectOutputStream aWriter = new ObjectOutputStream(clientSocket.getOutputStream()) // Opens output stream
				) {
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
	
	/**Make a thread to update all of the other player's info*/
	private void updateOtherPlayers(){
		new Thread(new Runnable(){
			@Override
			public void run() {
				while(connectedToServer){
					if(!packages.isEmpty()){//there's a package to get
						Package currentP = packages.poll();
						Tank currentTFromList = tanks.get(currentP.getName());
						Tank currentTFromP = currentP.getTank();
						if(currentTFromList == null){ //Tank isn't in the hashmap yet
							tanks.put(currentP.getName(), currentTFromP);
							pane.getChildren().add(currentTFromP); //This might cause errors due to reference stuff, not really sure
						}
						else{//just update the tank in the map
							currentTFromList = currentTFromP; //This might cause errors due to reference stuff, not really sure
						}
					}
				}
			}
		}).start();
	}

	/**Makes a new Tank for this client*/
	private void makeNewTank(){
		myTank = new Tank(name);
		//Makes sure that your tank is the focus
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				myTank.requestFocus();
			}
		});
	}
	
	@Override
	/**Starts up the GUI window*/
	public void start(Stage primaryStage) throws Exception {
		//Setup the pane
		pane = new Pane();
		pane.setPrefSize(GUI_SETTINGS.GAME_WINDOW_SIZE, GUI_SETTINGS.GAME_WINDOW_SIZE);
		//Add the player's tank to the pane
		makeNewTank();
		pane.getChildren().add(myTank);
		updateOtherPlayers();
		
		//Show the pane
		Scene scene = new Scene(new AnchorPane(pane));
		primaryStage.setTitle(GUI_SETTINGS.MENU_TITLE + " | Player: " + name);
		primaryStage.setScene(scene);
		primaryStage.show();
		stage = primaryStage; //Have a class reference to the stage
	}
}
