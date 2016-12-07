package tanks;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class GUI extends Application {
	private static int port = 25565;
	private static String ip = "localhost";
	private static String name = "Brad";
	private static String yourColor = "Blue";
	private static String opponentColor = "Red";
	private Stage stage;
	
	public void startClient() {
		new Client(ip, port, name, yourColor, opponentColor);
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
		ImageView title = new ImageView(new Image(getClass().getResource("Photoshop/Title.png").toExternalForm()));
		
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
		ImageView title = new ImageView(new Image(getClass().getResource("Photoshop/SettingsTitle.png").toExternalForm()));
		
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
		
		VBox labelVBox = new VBox(15 + GUI_SETTINGS.VERT_SPACING_BTN_ELEMENTS);
		labelVBox.setAlignment(Pos.CENTER);
		labelVBox.getChildren().addAll(nameLabel,ipLabel,portLabel);
		
		VBox fieldVBox = new VBox(GUI_SETTINGS.VERT_SPACING_BTN_ELEMENTS);
		fieldVBox.setAlignment(Pos.CENTER);
		fieldVBox.getChildren().addAll(nameField,ipField,portField);
		
		HBox textHBox = new HBox(GUI_SETTINGS.HOR_SPACING_BTN_ELEMENTS);
		textHBox.setAlignment(Pos.CENTER);
		textHBox.getChildren().addAll(labelVBox,fieldVBox);
		
		HBox yourTankSelection = new HBox(GUI_SETTINGS.HOR_SPACING_BTN_COLORS);
		yourTankSelection.setAlignment(Pos.CENTER);
		Label yourSelectTank = new Label("Select Your Tank:           ");
		yourSelectTank.setFont(GUI_SETTINGS.FONT);
		ArrayList<ImageView> yourImages = new ArrayList<>();
		ArrayList<String> colors = new ArrayList<>(Arrays.asList("Red", "Blue"));
		DropShadow shadow = new DropShadow(20, Color.BLACK);
		for(String c : colors){
			ImageView temp = new ImageView(new Image(getClass().getResource("Photoshop/" + c + "Tank.png").toExternalForm()));
			temp.setOnMousePressed(e -> {
				for(ImageView i : yourImages)
					i.setEffect(null);
				temp.setEffect(shadow);
				yourColor = c;
			});
			yourImages.add(temp);
		}
		yourImages.get(colors.indexOf(yourColor)).setEffect(shadow);
		yourTankSelection.getChildren().add(yourSelectTank);
		yourTankSelection.getChildren().addAll(yourImages);
		
		HBox oppTankSelection = new HBox(GUI_SETTINGS.HOR_SPACING_BTN_COLORS);
		oppTankSelection.setAlignment(Pos.CENTER);
		Label oppSelectTank = new Label("Select Opponent's Tank:");
		oppSelectTank.setFont(GUI_SETTINGS.FONT);
		ArrayList<ImageView> oppImages = new ArrayList<>();
		for(String c : colors){
			ImageView temp = new ImageView(new Image(getClass().getResource("Photoshop/" + c + "Tank.png").toExternalForm()));
			temp.setOnMousePressed(e -> {
				for(ImageView i : oppImages){
					i.setEffect(null);
				}
				temp.setEffect(shadow);
				opponentColor = c;
			});
			oppImages.add(temp);
		}
		oppImages.get(colors.indexOf(opponentColor)).setEffect(shadow);
		oppTankSelection.getChildren().add(oppSelectTank);
		oppTankSelection.getChildren().addAll(oppImages);
		
		VBox tankSelection = new VBox(GUI_SETTINGS.VERT_SPACING_BTN_ELEMENTS + 10);
		tankSelection.setAlignment(Pos.CENTER);
		tankSelection.getChildren().addAll(yourTankSelection, oppTankSelection);
		
		VBox vBox3 = new VBox(GUI_SETTINGS.VERT_SPACING_BTN_ELEMENTS);
		vBox3.setAlignment(Pos.CENTER);
		vBox3.getChildren().addAll(title, textHBox, tankSelection, newButton("Done"));
		
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
			yourColor = sc.nextLine();
			opponentColor = sc.nextLine();
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
					pr.println(yourColor);
					pr.println(opponentColor);
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