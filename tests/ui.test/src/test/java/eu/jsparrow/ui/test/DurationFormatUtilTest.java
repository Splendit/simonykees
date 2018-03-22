package eu.jsparrow.ui.test;

import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

import eu.jsparrow.ui.preview.model.DurationFormatUtil;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.5.0
 */
public class DurationFormatUtilTest {

	@Test
	public void formatTimeSaved_1600minutes() {
		Duration duration = Duration.of(1600, ChronoUnit.MINUTES);
		String expectedResult = "3 Days 2 Hours 40 Minutes"; //$NON-NLS-1$

		String formatted = DurationFormatUtil.formatTimeSaved(duration);

		assertEquals(expectedResult, formatted);
	}

	@Test
	public void formatTimeSaved_exactlyOneWorkingDay() {
		Duration duration = Duration.of(480, ChronoUnit.MINUTES);
		String expectedResult = "1 Days 0 Hours 0 Minutes"; //$NON-NLS-1$

		String formatted = DurationFormatUtil.formatTimeSaved(duration);

		assertEquals(expectedResult, formatted);
	}

	@Test
	public void formatTimeSaved_oneWrokingDayAndOneMinute() {
		Duration duration = Duration.of(481, ChronoUnit.MINUTES);
		String expectedResult = "1 Days 0 Hours 1 Minutes"; //$NON-NLS-1$

		String formatted = DurationFormatUtil.formatTimeSaved(duration);

		assertEquals(expectedResult, formatted);
	}

	@Test
	public void formatTimeSaved_oneMinuteLessThanOneWorkingDay() {
		Duration duration = Duration.of(479, ChronoUnit.MINUTES);
		String expectedResult = "7 Hours 59 Minutes"; //$NON-NLS-1$

		String formatted = DurationFormatUtil.formatTimeSaved(duration);

		assertEquals(expectedResult, formatted);
	}
}
