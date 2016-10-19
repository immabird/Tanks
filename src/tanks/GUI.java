package tanks;

import java.lang.annotation.Target;

import javafx.application.Application;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class GUI extends Application {
	
	public static void main(String[] args) {
<<<<<<< HEAD
		launch(args);
=======
		Server s = new Server(25565);
		s.start();
		Client c = new Client("Brad","localhost",25565);
		c.start();
		c.write("print hello");
		
		//launch(args);
>>>>>>> refs/remotes/origin/master
	}
	
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
	
	public void settings(Event e) {
		try {
			Node node = (Node) e.getSource();
			Stage stage = (Stage) node.getScene().getWindow();
			FXMLLoader loader = new FXMLLoader(getClass().getResource("SettingsWindow.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.show();
		} catch(Exception ex) {
			System.out.println("Something has gone wrong with the GUI.");
			ex.printStackTrace();
		}
	}
	
	@FXML private TextField textName;
	@FXML private TextField textIp;
	@FXML private TextField textPort;
	
	public void settingsDone(Event e) {
		try {
			Node node = (Node) e.getSource();
			Stage stage = (Stage) node.getScene().getWindow();
			Parent root = FXMLLoader.load(getClass().getResource("MainMenu.fxml"));
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.show();
		} catch(Exception ex) {
			System.out.println("Something has gone wrong with the GUI.");
			ex.printStackTrace();
		}
	}
	
	public void start(Stage stage) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("SettingsWindow.fxml"));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			stage.setScene(scene);
			stage.show();
		} catch(Exception ex) {
			System.out.println("Something has gone wrong with the GUI.");
			ex.printStackTrace();
		}
	}
}