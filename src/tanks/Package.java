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
	
	/**Sends the name because Brad used a bad constructor and I had to fix his mess*/
	public Package(String name){
		this.name = name;
	}
	
	/**Used to set up a leave message*/
	public Package(String name, boolean isLeaving){
		this.name = name;
		this.isLeaving = isLeaving;
	}
	
	public void addTankData(double rotate, double x, double y, double cannonRotate) {
		this.rotate = rotate;
		this.x = x;
		this.y = y;
		this.cannonRotate = cannonRotate;
	}
	
	public String getName() {
		return name;
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
	
	public boolean isLeaving(){
		return isLeaving;
	}

	public double getCannonRotate() {
		return cannonRotate;
	}
}
