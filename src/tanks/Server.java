package tanks;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** 
 * The main job of the server is to relay data between all of the clients during the game
 * The server is also responsible for organizing the users
 */
public class Server extends Application{
	
	private int port; // Stores the port number that the server will start on.
	public Server(int thePort) {
		port = thePort; // Initializes the port
		
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
	private int playerCount = 0;
	private void updateNumberOfPlayers(int add) {
		playerCount += add;
		Platform.runLater(new Runnable(){//change the label on the javafx thread
			@Override
			public void run() {
				// TODO Auto-generated method stub
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
		
		VBox v1 = new VBox();
		v1.getChildren().addAll(title,numberOfPlayers);
		v1.setAlignment(Pos.TOP_CENTER);
		
		StackPane pane = new StackPane(v1);
		pane.setPrefSize(GUI_SETTINGS.SERVER_WIDTH,GUI_SETTINGS.SERVER_HEIGHT);
		StackPane.setAlignment(v1,Pos.TOP_CENTER);
		
		Scene serverScene = new Scene(new AnchorPane(pane));
		stage.setTitle(GUI_SETTINGS.MENU_TITLE);
		stage.setScene(serverScene);
		stage.show();
		
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
					//writer.flush();
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
				try( 	ObjectOutputStream write = new ObjectOutputStream(clientSocket.getOutputStream())
						;ObjectInputStream read = new ObjectInputStream(clientSocket.getInputStream())
					) {
					writers.add(write);
					boolean clientIsOnline = true;	// Determines whether the client is still active
					updateNumberOfPlayers(1);
					Package data;
					while(clientIsOnline && serverIsOnline) {
						data = (Package)read.readObject();
						if("replace with something meaningfull later" == null)
							clientIsOnline = false;
						// TODO Implement more checks in the Package class for the server
						messageAllClients(write,data);
					}
					writers.remove(write); // Removes the client from the "mailing list" after their session has terminated
					updateNumberOfPlayers(-1);
				} catch(Exception ex) {
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
	
}
