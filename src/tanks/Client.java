package tanks;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
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
	private volatile HashMap<String, Tank> tanks = new HashMap<>();
	private String opponentColor;
	//This Client's Tank
	private volatile Tank myTank;
	private String myColor;
	// Keeps track of the connection with the server
	private volatile boolean connectedToServer = false;
	//Turns true to let everything know that the attempt to connect to the server failed
	private volatile boolean connectionTimedOut = false;
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
	public Client(String anIp, int aPort, String name, String myColor, String oppColor) {
		//Connecting to the server
		ip = anIp;
		port = aPort;
		this.name = name;
		this.myColor = myColor;
		this.opponentColor = oppColor;
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
		} catch(Exception ex) {
			System.out.println("An attempt to write the server has failed.");
			ex.printStackTrace();
		}
	}
	
	/**Sends the Client's Tank to the Server*/
	public void writeTank(){
		if(connectedToServer)
			write(myTank.getPackage());
	}
	
	/**Sends a message to the server letting it know that it is leaving*/
	private void sendLeaveMessage(){
		write(new Package(name, true));
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
						packages.add(data);
					}
				} catch(Exception ex) {
					connectedToServer = false;
					connectionTimedOut = true;
					System.out.println(name + "'s connection the the server has failed.");
					ex.printStackTrace();
					Platform.runLater(new Runnable(){ //close the window
						@Override
						public void run() {
							stage.close();
						}
					});
				}
			}
		}).start();
	}
	
	/**Make a thread to update all of the other player's info*/
	private void updateOtherPlayers(){
		new Thread(new Runnable(){
			@Override
			public void run() {	
				//Wait for the server to connect, if timed out just quit
				while(!connectedToServer && !connectionTimedOut) {}
				
				//Connected to the server, start reading through packets and updating
				while(connectedToServer){
					if(!packages.isEmpty()){//there's a package to get
						Package currentP = packages.removeFirst();
						if(currentP.isLeaving()){ //someone is leaving
							Tank removed = tanks.remove(currentP.getName());
							//Take the take off of the pane
							Platform.runLater(new Runnable(){
								@Override
								public void run() {
									pane.getChildren().remove(removed);
									pane.getChildren().remove(removed.getCannon());
								}
							});
						}
						else if(!tanks.containsKey(currentP.getName())){ //Tank isn't in the hashmap yet
							Platform.runLater(new Runnable(){
								public void run(){
									tanks.put(currentP.getName(), new Tank(currentP.getName(), currentP.getRotate(),currentP.getX(),
																			currentP.getY(),currentP.getCannonRotate(),opponentColor));
									pane.getChildren().add(tanks.get(currentP.getName()));
								}
							});
							writeTank(); //Send out position once a new player joins
							//Add the new tank to the pane
							
						}
						else{//just update the tank in the map
							Platform.runLater(new Runnable(){
								@Override
								public void run() {
									tanks.get(currentP.getName()).updateFromPackage(currentP);
								}
							});
						}
					} else {
						//Sleep the thread for 10 milliseconds every cycle so it doesn't use 40% CPU Usage
						try{Thread.sleep(10);}catch(Exception ex){}
					}
				}
			}
		}).start();
	}

	/**Makes a new Tank for this client*/
	private void makeNewTank(){
		myTank = new Tank(name, this, myColor);
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
			primaryStage.close();
		});
		stage = primaryStage; //Have a class reference to the stage
	}
}
