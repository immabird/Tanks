package tanks;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
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
	private volatile LinkedList<Package> packages = new LinkedList<>();
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
			//write.flush();
		} catch(Exception ex) {
			System.out.println("An attempt to write the server has failed.");
			ex.printStackTrace();
		}
	}
	
	/**Sends the Client's Tank to the Server*/
	public void writeTank(){
		write(new Package(myTank));
	}
	
	/**Sends a message to the server letting it know that it is leaving*/
	private void sendLeaveMessage(){
		write(new Package(name));
		System.out.println(name + " just sent leave message.");
	}
	
	/**Connects to the server and keeps listening for packages*/
	private void connect() {
		new Thread(new Runnable() {
			public void run() {
				try(
						 Socket clientSocket = new Socket(ip,port) // Connects the the server
						;ObjectOutputStream aWriter = new ObjectOutputStream(clientSocket.getOutputStream()) // Opens output stream
						;ObjectInputStream read = new ObjectInputStream(clientSocket.getInputStream()) // Opens input stream
				) {
					write = aWriter;
					connectedToServer = true; // Client is officially connected
					Package data;
					while(connectedToServer) { // Reads in the data from the server
						data = (Package)read.readObject();
						if(data.getTank() != null)
							data.getTank().updatePosition();
						packages.add(data);
						System.out.println(name + " read in:" + data.getName() + " ");
					}
				} catch(Exception ex) {
					connectedToServer = false;
					System.out.println(name + "'s connection the the server has failed.");
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
						System.out.println("new player! updatePlayers");
						Package currentP = packages.removeFirst();
						if(currentP.getTank() == null){ //someone is leaving
							Tank removed = tanks.remove(currentP.getName());
							//Take the take off of the pane
							Platform.runLater(new Runnable(){
								@Override
								public void run() {
									pane.getChildren().remove(removed);
								}
							});
						}
						else if(!tanks.containsKey(currentP.getName())){ //Tank isn't in the hashmap yet
							System.out.println("New tank in the hashmap" + currentP.getTank());
							tanks.put(currentP.getName(), currentP.getTank());
							//Add the new tank to the pane
							Platform.runLater(new Runnable(){
								public void run(){
									pane.getChildren().add(tanks.get(currentP.getName()));
									System.out.println(pane.getChildren());
								}
							});
						}
						else{//just update the tank in the map
							Tank oldTank = tanks.replace(currentP.getName(), currentP.getTank());
							//Update the pane/GUI with new position
							Platform.runLater(new Runnable(){
								@Override
								public void run() {
									pane.getChildren().remove(oldTank);
									pane.getChildren().add(tanks.get(currentP.getName()));
								}
							});
						}
					}
				}
			}
		}).start();
	}

	/**Makes a new Tank for this client*/
	private void makeNewTank(){
		myTank = new Tank(name, this);
		//Makes sure that your tank is the focus
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				myTank.requestFocus();
			}
		});
		writeTank();
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
		primaryStage.setOnCloseRequest(e -> {
			sendLeaveMessage();
			stop();
		});
		stage = primaryStage; //Have a class reference to the stage
	}
}
