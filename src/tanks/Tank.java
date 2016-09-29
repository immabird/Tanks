package tanks;

public class Tank {
	private String name;
	
	public Tank(String tanksName, String data) {
		name = tanksName;
		setPosition(data);
	}
	
	public String getName() {
		return name;
	}
	
	public void setPosition(String data) {
		String[] pos = data.split(",");
		
	}
	
	public void delete() {
		
	}
}
