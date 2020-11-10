package eu.jsparrow.core.statistic;

import java.time.Duration;

import org.apache.commons.lang3.time.DurationFormatUtils;

import eu.jsparrow.i18n.Messages;

public class DurationFormatUtil {

	private static final String DAYS = Messages.DurationFormatUtil_Days;

	private static final String HOURS = Messages.DurationFormatUtil_Hours;

	private static final String MINUTES = Messages.DurationFormatUtil_Minutes;

	private static final String SECONDS = Messages.DurationFormatUtil_Seconds;

	private DurationFormatUtil() {
		// Hide default constructor
	}

	/**
	 * <p>
	 * formats a {@link Duration} and presents it as a string containing days,
	 * hours and minutes, where 1 (working-)day is defined by 8 (working-)hours.
	 * </p>
	 * 
	 * <p>
	 * i.e. 1 day (24h) = 3 working-days (8h)
	 * </p>
	 * <p>
	 * i.e. 9 hours (24h) = 1 working-day 1 hour (8h)
	 * </p>
	 * 
	 * @param duration
	 * @return a string with the format "%d Days %d Hours %d Minutes" based on
	 *         an 8-hour working-day
	 */
	public static String formatTimeSaved(Duration duration) {
		/*
		 * these calculations are made manually, because a java library that
		 * handles 8 hour working days couldn't be found
		 */
		long millis = duration.toMillis();
		long minutes = (millis / (1_000 * 60)) % 60;
		long workingHours = (millis / (1000 * 60 * 60)) % 8;
		long workingDays = millis / (1000 * 60 * 60 * 8);

		String formatted = String.format("%d %s, %d %s, %d %s", workingDays, DAYS, workingHours, HOURS, minutes, //$NON-NLS-1$
				MINUTES);

		formatted = removeZeroValueTimeUnit(DAYS, formatted);
		formatted = removeZeroValueTimeUnit(HOURS, formatted);
		formatted = removeZeroValueTimeUnit(MINUTES, formatted);
		return formatted;
	}

	/**
	 * Formats the run duration of jSparrow (seen in the left corner of the summary page)
	 * 
	 * @param milliseconds
	 * @return Human-readable time String, containing hours, minutes, and seconds
	 */
	public static String formatRunDuration(long milliseconds) {
		String dateFormat = String.format("HH '%s', mm '%s', ss '%s'", HOURS, MINUTES, SECONDS); //$NON-NLS-1$
		String formatted = DurationFormatUtils.formatDuration(milliseconds, dateFormat, false);
		formatted = removeZeroValueTimeUnit(HOURS, formatted);
		formatted = removeZeroValueTimeUnit(MINUTES, formatted);
		formatted = removeZeroValueTimeUnit(SECONDS, formatted);
		return formatted;
	}

	/**
	 * Removes a leading time unit, if its value is zero
	 * 
	 * @param timeUnit
	 *            written time unit
	 * @param input
	 *            formated time String
	 * @return formated time unit where useless timeUnit was removed
	 */
	private static String removeZeroValueTimeUnit(String timeUnit, String input) {
		return input.replaceAll("(^0 " + timeUnit + ",\\s)", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
