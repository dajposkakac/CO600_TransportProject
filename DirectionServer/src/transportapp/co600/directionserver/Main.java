package transportapp.co600.directionserver;

public class Main {
	
	public Main() {
		Life l = new Life();
		Thread t1 = new Thread(l);
		t1.start();
		Server server = new Server();
	}

	
	public static void main(String[] args) {
		try {
			new Main();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
