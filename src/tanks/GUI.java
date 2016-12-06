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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class GUI extends Application {
	private static int port = 25565;
	private static String ip = "localhost";
	private static String name = "Brad";
	private Stage stage;
	
	public void startClient() {
		new Client(ip,port,name);
	}
	
	public void startServer() {
		new Server(port, stage.getX(), stage.getWidth(), stage.getY());
	}
	
	
	private Scene mainMenuScene;
	private Scene settingsScene;
	
	private Button newButton(String name) {
		Button button = new Button(name);
		button.setFont(GUI_SETTINGS.FONT);
		button.setPrefSize(GUI_SETTINGS.BUTTON_WIDTH, GUI_SETTINGS.BUTTON_HEIGHT);
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
						//Go up chain to get VBox of the fields
						VBox vBoxOfAllElements = (VBox) button.getParent();
						HBox hBoxOfInputs = (HBox) vBoxOfAllElements.getChildren().get(1);
						VBox vBoxOfFields = (VBox) hBoxOfInputs.getChildren().get(1);
					
						GUI.name = ((TextField)vBoxOfFields.getChildren().get(0)).getText();
						GUI.ip = ((TextField)vBoxOfFields.getChildren().get(1)).getText();
						try {
							GUI.port = Integer.parseInt(((TextField)vBoxOfFields.getChildren().get(2)).getText());
						} catch(Exception ex) {}
					
						stage.setScene(mainMenuScene);
						break;
				} //end switch
			}//end handle
		}); //end setOnAction
		return button;
	}
	
	private void createMainMenuScene() {
		// Makes the title
		Label title = new Label("Welcome To Tanks!");
		title.setFont(GUI_SETTINGS.TITLE_FONT);
		
		// Makes all the buttons and adds them to a VBox
		VBox vBox = new VBox(GUI_SETTINGS.VERT_SPACING_BTN_ELEMENTS);
		vBox.setAlignment(Pos.CENTER);
		vBox.getChildren().addAll(title,newButton("Start Game"),newButton("Start Server"),newButton("Settings"));
		
		// Puts the VBox in a Pane and centers it
		StackPane pane = new StackPane(vBox);
		StackPane.setAlignment(vBox, Pos.CENTER);
		pane.setPrefSize(GUI_SETTINGS.WINDOW_WIDTH,GUI_SETTINGS.WINDOW_HEIGHT);
		
		// Creates the scene
		mainMenuScene = new Scene(new AnchorPane(pane));
	}
	
	private void createSettingsScene() {
		// Makes the title
		Label title = new Label("Settings!");
		title.setFont(GUI_SETTINGS.TITLE_FONT);
		
		TextField nameField = new TextField(name);
		nameField.setFont(GUI_SETTINGS.FONT);
		
		TextField ipField = new TextField(ip);
		ipField.setFont(GUI_SETTINGS.FONT);
		
		TextField portField = new TextField(""+port);
		portField.setFont(GUI_SETTINGS.FONT);
		
		Label nameLabel = new Label("Name:");
		nameLabel.setFont(GUI_SETTINGS.FONT);
		
		Label ipLabel = new Label("IP:");
		ipLabel.setFont(GUI_SETTINGS.FONT);
		
		Label portLabel = new Label("Port:");
		portLabel.setFont(GUI_SETTINGS.FONT);
		
		VBox vBox1 = new VBox(15 + GUI_SETTINGS.VERT_SPACING_BTN_ELEMENTS);
		vBox1.setAlignment(Pos.CENTER);
		vBox1.getChildren().addAll(nameLabel,ipLabel,portLabel);
		
		VBox vBox2 = new VBox(GUI_SETTINGS.VERT_SPACING_BTN_ELEMENTS);
		vBox2.setAlignment(Pos.CENTER);
		vBox2.getChildren().addAll(nameField,ipField,portField);
		
		HBox hBox = new HBox(GUI_SETTINGS.HOR_SPACING_BTN_ELEMENTS);
		hBox.setAlignment(Pos.CENTER);
		hBox.getChildren().addAll(vBox1,vBox2);
		
		VBox vBox3 = new VBox(GUI_SETTINGS.VERT_SPACING_BTN_ELEMENTS);
		vBox3.setAlignment(Pos.CENTER);
		vBox3.getChildren().addAll(title,hBox,newButton("Done"));
		
		// Puts the VBox in a StackPane and centers it
		StackPane pane = new StackPane(vBox3);
		StackPane.setAlignment(vBox3, Pos.CENTER);
		pane.setPrefSize(GUI_SETTINGS.WINDOW_WIDTH,GUI_SETTINGS.WINDOW_HEIGHT);
		
		// Creates the scene
		settingsScene = new Scene(new AnchorPane(pane));
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
			stage.setScene(mainMenuScene);
			stage.setTitle(GUI_SETTINGS.MENU_TITLE);
			stage.show();
			this.stage = stage;
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