package tanks;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

public class Tank extends Rectangle implements Serializable {
	
	private static final long serialVersionUID = 3327759244053487144L;
	private String name;
	private volatile int bodyAngle = 0;
	private volatile boolean w;
	private volatile boolean a;
	private volatile boolean s;
	private volatile boolean d;
	private volatile double xPos;
	private volatile double yPos;
	
	public Tank(String tanksName, Client myself) {
		name = tanksName;
		xPos = 0;
		yPos = 0;
		w = a = s = d = false;
		setHeight(GUI_SETTINGS.TANK_HEIGHT);
		setWidth(GUI_SETTINGS.TANK_WIDTH);
		
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				if(!isFocused()) {
					w = a = s = d = false;
				}
				
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
							if(s) {
								bodyAngle = Math.floorMod((bodyAngle + 1),360);
								setRotate(bodyAngle);
							} else {
								bodyAngle = Math.floorMod((bodyAngle - 1),360);
								setRotate(bodyAngle);
							}
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
							if(s) {
								bodyAngle = Math.floorMod((bodyAngle - 1),360);
								setRotate(bodyAngle);
							} else {
								bodyAngle = Math.floorMod((bodyAngle + 1),360);
								setRotate(bodyAngle);
							}
						}
					});
				}
				
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						Bounds window = getParent().getParent().getBoundsInParent();
						Bounds tank = getBoundsInParent();
						if(tank.getMaxX() >= window.getMaxX()) {
							setX(getX() - 1);
						} else if(tank.getMinX() <= window.getMinX()) {
							setX(getX() + 1);
						}
						if(tank.getMaxY() >= window.getMaxY()) {
							setY(getY() - 1);
						} else if(tank.getMinY() <= window.getMinY()) {
							setY(getY() + 1);
						}
					}
				});
				
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						xPos = getX();
						yPos = getY();
						myself.writeTank();
					}
				});
				
			}
		},0,10);
		
		setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				KeyCode keyCode = event.getCode();
				String key = keyCode.getName();
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
	
	/**
	 * Updates the position of the tank when it is mysteriously lost in transit.
	 * Use this after sending a tank through an ObjectOutputStream.
	 */
	public void updatePosition() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				setX(xPos);
				setY(yPos);
				setRotate(bodyAngle);
				setHeight(GUI_SETTINGS.TANK_HEIGHT);
				setWidth(GUI_SETTINGS.TANK_WIDTH);
			}
		});
	}
	
	public boolean colision(Node node) {
		return this.getBoundsInParent().intersects(node.getBoundsInParent());
	}
	
	public String getName() {
		return name;
	}
	
	public String getPos() {
		return xPos + " " + yPos;
	}
}
