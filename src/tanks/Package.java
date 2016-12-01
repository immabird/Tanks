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
	
	/**Used to set up a leave message*/
	public Package(String name){
		this.name = name;
	}
	
	public void addTankData(double rotate, double x, double y) {
		this.rotate = rotate;
		this.x = x;
		this.y = y;
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
}
