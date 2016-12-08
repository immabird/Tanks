package tanks;

import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class Tank extends ImageView {
	
	private String name = "Player";
	private boolean w = false;
	private boolean a = false;
	private boolean s = false;
	private boolean d = false;
	private boolean mouseMoved = false;
	private int rotateSpeedMultiplier = 1;
	private int movementSpeedMultiplier = 1;
	private volatile Cannon cannon;
	private volatile Label namePlate;
	private double namePlateOffsetX = 0;
	private double namePlateOffsetY = 0;
	private String color = "";
	private Point mouse = new Point(0,0);
	private boolean mouseClicked = false;
	
	public Tank(String name,double bodyAngle,double xPos,double yPos, double cannonAngle, String color) {
		createTank(name,bodyAngle,cannonAngle,xPos,yPos,color);
	}
	
	public Tank(String name, Client myself, String color) {
		createTank(name,0,0,getX(),getY(),color);
		
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
							boolean colision = false;
							boolean hasChanged = false;
							
							if(finalRotate != getRotate()) {
								setRotate(finalRotate);
								hasChanged = true;
							}
							if(finalX != getX()) {
								setX(finalX);
								hasChanged = true;
							}
							if(finalY != getY()) {
								setY(finalY);
								hasChanged = true;
							}
							
							ObservableList<Node> children = ((Pane) getParent()).getChildren();
							for(Node tank : children) {
								if(tank instanceof Tank && !((Tank) tank).getName().equals(name)) {
									Bounds it = tank.getBoundsInParent();
									Bounds me = getBoundsInParent();
									if(me.intersects(it)) {
										colision = true;
									}
								}
							}
							if(colision) {
								//System.out.println("hi");
							}
							
							if(hasChanged) {
								Scene window = getScene();
								Bounds tank = getBoundsInParent();
								
								if(tank.getMaxX() > window.getWidth()) {
									setX(getX() - 1 * movementSpeedMultiplier);
								} else if(tank.getMinX() < 0) {
									setX(getX() + 1 * movementSpeedMultiplier);
								}
								if(tank.getMaxY() > window.getHeight()) {
									setY(getY() - 1 * movementSpeedMultiplier);
								} else if(tank.getMinY() < 0) {
									setY(getY() + 1 * movementSpeedMultiplier);
								}
								
								namePlate.setLayoutX(finalX + namePlateOffsetX);
								namePlate.setLayoutY(finalY + namePlateOffsetY);
								
								cannon.setCenterX(finalX);
								cannon.setCenterY(finalY);
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
						mouseClicked = true;
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								Point cannonCenter = getCenter(cannon);
								((Pane) getParent()).getChildren().add(new Bullet(cannon.getRotate(),cannonCenter.getX(),cannonCenter.getY()));
								myself.writeTank();
							}
						});
					}
				});
			}
		});
	}
	
	private void createTank(String name, double bodyAngle, double cannonAngle, double x, double y, String color) {
		setImage(GUI_SETTINGS.getBodyImage(color));
		this.name = name;
		this.setRotate(bodyAngle);
		this.setX(x);
		this.setY(y);
		this.color = color;
		
		//Set up cannon
		cannon = new Cannon(cannonAngle,x,y,color);
		//Determines the offset needed to center the head
		Point cannonCenter = getCenter(cannon);
		Point tankCenter = getCenter(this);
		cannon.setOffsetX(tankCenter.getX() - cannonCenter.getX());
		cannon.setOffsetY(tankCenter.getY() - cannonCenter.getY());
		cannon.setCenterX(x);
		cannon.setCenterY(y);
		
		//Set up name plate
		namePlate = new Label(name);
		namePlate.setFont(GUI_SETTINGS.NAME_FONT);
		namePlate.setTextFill(Color.BLACK);
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				((Pane) getParent()).getChildren().addAll(cannon,namePlate);
				
				//Determines the offset needed to center the label
				namePlate.applyCss();
				namePlateOffsetX = tankCenter.getX() - (x + (namePlate.prefWidth(-1) / 2));
				namePlateOffsetY = tankCenter.getY() - (y + (namePlate.prefHeight(-1) / 2));
				namePlate.setLayoutX(x + namePlateOffsetX);
				namePlate.setLayoutY(y + namePlateOffsetY);
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
	
	public String getName() {
		return name;
	}

	public Package getPackage() {
		Package data = new Package(name);
		data.addTankData(getRotate(),getX(),getY(),cannon.getRotate());
		data.setBulletShot(mouseClicked);
		mouseClicked = false;
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
		namePlate.setLayoutX(p.getX() + namePlateOffsetX);
		namePlate.setLayoutY(p.getY() + namePlateOffsetY);
		
		if(p.bulletShot()) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Point cannonCenter = getCenter(cannon);
					((Pane) getParent()).getChildren().add(new Bullet(cannon.getRotate(),cannonCenter.getX(),cannonCenter.getY()));
				}
			});
		}
	}
	
	public Node[] getComponents() {
		Node[] parts = new Node[3];
		parts[0] = this;
		parts[1] = cannon;
		parts[2] = namePlate;
		return parts;
	}
	
	/**@return true if the tank has the same name, x and y pos, and rotation angle*/
	public boolean equals(Tank t){
		return t.getName().equals(this.getName()) && t.getX() == this.getX() && t.getY() == this.getY() && t.getRotate() == this.getRotate();
	}
	
	private class Bullet extends ImageView {
		
		private volatile boolean stillGoing = true;
		private volatile Bullet This;
		
		Bullet(double angle, double x, double y) {
			setImage(new Image("imgs/Pistol Bullet " + color + ".png"));
			
			//Center Bullet
			Point bulletCenter = getCenter(this);
			setX(x - bulletCenter.getX());
			setY(y - bulletCenter.getY());
			setRotate(angle);
			
			//Move bullet ahead of the tank
			setX(getX() + (Math.cos(Math.toRadians(getRotate()))) * ((cannon.getBoundsInLocal().getWidth()/2) + 25));
			setY(getY() + (Math.sin(Math.toRadians(getRotate()))) * ((cannon.getBoundsInLocal().getWidth()/2) + 25));
			
			This = this;
			
			new Thread(new Runnable() {
				@Override
				public void run() {
					while(stillGoing) {
						final double newX = getX() + (Math.cos(Math.toRadians(getRotate()))) * 2;
						final double newY = getY() + (Math.sin(Math.toRadians(getRotate()))) * 2;
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								setX(newX);
								setY(newY);
								Scene window = getScene();
								Bounds bullet = getBoundsInParent();
								try {
									if(bullet.getMaxX() > window.getWidth() || bullet.getMinX() < 0 || bullet.getMaxY() > window.getHeight() || bullet.getMinY() < 0) {
										((Pane) getParent()).getChildren().remove(This);
										stillGoing = false;
									}
								} catch(Exception ex) {
									stillGoing = false;
								}
							}
						});
						try {
							Thread.sleep(10);
						} catch(Exception ex) {}
					}
				}
			}).start();
		}
	}
	
	private class Cannon extends ImageView {
		private double offsetX = 0;
		private double offsetY = 0;
		
		public Cannon(double angle,double x,double y,String color) {
			setImage(GUI_SETTINGS.getTopImage(color));
			setRotate(angle);
			setX(x);
			setY(y);
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
		
	}
	
	private class Point {
		
		double x;
		double y;
		
		public Point() {
			x = 0;
			y = 0;
		}
		
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
