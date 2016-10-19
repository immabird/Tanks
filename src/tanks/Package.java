package tanks;

public class Package {
	
	
	private Tank tank;
	public void setTank(Tank aTank) {
		tank = aTank;
		name = tank.getName();
	}
	public Tank getTank() {
		return tank;
	}
	
	private String name = "No Name";
	public String getName() {
		return name;
	}
}
