package tanks;

import java.io.Serializable;

public class Package implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7693361748336431191L;
	private Tank tank;
	private String name;
	
	public Package(Tank t){
		setTank(t);
	}
	
	public void setTank(Tank aTank) {
		tank = aTank;
		name = tank.getName();
	}
	
	public Tank getTank() {
		return tank;
	}
	
	
	public String getName() {
		return name;
	}
}
