package transportapp.co600.directionserver;

public class Life implements Runnable {

	private boolean alive;
	
	@Override
	public void run() {
		alive = true;
		while(alive)	{
			
		}
	}
	
	public boolean isAlive() {
		return alive;
	}

	public void setAlive(boolean alive) {
		this.alive = alive;
	}
}
