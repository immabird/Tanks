package tanks;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

public class Tank extends Rectangle {
	
	private String name;
	private int bodyAngle = 0;
	
	public Tank(String tanksName) {
		name = tanksName;
		setHeight(GUI_SETTINGS.TANK_HEIGHT);
		setWidth(GUI_SETTINGS.TANK_WIDTH);
		
		setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				String key = event.getCharacter();
				switch(key) {
				case"w":
					setX(Math.cos(Math.toRadians(bodyAngle))+getX());
					setY(Math.sin(Math.toRadians(bodyAngle))+getY());
					break;
				case"a":
					bodyAngle = (bodyAngle + 1) % 360;
					setRotate(bodyAngle);
					break;
				case"s":
					setX(Math.cos(Math.toRadians(bodyAngle))*(-1)+getX());
					setY(Math.sin(Math.toRadians(bodyAngle))*(-1)+getY());
					break;
				case"d":
					bodyAngle = (bodyAngle - 1) % 360;
					setRotate(bodyAngle);
					break;
				default:
					break;
				}
			}
		});
		
		setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				
			}
		});
	}
	
	public String getName() {
		return name;
	}
}
