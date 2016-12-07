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
	private boolean mouseMoved = false;
	private int rotateSpeedMultiplier = 1;
	private int movementSpeedMultiplier = 1;
	private Cannon cannon;
	private Point mouse = new Point(0,0);
	
	public Tank(String name,double bodyAngle,double xPos,double yPos) {
		setImage(new Image(getClass().getResource("Photoshop/TankBody.png").toExternalForm()));
		cannon = new Cannon(0,xPos,yPos);
		
		//Determines the offset needed to center the head
		Point cannonCenter = getCenter(cannon);
		Point tankCenter = getCenter(this);
		System.out.println(xPos + " " + yPos);
		System.out.println(cannonCenter + " " + tankCenter);
		cannon.setOffsetX(tankCenter.getX() - cannonCenter.getX());
		cannon.setOffsetY(tankCenter.getY() - cannonCenter.getY());
		cannon.setCenterX(xPos);
		cannon.setCenterY(yPos);
		
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
		cannon = new Cannon(0,getX(),getY());
		
		//Determines the offset needed to center the head
		Point cannonCenter = getCenter(cannon);
		Point tankCenter = getCenter(this);
		cannon.setOffsetX(tankCenter.getX() - cannonCenter.getX());
		cannon.setOffsetY(tankCenter.getY() - cannonCenter.getY());
		cannon.setCenterX(getX());
		cannon.setCenterY(getY());
		
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
								cannon.setCenterX(finalX);
								hasChanged = true;
							}
							if(finalY != getY()) {
								setY(finalY);
								cannon.setCenterY(finalY);
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
								
								Point cannonCenter = getCenter(cannon);
								cannon.setRotate(getAngle(cannonCenter.getX(),cannonCenter.getY(),mouse.getX(),mouse.getY()));
							}
							if(hasChanged || mouseMoved) {
								myself.writeTank();
								mouseMoved = false;
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
			}
		});
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				getScene().setOnMouseMoved(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						Point cannonCenter = getCenter(cannon);
						cannon.setRotate(getAngle(cannonCenter.getX(),cannonCenter.getY(),event.getX(),event.getY()));
						mouseMoved = true;
						mouse.setX(event.getX());
						mouse.setY(event.getY());
					}
				});
				
				getScene().setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
					}
				});
			}
		});
	}
	
	public double getAngle(double x1,double y1,double x2,double y2) {
		double opposite = y2-y1;
		double adjasant = x2-x1;
		double angle = Math.toDegrees(Math.atan(opposite/adjasant));
		if(x1 > x2) {
			angle += 180;
		}
		return angle;
	}
	
	private Point getCenter(ImageView image) {
		Point center = new Point();
		center.setX(image.getX() + (image.getBoundsInLocal().getWidth() / 2));
		center.setY(image.getY() + (image.getBoundsInLocal().getHeight() / 2));
		return center;
	}
	
	public boolean colision(Node node) {
		return this.getBoundsInParent().intersects(node.getBoundsInParent());
	}
	
	public String getName() {
		return name;
	}

	public Package getPackage() {
		Package data = new Package(name);
		data.addTankData(getRotate(),getX(),getY(),cannon.getRotate());
		return data;
	}

	/**Takes in a package and edits the data of the tank*/
	public void updateFromPackage(Package p){
		setX(p.getX());
		setY(p.getY());
		setRotate(p.getRotate());
		cannon.setRotate(p.getCannonRotate());
		cannon.setCenterX(p.getX());
		cannon.setCenterY(p.getY());
	}
	
	public Cannon getCannon() {
		return cannon;
	}
	
	/**@return true if the tank has the same name, x and y pos, and rotation angle*/
	public boolean equals(Tank t){
		return t.getName().equals(this.getName()) && t.getX() == this.getX() && t.getY() == this.getY() && t.getRotate() == this.getRotate();
	}
	
	private class Cannon extends ImageView {
		private double offsetX = 0;
		private double offsetY = 0;
		
		public Cannon(double angle,double x,double y) {
			setRotate(angle);
			setX(x);
			setY(y);
			
			setImage(new Image(getClass().getResource("Photoshop/TopTank.png").toExternalForm()));
		}
		
		public void setOffsetY(double offset) {
			offsetY = offset;
		}

		public void setOffsetX(double offset) {
			offsetX = offset;
		}
		
		public void setCenterX(double x) {
			setX(x + offsetX);
		}
		
		public void setCenterY(double y) {
			setY(y + offsetY);
		}

		@SuppressWarnings("unused")
		public Cannon() {
			setImage(new Image(getClass().getResource("Photoshop/TopTank.png").toExternalForm()));
		}
		
	}
	
	private class Point {
		
		double x;
		double y;
		
		public Point() {
			x = 0;
			y = 0;
		}
		
		@SuppressWarnings("unused")
		public Point(double x,double y) {
			this.x = x;
			this.y = y;
		}
		
		public double getX() {
			return x;
		}
		
		public double getY() {
			return y;
		}
		
		public void setX(double x) {
			this.x = x;
		}
		
		public void setY(double y) {
			this.y = y;
		}
		
		@Override
		public String toString() {
			return "X:" + x + " Y:" + y;
		}
	}
}
