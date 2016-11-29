package tanks;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

public class Tank extends Rectangle implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3327759244053487144L;
	private String name;
	private int bodyAngle = 0;
	private boolean w = false;
	private boolean a = false;
	private boolean s = false;
	private boolean d = false;
	
	public Tank(String tanksName) {
		name = tanksName;
		setHeight(GUI_SETTINGS.TANK_HEIGHT);
		setWidth(GUI_SETTINGS.TANK_WIDTH);
		
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				if(w) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							setX(Math.cos(Math.toRadians(bodyAngle))+getX());
							setY(Math.sin(Math.toRadians(bodyAngle))+getY());
						}
					});
				}
				
				if(a) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							bodyAngle = Math.floorMod((bodyAngle - 1),360);
							setRotate(bodyAngle);
						}
					});
				}
				
				if(s) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							setX(Math.cos(Math.toRadians(bodyAngle))*(-1)+getX());
							setY(Math.sin(Math.toRadians(bodyAngle))*(-1)+getY());
						}
					});
				}
				
				if(d) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							bodyAngle = Math.floorMod((bodyAngle + 1),360);
							setRotate(bodyAngle);
						}
					});
				}
			}
		},0,10);
		
		setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				KeyCode keyCode = event.getCode();
				String key = keyCode.getName();
				System.out.println(key);
				switch(key) {
				case"W":
					w = true;
					break;
				case"A":
					a = true;
					break;
				case"S":
					s = true;
					break;
				case"D":
					d = true;
					break;
				default:
					break;
				}
				event.consume();
			}
		});
		
		setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				KeyCode keyCode = event.getCode();
				String key = keyCode.getName();
				switch(key) {
				case"W":
					w = false;
					break;
				case"A":
					a = false;
					break;
				case"S":
					s = false;
					break;
				case"D":
					d = false;
					break;
				default:
					break;
				}
				event.consume();
			}
		});
		
		setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				event.consume();
			}
		});
	}
	
	public String getName() {
		return name;
	}
}
