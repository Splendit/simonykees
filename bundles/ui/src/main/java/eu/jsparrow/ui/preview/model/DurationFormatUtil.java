package eu.jsparrow.ui.preview.model;

import java.time.Duration;

import org.apache.commons.lang3.time.DurationFormatUtils;

import eu.jsparrow.i18n.Messages;

public class DurationFormatUtil {
	
	private DurationFormatUtil() {
		//Hide default constructor
	}

	private static final String DAYS = Messages.DurationFormatUtil_Days;

	private static final String HOURS = Messages.DurationFormatUtil_Hours;

	private static final String MINUTES = Messages.DurationFormatUtil_Minutes;

	private static final String SECONDS = Messages.DurationFormatUtil_Seconds;
	
	public static String formatTimeSaved(Duration duration) {
		String dateFormat = String.format("dd '%s' HH '%s' mm '%s'", DAYS, HOURS, MINUTES); //$NON-NLS-1$
		String formatted = DurationFormatUtils.formatDuration(duration.toMillis(), dateFormat, false);
		formatted = removeZeroValueTimeUnit(DAYS, formatted);
		formatted = removeZeroValueTimeUnit(HOURS, formatted);
		formatted = removeZeroValueTimeUnit(MINUTES, formatted);
		return formatted;
	}

	public static String formatRunDuration(long milliseconds) {
		String dateFormat = String.format("HH '%s' mm '%s' ss '%s'", HOURS, MINUTES, SECONDS); //$NON-NLS-1$
		String formatted = DurationFormatUtils.formatDuration(milliseconds, dateFormat, false);
		formatted = removeZeroValueTimeUnit(HOURS, formatted);
		formatted = removeZeroValueTimeUnit(MINUTES, formatted);
		formatted = removeZeroValueTimeUnit(SECONDS, formatted);
		return String.format(Messages.DurationFormatUtil_RunDuration, formatted);
	}

	/**
	 * Removes a leading time unit, if its value is zero
	 * @param timeUnit written time unit
	 * @param input formated time String
	 * @return formated time unit where useless timeUnit was removed
	 */
	private static String removeZeroValueTimeUnit(String timeUnit, String input) {
		return input.replaceAll("(^0 "+timeUnit+"\\s)", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
