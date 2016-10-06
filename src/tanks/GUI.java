package tanks;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class GUI extends Application {
	
	private Client client;
	private Server server;
	
	public static void main(String[] args) {
		Server s = new Server();
		s.start();
		Client c = new Client("Brad");
		c.start();
		c.write("print hello");
		
		//launch(args);
	}

	public void start(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load(getClass().getResource("display.fxml"));
			Scene scene = new Scene(root);
			
			primaryStage.setTitle("Tanks");
			
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception ex) {
			System.out.println("Something has gone wrong with the GUI.");
			ex.printStackTrace();
		}
	}
	
}