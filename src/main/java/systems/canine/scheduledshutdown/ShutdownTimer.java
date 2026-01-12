package systems.canine.scheduledshutdown;

public class ShutdownTimer {
	private static ShutdownTimer instance = new ShutdownTimer();
	
	private long startTime = -1;
	private long duration = -1;
	
	private ShutdownTimer() { }
	
	public void restart(int newDuration) {
		duration = newDuration;
		startTime = System.nanoTime();
		
		
	}
	
	
	
	public static ShutdownTimer getInstance() {
		return instance;
	}
}