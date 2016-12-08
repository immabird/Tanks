package tanks;

import java.io.Serializable;

public class Package implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7693361748336431191L;
	
	// Tank data
	private String name = "";
	private double rotate = 0;
	private double x = 0;
	private double y = 0;
	private double cannonRotate = 0;
	private boolean isLeaving = false;
	private boolean newName = false;
	private boolean bulletShot = false;
	private boolean isDead = false;
	private String color = "";
	
	/**Sends the name because Brad used a bad constructor and I had to fix his mess*/
	public Package(String name){
		this.name = name;
	}
	
	/**Used to set up a leave message*/
	public Package(String name, boolean isLeaving){
		this.name = name;
		this.isLeaving = isLeaving;
	}
	
	public Package(boolean newName){
		this.newName = newName;
	}
	
	
	public void addTankData(double rotate, double x, double y, double cannonRotate, String color) {
		this.color = color;
		this.rotate = rotate;
		this.x = x;
		this.y = y;
		this.cannonRotate = cannonRotate;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setNewName(boolean newName){
		this.newName = newName;
	}
	
	public void setBulletShot(boolean bulletShot) {
		this.bulletShot = bulletShot;
	}
	
	public boolean getNewName(){
		return newName;
	}
	
	public double getRotate() {
		return rotate;
	}
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}
	
	public boolean bulletShot() {
		return bulletShot;
	}
	
	public boolean isLeaving(){
		return isLeaving;
	}

	public double getCannonRotate() {
		return cannonRotate;
	}

	public void setIsDead() {
		isDead = true;
	}
}
