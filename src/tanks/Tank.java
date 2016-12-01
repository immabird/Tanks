package tanks;

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

public class Tank extends Rectangle {
	
	private String name = "Player";
	private boolean w = false;
	private boolean a = false;
	private boolean s = false;
	private boolean d = false;
	
	public Tank(String name,double bodyAngle,double xPos,double yPos) {
		setHeight(GUI_SETTINGS.TANK_HEIGHT);
		setWidth(GUI_SETTINGS.TANK_WIDTH);
		
		this.name = name;
		setRotate(bodyAngle);
		setX(xPos);
		setY(yPos);
	}
	
	public Tank(String name, Client myself) {
		setHeight(GUI_SETTINGS.TANK_HEIGHT);
		setWidth(GUI_SETTINGS.TANK_WIDTH);
		this.name = name;
		
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
							setX(Math.cos(Math.toRadians(getRotate()))+getX());
							setY(Math.sin(Math.toRadians(getRotate()))+getY());
						}
					});
				}
				
				if(a) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							if(s) {
								setRotate(Math.floorMod((int) (getRotate() + 1),360));
							} else {
								setRotate(Math.floorMod((int) (getRotate() - 1),360));
							}
						}
					});
				}
				
				if(s) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							setX(Math.cos(Math.toRadians(getRotate()))*(-1)+getX());
							setY(Math.sin(Math.toRadians(getRotate()))*(-1)+getY());
						}
					});
				}
				
				if(d) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							if(s) {
								setRotate(Math.floorMod((int) (getRotate() - 1),360));
							} else {
								setRotate(Math.floorMod((int) (getRotate() + 1),360));
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
	
	public boolean colision(Node node) {
		return this.getBoundsInParent().intersects(node.getBoundsInParent());
	}
	
	public String getName() {
		return name;
	}

	public Package getPackage() {
		Package data = new Package(name);
		data.addTankData(getRotate(),getX(),getY());
		return data;
	}

	
	public boolean equals(Tank t){
		return t.getName().equals(this.getName()) &&t.getX() == this.getX() && t.getY() == this.getY() && t.getRotate() == this.getRotate();
	}
	
}
