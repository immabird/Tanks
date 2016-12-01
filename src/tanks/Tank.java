package tanks;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class Tank extends ImageView {
	
	private String name = "Player";
	private boolean w = false;
	private boolean a = false;
	private boolean s = false;
	private boolean d = false;
	private int rotateSpeedMultiplier = 1;
	private int movementSpeedMultiplier = 1;
	
	private Cannon cannon;
	
	public Tank(String name,double bodyAngle,double xPos,double yPos) {
		setImage(new Image(getClass().getResource("Photoshop/TankBody.png").toExternalForm()));
		cannon = new Cannon(0,xPos,yPos);
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				((Pane) getParent()).getChildren().add(cannon);
			}
		});
		
		this.name = name;
		setRotate(bodyAngle);
		setX(xPos);
		setY(yPos);
	}
	
	public Tank(String name, Client myself) {
		
		setImage(new Image(getClass().getResource("Photoshop/TankBody.png").toExternalForm()));
		cannon = new Cannon();
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				((Pane) getParent()).getChildren().add(cannon);
			}
		});
		
		this.name = name;
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				if(!isFocused()) {
					w = a = s = d = false;
				} else {
					
					double newRotate = getRotate();
					double newX = getX();
					double newY = getY();
				
					if(w) {
						newX += (Math.cos(Math.toRadians(getRotate()))) * movementSpeedMultiplier;
						newY += (Math.sin(Math.toRadians(getRotate()))) * movementSpeedMultiplier;
					}
				
					if(a) {
						if(s) {
							newRotate = (Math.floorMod((int) (getRotate() + 1),360)) * rotateSpeedMultiplier;
						} else {
							newRotate = (Math.floorMod((int) (getRotate() - 1),360)) * rotateSpeedMultiplier;
						}
					}
		
		
					if(s) {
						newX -= (Math.cos(Math.toRadians(getRotate()))) * movementSpeedMultiplier;
						newY -= (Math.sin(Math.toRadians(getRotate()))) * movementSpeedMultiplier;
					}
			
					if(d) {
						if(s) {
							newRotate = (Math.floorMod((int) (getRotate() - 1),360)) * rotateSpeedMultiplier;
						} else {
							newRotate = (Math.floorMod((int) (getRotate() + 1),360)) * rotateSpeedMultiplier;
						}
					}
				
					final double finalRotate = newRotate;
					final double finalX = newX;
					final double finalY = newY;
					
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							boolean hasChanged = false;
							if(finalRotate != getRotate()) {
								setRotate(finalRotate);
								hasChanged = true;
							}
							if(finalX != getX()) {
								setX(finalX);
								cannon.setX(finalX);
								hasChanged = true;
							}
							if(finalY != getY()) {
								setY(finalY);
								cannon.setY(finalY);
								hasChanged = true;
							}
						
							if(hasChanged) {
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
						
								myself.writeTank();
							}
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
	
	private class Cannon extends ImageView {
		
		public Cannon(double angle,double x,double y) {
			setRotate(angle);
			setX(x);
			setY(y);
			
			setImage(new Image(getClass().getResource("Photoshop/TopTank.png").toExternalForm()));
		}
		
		public Cannon() {
			setImage(new Image(getClass().getResource("Photoshop/TopTank.png").toExternalForm()));
		}
		
	}
	
}
