package tanks;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

/** 
 * The main job of the server is to relay data between all of the clients during the game
 * The server is also responsible for organizing the users
 */
public class Server extends Application{
	//Variable to hold the class's stage
	private Stage stage;
	private double GUIXPos;
	private double GUIWidth;
	private double GUIYPos;
	private ServerSocket welcomeSocket;
	
	private int port; // Stores the port number that the server will start on.
	public Server(int thePort, double GUIX, double GUIWidth, double GUIY) {
		port = thePort; // Initializes the port
		GUIXPos = GUIX;
		this.GUIWidth = GUIWidth;
		GUIYPos = GUIY;
		try {
			start(new Stage());
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private boolean serverIsOnline = false; // Keeps the sate of the server
	public boolean isOn() {
		return serverIsOnline; // Returns the state of the server
	}
	
	private Label numberOfPlayers;
	private PlayersList playersList;
	private int playerCount = 0;
	private void updateNumberOfPlayers(int add) {
		playerCount += add;
		Platform.runLater(new Runnable(){//change the label on the javafx thread
			@Override
			public void run() {
				numberOfPlayers.setText("Players: " + playerCount);
			}	
		});
	}
	@Override
	public void start(Stage stage) throws Exception {
		Label title = new Label("Server");
		title.setFont(GUI_SETTINGS.TITLE_FONT);
		
		numberOfPlayers = new Label("Players: 0");
		numberOfPlayers.setFont(GUI_SETTINGS.FONT);
		
		playersList = new PlayersList();
		
		VBox v1 = new VBox();
		v1.getChildren().addAll(title,numberOfPlayers,playersList);
		v1.setAlignment(Pos.TOP_CENTER);
		
		StackPane pane = new StackPane(v1);
		pane.setMinSize(GUI_SETTINGS.SERVER_WIDTH,GUI_SETTINGS.SERVER_HEIGHT);
		StackPane.setAlignment(v1,Pos.TOP_CENTER);
		
		Scene serverScene = new Scene(pane);
		stage.setTitle(GUI_SETTINGS.MENU_TITLE);
		stage.setScene(serverScene);
		stage.setX(GUIXPos + GUIWidth);
		stage.setY(GUIYPos);
		stage.show();
		stage.sizeToScene();
		stage.setOnCloseRequest(e -> {
			stop();
		});
		
		this.stage = stage;
		welcomeSocket(); // Starts the welcome socket
	}
	
	public void stop(){
		sendingQueue.add(null); // Tells all the clients that the server is closing
		serverIsOnline = false; // Sets the state of the server to off
		try {
			if(welcomeSocket != null) //just in case server doesn't connect
				welcomeSocket.close();
		} catch (IOException e) {
			System.out.println("Closing the welcome socket failed");
			e.printStackTrace();
			stage.close();
		}
	}
	
	private volatile LinkedList<ObjectOutputStream> writers = new LinkedList<ObjectOutputStream>(); // Stores the PrintWriters of all the clients
	private void messageAllClients() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while(serverIsOnline){
					if(!sendingQueue.isEmpty() && isSomethingToSend){
						ClientPackageNode node = getNextNode();
						Package clientsData = node.p;
						ObjectOutputStream client = node.o;
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
					else{
						isSomethingToSend = false;
						try{
							Thread.sleep(10);
						}catch(Exception ex){}
					}
				}
			}
		}).start();
		
	}
	
	private synchronized void addNextNode(ClientPackageNode c){
		sendingQueue.add(c);
	}
	
	private synchronized ClientPackageNode getNextNode(){
		return sendingQueue.poll();
	}

	private void welcomeSocket() {
		new Thread(new Runnable() {
			public void run() {
				try { 
					welcomeSocket = new ServerSocket(port); // Creates welcome socket
					serverIsOnline = true; // Server has officially started
					messageAllClients();
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
	
	private volatile LinkedList<ClientPackageNode> sendingQueue = new LinkedList<>();
	private volatile boolean isSomethingToSend = false;
	private void clientSockets(Socket clientSocket) {
		new Thread(new Runnable() {
			public void run() {
				try( 	ObjectOutputStream write = new ObjectOutputStream(clientSocket.getOutputStream())
						;ObjectInputStream read = new ObjectInputStream(clientSocket.getInputStream())
					) {
					writers.add(write);
					boolean clientIsOnline = true;	// Determines whether the client is still active
					updateNumberOfPlayers(1);
					Package data;
					boolean addedName = false;
					while(clientIsOnline && serverIsOnline) {
						try{
							data = (Package)read.readObject();
							if(!addedName){
								playersList.add(data.getName());
								addedName = true;
							}
							if(data.isLeaving()){
								System.out.println("leave message");
								clientIsOnline = false;
								playersList.remove(data.getName());
							}
							// TODO Implement more checks in the Package class for the server
							addNextNode(new ClientPackageNode(write, data));
							isSomethingToSend = true;
						}catch(StreamCorruptedException streamEx){
							System.out.println("Stream corrupted exception");
						}
					}
					writers.remove(write); // Removes the client from the "mailing list" after their session has terminated
					updateNumberOfPlayers(-1);
					System.out.println("writer removed");
				}
				catch(Exception ex) {
					System.out.println("One of the clients threads has crashed.");
					ex.printStackTrace();
					updateNumberOfPlayers(-1);
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
	
	/**Container to hold the writer for who sent the package and their package*/
	private class ClientPackageNode{
		private volatile Package p;
		private volatile ObjectOutputStream o;
		
		private ClientPackageNode(ObjectOutputStream client, Package pack){
			p = pack;
			o = client;
		}
	}//end ClientPackageNode class
	
	/**Extends VBox to hold the PlayersList*/
	private class PlayersList extends VBox{
		private volatile ArrayList<String> names;
		private volatile Label title;
		private volatile Label players;
		private volatile boolean show = false;
		
		/**Makes a new PlayersList*/
		private PlayersList(){
			super();
			names = new ArrayList<>();
			
			title = new Label("Connected Players: ");
			title.setTextAlignment(TextAlignment.CENTER);
			title.setFont(GUI_SETTINGS.FONT);
			title.setStyle("-fx-font-weight: bold");
			title.setManaged(false);
			title.setVisible(false);
			
			//Set up players 
			players = new Label("");
			players.setTextAlignment(TextAlignment.CENTER);
			players.setFont(GUI_SETTINGS.FONT);
			players.setManaged(false);
			
			getChildren().add(title);
			getChildren().add(players);
			setAlignment(Pos.CENTER);
		}
		
		/**Add a new player to the list*/
		private void add(String name){
			if(names.size() == 0)
				show = true;
			names.add(name);
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					if(show){
						title.setVisible(true);
						title.setManaged(true);
						players.setVisible(true);
						players.setManaged(true);
						setManaged(true);
					}
					updatePlayersLabel();
					stage.sizeToScene();
				}
			});
		}
		
		/**Remove a player from the list*/
		private void remove(String name){
			names.remove(name);
			if(names.size() == 0)
				show = false;
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					if(!show){
						title.setVisible(false);
						title.setManaged(false);
						players.setVisible(false);
						players.setVisible(false);
						players.setText("");
						setManaged(false);
					}
					else //don't update if there are no players
						updatePlayersLabel();
					stage.sizeToScene();
				}
			});
		}
		
		/**Rewrite the players label*/
		private void updatePlayersLabel(){
			players.setText("");
			for(String name : names){
				players.setText(players.getText() + name + "\n");
			}
			players.setText(players.getText().substring(0, players.getText().length() - 1));
		}
	}//end PlayersList class
	
}
