package tanks;

public class Package {
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
