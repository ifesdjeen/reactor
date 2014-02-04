package reactor.support;

import reactor.function.Consumer;
import reactor.timer.HashWheelTimer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Jon Brisbin
 */
public abstract class TimeUtils {

	private static final int        DEFAULT_RESOLUTION = 100;
	private static final AtomicLong now                = new AtomicLong();
	private static HashWheelTimer timer;

	protected TimeUtils() {
	}

	public static long approxCurrentTimeMillis() {
		getTimer();
		return now.get();
	}

	public static void setTimer(HashWheelTimer timer) {
		timer.schedule(new Consumer<Long>() {
			@Override
			public void accept(Long aLong) {
				now.set(System.currentTimeMillis());
			}
		}, DEFAULT_RESOLUTION, TimeUnit.MILLISECONDS, DEFAULT_RESOLUTION);
		now.set(System.currentTimeMillis());
		TimeUtils.timer = timer;
	}

	public static HashWheelTimer getTimer() {
		if(null == timer) {
			setTimer(HashWheelTimer.instance);
		}
		return timer;
	}

}
