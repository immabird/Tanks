package tanks;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

class Client extends Application{
	//Where the server is
	private String ip;
	private int port;
	//The client's name
	private String name;
	//Hashmap of all other client tanks on the server
	private volatile HashMap<String, Tank> tanks = new HashMap<>();
	//This Client's Tank
	private volatile Tank myTank;
	//The heart pictures for life
	private Hearts hearts = new Hearts();
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
	public Client(String anIp, int aPort, String name, String myColor) {
		//Connecting to the server
		ip = anIp;
		port = aPort;
		this.name = name;
		if(name.toLowerCase().contains("rainbow") || name.toLowerCase().contains("unicorn"))
			this.myColor = "Rainbow";
		else if(name.toLowerCase().contains("merica") || (name.toLowerCase().contains("chuck") && name.toLowerCase().contains("norris")) )
			this.myColor = "America";
		else
			this.myColor = myColor;
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
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				stage.close();
			}
		});
	}
	
	/**Sends a Package to the Server*/
	public synchronized void write(Package data) {
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
						addPackage(data);
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
		Client This = this;
		new Thread(new Runnable(){
			@Override
			public void run() {	
				//Wait for the server to connect, if timed out just quit
				while(!connectedToServer && !connectionTimedOut) {}
				
				//Connected to the server, start reading through packets and updating
				while(connectedToServer){
					if(!packagesIsEmpty()){//there's a package to get
						Package currentP = getPackage();
						if(currentP.getNewName()){
							new NewNamePopUp();
							stop();
						}
						if(currentP.getRestart()){
							Platform.runLater(new Runnable(){
								@Override
								public void run() {
									tanks.clear();
									pane.getChildren().clear();
									myTank = new Tank(name, This, myColor);
									pane.getChildren().add(myTank);
									myTank.requestFocus();
									hearts = new Hearts();
									pane.getChildren().add(hearts);
									stage.requestFocus();
								}
							});
							continue;
						}
						if(currentP.isLeaving() || currentP.isDead()){ //someone is leaving or they died
							System.out.println("leaving: " + currentP.isLeaving() + " isDead: " + currentP.isDead());
							Tank removed = tanks.remove(currentP.getName());
							//Take the take off of the pane
							Platform.runLater(new Runnable(){
								@Override
								public void run() {
									if(removed.getComponents()[0] != null)
										pane.getChildren().remove(removed.getComponents()[0]);
									if(removed.getComponents()[1] != null)
										pane.getChildren().remove(removed.getComponents()[1]);
									if(removed.getComponents()[2] != null)
										pane.getChildren().remove(removed.getComponents()[2]);
								}
							});
						}
						else if(!tanks.containsKey(currentP.getName())){ //Tank isn't in the hashmap yet
							//Add the new tank to the pane
							Platform.runLater(new Runnable(){
								public void run(){
									if(!tanks.containsKey(currentP.getName())){
										tanks.put(currentP.getName(), new Tank(currentP.getName(), currentP.getRotate(),currentP.getX(),
																			currentP.getY(),currentP.getCannonRotate(),currentP.getColor(), This));
										pane.getChildren().add(tanks.get(currentP.getName()));
									}
								}
							});
							writeTank(); //Send out position once a new player joins
						}
						else{//just update the tank in the map
							Platform.runLater(new Runnable(){
								@Override
								public void run() {
									if(tanks.containsKey(currentP.getName()))
										tanks.get(currentP.getName()).updateFromPackage(currentP);
								}
							});
						}
						putHeartsOnFront();
					} else {
						putHeartsOnFront();
						//Sleep the thread for a millisecond every cycle so it doesn't use 40% CPU Usage
						try{Thread.sleep(1);}catch(Exception ex){}
					}
				}
			}
		}).start();
	}
	
	private synchronized Package getPackage(){
		return packages.poll();
	}
	
	private synchronized void addPackage(Package p){
		packages.add(p);
	}
	
	private synchronized boolean packagesIsEmpty(){
		return packages.isEmpty();
	}
	
	private void putHeartsOnFront(){
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				hearts.toFront();
			}
		});
	}
	
	/**Makes a new Tank for this client*/
	private void makeNewTank(){
		Client This = this;
		//Makes sure that your tank is the focus
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				myTank = new Tank(name, This, myColor);
				pane.getChildren().add(myTank);
				myTank.requestFocus();
				writeTank();
			}
		});
	}
	
	public Tank getTank() {
		return myTank;
	}
	
	public void decrementHeart(){
		hearts.removeAHeart();
	}
	
	@Override
	/**Starts up the GUI window*/
	public void start(Stage primaryStage) throws Exception {
		//Setup the pane
		pane = new Pane();
		pane.setPrefSize(GUI_SETTINGS.GAME_WINDOW_SIZE, GUI_SETTINGS.GAME_WINDOW_SIZE);
		//Add the player's tank to the pane
		makeNewTank();
		updateOtherPlayers();
	
		pane.getChildren().add(hearts);
		hearts.toFront();
		
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
	
	private class Hearts extends HBox{
		ImageView[] images = new ImageView[GUI_SETTINGS.PLAYER_MAX_LIFE];
		
		private Hearts(){
			super(10);
			for(int x = 0; x < GUI_SETTINGS.PLAYER_MAX_LIFE; x++){
				images[x] = new ImageView("imgs/Heart.png");
				getChildren().add(images[x]);
			}
		}
		
		private void removeAHeart(){
			getChildren().remove(getChildren().size() - 1);
		}
	}
	
	private class NewNamePopUp extends Application{
		private NewNamePopUp(){
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					try {
						start(new Stage());
					} catch (Exception e) {
						System.out.println("New Name Pop Up window failed to start.");
						e.printStackTrace();
					}
				}
			});
		}
		
		@Override
		public void start(Stage stage) throws Exception {
			Label topError = new Label("Someone on this server already has your name.");
			topError.setFont(GUI_SETTINGS.FONT);
			topError.setTextAlignment(TextAlignment.CENTER);
			Label bottomError = new Label("Please go into settings and choose a new name.");
			bottomError.setFont(GUI_SETTINGS.FONT);
			bottomError.setTextAlignment(TextAlignment.CENTER);
			VBox errors = new VBox();
			errors.setAlignment(Pos.CENTER);
			errors.getChildren().addAll(topError, bottomError);
			
			Button okBtn = new Button("OK");
			okBtn.setPrefSize(GUI_SETTINGS.POPUP_BTN_WIDTH, GUI_SETTINGS.POPUP_BTN_HEIGHT);
			okBtn.setOnAction(e -> {
				stage.close();
			});
			
			BorderPane pane = new BorderPane();
			pane.setPrefSize(GUI_SETTINGS.POPUP_WIDTH, GUI_SETTINGS.POPUP_HEIGHT);
			BorderPane.setMargin(okBtn, new Insets(GUI_SETTINGS.POPUP_MARGIN));
			pane.setPadding(new Insets(GUI_SETTINGS.POPUP_PADDING));
			pane.setCenter(errors);
			pane.setBottom(okBtn);
			BorderPane.setAlignment(errors, Pos.CENTER);
			BorderPane.setAlignment(okBtn, Pos.CENTER);
			stage.setScene(new Scene(pane));
			stage.setTitle("ERROR");
			stage.show();
		}
	}
}

