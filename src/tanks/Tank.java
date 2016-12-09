package tanks;

import java.util.ArrayList;
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

public class Tank extends ImageView implements Comparable<Tank> {
	
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
	
	// fast move cannon
	private Point mouse = new Point(0,0);
	
	private boolean mouseClicked = false;
	private int health = GUI_SETTINGS.PLAYER_MAX_LIFE;
	private boolean isDead = false;
	private Client myself;
	private boolean canShoot = true;
	
	// slow move cannon
	//private boolean cannonRotateLeft = false;
	//private boolean cannonRotateRight = false;
	//private double targetCannonRotate = 0;
	
	public Tank(String name,double bodyAngle,double xPos,double yPos, double cannonAngle, String color, Client myself) {
		createTank(name,bodyAngle,cannonAngle,xPos,yPos,color,myself);
	}
	
	public Tank(String name, Client myself, String color) {
		createTank(name,0,0,getX(),getY(),color,myself);
		
		Timer t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				if(!isFocused() && isDead) {
					w = a = s = d = false;
					
					//slow move cannon
					//cannonRotateLeft = cannonRotateRight = false;
				} else {
					// slow move cannon
					//double newCannonRotate = cannon.getRotate();
					
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
					
					// slow move cannon
					/*
					if(cannonRotateLeft) {
						newCannonRotate = (Math.floorMod((int) (cannon.getRotate() - 1),360));
						if(newCannonRotate > targetCannonRotate - 2 && newCannonRotate < targetCannonRotate + 2) {
							newCannonRotate = targetCannonRotate;
							cannonRotateLeft = false;
						}
					}
					if(cannonRotateRight) {
						newCannonRotate = (Math.floorMod((int) (cannon.getRotate() + 1),360));
						if(newCannonRotate > targetCannonRotate - 2 && newCannonRotate < targetCannonRotate + 2) {
							newCannonRotate = targetCannonRotate;
							cannonRotateRight = false;
						}
					}
					final double finalCannonRotate = newCannonRotate;
					*/
					
					final double finalRotate = newRotate;
					final double finalX = newX;
					final double finalY = newY;
					
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							boolean hasChanged = false;
							boolean colision = false;
							
							if(finalRotate != getRotate() || finalX != getX() || finalY != getY()) {
								hasChanged = true;
								double oldRotate = getRotate();
								double oldX = getX();
								double oldY = getY();
								
								setRotate(finalRotate);
								setX(finalX);
								setY(finalY);
								Tank me = This();
							
								ObservableList<Node> children = ((Pane) getParent()).getChildren();
								for(Node tank : children) {
									if(tank instanceof Tank && !((Tank) tank).getName().equals(name)) {
										if(colision(me, (ImageView) tank)) {
											colision = true;
										}
									}
								}
								
								if(colision) {
									setRotate(oldRotate);
									setX(oldX);
									setY(oldY);
									hasChanged = false;
								}
							}
							
							// slow move cannon
							//if(finalCannonRotate != cannon.getRotate()) {
							//	cannon.setRotate(finalCannonRotate);
							//	hasChanged = true;
							//}
							
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
								
								
								snapComponents();
								
								
								//fast move cannon
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
				case"Space":
					shoot();
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
						if(!isDead) {
							Point cannonCenter = getCenter(cannon);
							
							// slow move cannon
							/*
							targetCannonRotate = getAngle(cannonCenter.getX(),cannonCenter.getY(),event.getX(),event.getY());
							if(Math.abs(targetCannonRotate - cannon.getRotate()) > (targetCannonRotate + (359 - cannon.getRotate()))) {
								System.out.println("left");
								cannonRotateLeft = true;
								cannonRotateRight = false;
							} else {
								System.out.println("right");
								cannonRotateRight = true;
								cannonRotateLeft = false;
							}
							*/
							
							// fast move cannon
							cannon.setRotate(getAngle(cannonCenter.getX(),cannonCenter.getY(),event.getX(),event.getY()));
							mouseMoved = true;
							mouse.setX(event.getX());
							mouse.setY(event.getY());
						}
					}
				});
				
				getScene().setOnMouseDragged(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if(!isDead) {
							Point cannonCenter = getCenter(cannon);
							
							// slow move cannon
							/*
							targetCannonRotate = getAngle(cannonCenter.getX(),cannonCenter.getY(),event.getX(),event.getY());
							if(Math.abs(targetCannonRotate - cannon.getRotate()) > (targetCannonRotate + (359 - cannon.getRotate()))) {
								System.out.println("left");
								cannonRotateLeft = true;
								cannonRotateRight = false;
							} else {
								System.out.println("right");
								cannonRotateRight = true;
								cannonRotateLeft = false;
							}
							*/
							
							// fast move cannon
							cannon.setRotate(getAngle(cannonCenter.getX(),cannonCenter.getY(),event.getX(),event.getY()));
							mouseMoved = true;
							mouse.setX(event.getX());
							mouse.setY(event.getY());
						}
					}
				});
				
				getScene().setOnMousePressed(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						shoot();
					}
				});
			}
		});
	}
	
	private void createTank(String name, double bodyAngle, double cannonAngle, double x, double y, String color, Client myself) {
		setImage(GUI_SETTINGS.getBodyImage(color));
		this.name = name;
		this.myself = myself;
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
	
	private Tank This() {
		return this;
	}
	
	private int imHacking = 0;
	private void shoot() {
		if(!isDead && canShoot) {
			if(imHacking < 5) {
				canShoot = false;
			}
			
			mouseClicked = true;
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					Point cannonCenter = getCenter(cannon);
					((Pane) getParent()).getChildren().add(new Bullet(cannon.getRotate(),cannonCenter.getX(),cannonCenter.getY()));
					myself.writeTank();
					
					Bounds window = ((Pane) getParent()).getBoundsInParent();
					Bounds cannonBounds = cannon.getBoundsInParent();
					if(cannonBounds.getMaxX() > window.getWidth() || cannonBounds.getMinX() < 0 || cannonBounds.getMaxY() > window.getHeight() || cannonBounds.getMinY() < 0) {
						if(imHacking < 5) {
							imHacking++;
						}
					}
				}
			});
			
			if(imHacking < 5) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try{Thread.sleep(GUI_SETTINGS.SHOOT_DELAY);}catch(Exception ex){}
						canShoot = true;
					}
				}).start();
			}
		}
	}
	
	public boolean colision(ImageView image1, ImageView image2) {
		Line[] myLines = getEdges(image1);
		Line[] theirLines = getEdges(image2);
		for(Line myLine : myLines) {
			for(Line theirLine : theirLines) {
				Point intersection = myLine.intersection(theirLine);
				if(intersection == null) {
					continue;
				} else if(intersection.getX() == myLine.getP1().getX() && intersection.getY() == myLine.getP1().getY()) {
					if(theirLine.contains(myLine.getP1()) || theirLine.contains(myLine.getP2())) {
						return true;
					}
				} else {
					if(myLine.contains(intersection) && theirLine.contains(intersection)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public Line[] getEdges(ImageView image) {
		Point center = getCenter(image);
		double x = center.getX();
		double y = center.getY();
		double angle = image.getRotate();
		double height = image.getBoundsInLocal().getHeight();
		double width = image.getBoundsInLocal().getWidth();
		Line[] lines = new Line[4];
		Point topRight = new Point(x + (Math.cos(Math.toRadians(angle)) * (width / 2)) + (Math.sin(Math.toRadians(angle)) * (height / 2)),
								y + (Math.sin(Math.toRadians(angle)) * (width / 2)) - (Math.cos(Math.toRadians(angle)) * (height / 2)));
		
		Point topLeft = new Point(x - (Math.cos(Math.toRadians(angle)) * (width / 2)) + (Math.sin(Math.toRadians(angle)) * (height / 2)),
								y - (Math.sin(Math.toRadians(angle)) * (width / 2)) - (Math.cos(Math.toRadians(angle)) * (height / 2)));
		
		Point bottomRight = new Point(x + (Math.cos(Math.toRadians(angle)) * (width / 2)) - (Math.sin(Math.toRadians(angle)) * (height / 2)),
								y + (Math.sin(Math.toRadians(angle)) * (width / 2)) + (Math.cos(Math.toRadians(angle)) * (height / 2)));
		
		Point bottomLeft = new Point(x - (Math.cos(Math.toRadians(angle)) * (width / 2)) - (Math.sin(Math.toRadians(angle)) * (height / 2)),
								y - (Math.sin(Math.toRadians(angle)) * (width / 2)) + (Math.cos(Math.toRadians(angle)) * (height / 2)));
		
		lines[0] = new Line(topRight,topLeft);
		lines[1] = new Line(topRight,bottomRight);
		lines[2] = new Line(bottomRight,bottomLeft);
		lines[3] = new Line(bottomLeft,topLeft);
		return lines;
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
	
	public void reset() {
		if(Platform.isFxApplicationThread()) {
			w = a = s = d = false;
			mouseMoved = false;
			setX(0);
			setY(0);
			setRotate(0);
			snapComponents();
			mouseClicked = false;
			canShoot = true;
			health = GUI_SETTINGS.PLAYER_MAX_LIFE;
			isDead = false;
			imHacking = 0;
		}
	}
	
	public void kill() {
		if(health != 0) {
			health--;
			myself.decrementHeart();
		}
		if(health == 0) {
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					((Pane) getParent()).getChildren().removeAll(getComponents());
					isDead = true;
					myself.writeTank();
					health = GUI_SETTINGS.PLAYER_MAX_LIFE;
				}
			});
		}
	}
	
	public String getName() {
		return name;
	}

	public Package getPackage() {
		Package data = new Package(name);
		if(isDead) {
			mouseClicked = false;
			data.setIsDead();
		}
		data.addTankData(getRotate(),getX(),getY(),cannon.getRotate(),color);
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
	
	public void setIsDead(boolean isDead) {
		this.isDead = isDead;
	}
	
	public Node[] getComponents() {
		Node[] parts = new Node[3];
		parts[0] = this;
		parts[1] = cannon;
		parts[2] = namePlate;
		return parts;
	}
	
	public int getHealth(){
		return health;
	}
	
	public void snapComponents() {
		if(Platform.isFxApplicationThread()) {
			namePlate.setLayoutX(getX() + namePlateOffsetX);
			namePlate.setLayoutY(getY() + namePlateOffsetY);
			cannon.setCenterX(getX());
			cannon.setCenterY(getY());
		}
	}
	
	/**@return true if the tank has the same name, x and y pos, and rotation angle*/
	public boolean equals(Tank t){
		return t.getName().equals(this.getName()) && t.getX() == this.getX() && t.getY() == this.getY() && t.getRotate() == this.getRotate();
	}
	
	@Override
	public int compareTo(Tank tank) {
		return this.name.compareTo(tank.getName());
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
			setX(getX() + (Math.cos(Math.toRadians(getRotate()))) * ((cannon.getBoundsInLocal().getWidth()/2) + 22));
			setY(getY() + (Math.sin(Math.toRadians(getRotate()))) * ((cannon.getBoundsInLocal().getWidth()/2) + 22));
			
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
									if(bullet.getMaxX() > window.getWidth() || bullet.getMinX() < 0 || bullet.getMaxY() > window.getHeight() || bullet.getMinY() < 0 || !stillGoing) {
										stillGoing = false;
									}
								} catch(Exception ex) {
									stillGoing = false;
								}
								if(getParent() != null) {
									ArrayList<Node> nodes = new ArrayList<Node>();
									ObservableList<Node> children = ((Pane) getParent()).getChildren();
									for(Node tank : children) {
										if(tank != null && tank instanceof Tank) {
											if(colision(This, (ImageView) tank)) {
												if(((Tank) tank).getName().equals(myself.getTank().getName())) {
													myself.getTank().kill();
												}
												stillGoing = false;
											}
										} else if(tank != null && tank instanceof Bullet) {
											if(tank != This) {
												if(tank != null && colision(This, (ImageView) tank)) {
													stillGoing = false;
													nodes.add(tank);
												}
											}
										}
									}
									for(Node node : nodes) {
										if(node != null) {
											((Pane) getParent()).getChildren().remove(node);
										}
									}
								}
								
								if(!stillGoing && getParent() != null) {
									((Pane) getParent()).getChildren().remove(This);
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
	
	private class Line {
		
		private Point p1;
		private Point p2;
		private boolean slope;
		private double m;
		private double b;
		
		public Line(Point p1,Point p2) {
			this.p1 = p1;
			this.p2 = p2;
			if(p1.getX() - p2.getX() == 0) {
				slope = false;
			} else {
				slope = true;
				m = (p1.getY() - p2.getY()) / (p1.getX() - p2.getX());
				b = p1.getY() - (m * p1.getX());
			}
		}
		
		public boolean contains(Point intersection) {
			double maxX = Math.max(p1.getX(), p2.getX());
			double minX = Math.min(p1.getX(), p2.getX());
			double maxY = Math.max(p1.getY(), p2.getY());
			double minY = Math.min(p1.getY(), p2.getY());
			return intersection.getX() <= maxX && intersection.getX() >= minX && intersection.getY() <= maxY && intersection.getY() >= minY;
		}

		public Point intersection(Line line) {
			Point intersection = new Point();
			if(slope) {
				if(!line.hasSlope()) {
					intersection.setX(line.getP1().getX());
					intersection.setY(m * intersection.getX() + b);
				} else if(m - line.getM() == 0) {
					if(b - line.getB() == 0) {
						//Same line
						intersection = p1;
					} else {
						return null;
					}
				} else {
					intersection.setX((line.getB() - b) / (m - line.getM()));
					intersection.setY((m * intersection.getX()) + b);
				}
			} else if(line.hasSlope()) {
				intersection.setX(p1.getX());
				intersection.setY((line.getM() * intersection.getX()) + line.getB());
			} else {
				if(p1.getY() == line.getP1().getY()) {
					//Same line
					intersection = p1;
				} else {
					return null;
				}
			}
			return intersection;
		}
		
		public boolean hasSlope() {
			return slope;
		}
		public Point getP1() {
			return p1;
		}
		
		public Point getP2() {
			return p2;
		}
		
		public double getM() {
			return m;
		}
		
		public double getB() {
			return b;
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
