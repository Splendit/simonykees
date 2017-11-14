package eu.jsparrow.ui.util;

import org.apache.commons.lang3.time.StopWatch;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.3.0
 */
public class StopWatchUtil {

	private static StopWatch stopWatch = new StopWatch();
	private static long durationInMilliseconds = -1;

	private StopWatchUtil() {

	}

	/**
	 * resets the stop watch and starts it
	 */
	public static void start() {
		/*
		 * the stop watch has to be reset because otherwise it will throw an
		 * Exception on the next start
		 */
		reset();
		stopWatch.start();
	}

	/**
	 * stops the stop watch
	 */
	public static void stop() {
		durationInMilliseconds = stopWatch.getTime();
		stopWatch.stop();
	}

	/**
	 * resets the stop watch
	 */
	public static void reset() {
		durationInMilliseconds = -1;
		stopWatch.reset();
	}

	/**
	 * 
	 * @return duration in milliseconds
	 */
	public static long getTime() {
		return durationInMilliseconds;
	}

}
