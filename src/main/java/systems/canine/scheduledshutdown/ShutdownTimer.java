package systems.canine.scheduledshutdown;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class ShutdownTimer {	
	private static ShutdownTimer instance = new ShutdownTimer();
	
	public static final Logger LOGGER = LogUtils.getLogger();
	
	private final long NANOS_PER_MIN = 60_000_000_000l;
	
	private long startTime = -1;
	private long endTime = -1;
	private long minsTotal = -1;
	private long minsElapsed = -1;
	
	private ShutdownTimer() { }
	
	public void restart(int minutes) {
		startTime = System.nanoTime();
		endTime = startTime + (minutes * NANOS_PER_MIN);
		minsTotal = minutes;
		minsElapsed = 0;
	}
	
	public void stop() {
		// TODO
	}
	
	private long nanosToMins(long nanos) {
		return Math.ceilDiv(nanos, NANOS_PER_MIN);
	}
	
	@SubscribeEvent
	public void onTick(ServerTickEvent.Post event) {
		if (startTime == -1) {
			return;
		}
		
		long minsLeft = nanosToMins(endTime - System.nanoTime());
		if (minsLeft == 0) {
			LOGGER.info("IT IS TIME");
			return;
		}
		
		long possibleMinsElapsed = minsTotal - minsLeft;
		if (possibleMinsElapsed > minsElapsed) {
			minsElapsed = possibleMinsElapsed;
			LOGGER.info("A MINUTE HAS PASSED");
			return;
		}
	}
	
	public static ShutdownTimer getInstance() {
		return instance;
	}
}