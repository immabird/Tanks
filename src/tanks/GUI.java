package tanks;

import java.io.File;
import java.io.PrintWriter;
import java.util.Scanner;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class GUI extends Application {
	private static int port = 25565;
	private static String ip = "localhost";
	private static String name = "Brad";
	private static Client client;
	private static Server server;
	
	public void startClient() {
		client = new Client(ip,port);
	}
	
	public void startServer() {
		server = new Server(port);
	}
	
	final private Font TITLE_FONT = new Font(46);
	final private Font FONT = new Font(16);
	final private int WINDOW_HEIGHT = 600;
	final private int WINDOW_WIDTH = 800;
	final private int BUTTON_HEIGHT = 20;
	final private int BUTTON_WIDTH = 120;
	private Scene mainMenuScene;
	private Scene settingsScene;
	
	private Button newButton(String name) {
		Button button = new Button(name);
		button.setFont(FONT);
		button.setPrefSize(BUTTON_WIDTH, BUTTON_HEIGHT);
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Node node = (Node) event.getSource();
				Stage stage = (Stage) node.getScene().getWindow();
				Button button = (Button) node;
				
				switch(button.getText()) {
				case"Start Game":
					startClient();
					break;
				case"Start Server":
					startServer();
					break;
				case"Settings":
					stage.setScene(settingsScene);
					break;
				case"Done":
					HBox hBox = (HBox) button.getParent().getParent();
					VBox vBox = (VBox) hBox.getChildren().get(1);
					GUI.name = ((TextField)vBox.getChildren().get(0)).getText();
					GUI.ip = ((TextField)vBox.getChildren().get(1)).getText();
					try {
						GUI.port = Integer.parseInt(((TextField)vBox.getChildren().get(2)).getText());
					} catch(Exception ex) {}
					
					stage.setScene(mainMenuScene);
					break;
				}
			}
		});
		return button;
	}
	
	private void createMainMenuScene() {
		// Makes the title
		Label title = new Label("Welcome To Tanks!");
		title.setFont(TITLE_FONT);
		
		// Makes all the buttons and adds them to a VBox
		VBox vBox = new VBox();
		vBox.setAlignment(Pos.CENTER);
		vBox.getChildren().addAll(title,newButton("Start Game"),newButton("Start Server"),newButton("Settings"));
		
		// Puts the VBox in a Pane
		Pane pane = new Pane(vBox);
		pane.setPrefSize(WINDOW_WIDTH,WINDOW_HEIGHT);
		
		// Creates the scene
		mainMenuScene = new Scene(new AnchorPane(pane));
	}
	
	private void createSettingsScene() {
		// Makes the title
		Label title = new Label("Settings!");
		title.setFont(TITLE_FONT);
		
		TextField nameField = new TextField(name);
		nameField.setFont(FONT);
		
		TextField ipField = new TextField(ip);
		ipField.setFont(FONT);
		
		TextField portField = new TextField(""+port);
		portField.setFont(FONT);
		
		Label nameLabel = new Label("Name:");
		nameLabel.setFont(FONT);
		
		Label ipLabel = new Label("IP:");
		ipLabel.setFont(FONT);
		
		Label portLabel = new Label("Port:");
		portLabel.setFont(FONT);
		
		VBox vBox1 = new VBox();
		vBox1.setAlignment(Pos.CENTER);
		vBox1.getChildren().addAll(nameLabel,ipLabel,portLabel,newButton("Done"));
		
		VBox vBox2 = new VBox();
		vBox2.setAlignment(Pos.CENTER);
		vBox2.getChildren().addAll(nameField,ipField,portField);
		
		HBox hBox = new HBox();
		hBox.setAlignment(Pos.CENTER);
		hBox.getChildren().addAll(vBox1,vBox2);
		
		VBox vBox3 = new VBox();
		vBox3.setAlignment(Pos.CENTER);
		vBox3.getChildren().addAll(title,hBox);
		
		// Puts the VBox in a Pane
		Pane pane = new Pane(vBox3);
		pane.setPrefSize(WINDOW_WIDTH,WINDOW_HEIGHT);
				
		// Creates the scene
		settingsScene = new Scene(new AnchorPane(pane));
	}
	
	private void createGameScene() {
		
	}
	
	public void start(Stage stage) {
		try(Scanner sc = new Scanner(new File("settings.txt"))) {
			name = sc.nextLine();
			ip = sc.nextLine();
			port = Integer.parseInt(sc.nextLine());
		} catch(Exception ex) {}
		
		try {
			createMainMenuScene();
			createSettingsScene();
			createGameScene();
			stage.setScene(mainMenuScene);
			stage.setTitle("Tanks");
			stage.show();
			stage.setOnCloseRequest(EventHandler -> {
				try(PrintWriter pr = new PrintWriter(new File("settings.txt"))) {
					pr.println(name);
					pr.println(ip);
					pr.println(port);
				} catch(Exception ex) {}
				System.exit(0);
			});
		} catch(Exception ex) {
			System.out.println("Something has gone wrong with the GUI.");
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}