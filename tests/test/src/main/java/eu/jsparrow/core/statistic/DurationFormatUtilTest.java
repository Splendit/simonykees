package eu.jsparrow.core.statistic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.5.0
 */
class DurationFormatUtilTest {

	@Test
	void formatTimeSaved_1600minutes() {
		Duration duration = Duration.of(1600, ChronoUnit.MINUTES);
		String expectedResult = "3 Days, 2 Hours, 40 Minutes";

		String formatted = DurationFormatUtil.formatTimeSaved(duration);

		assertEquals(expectedResult, formatted);
	}

	@Test
	void formatTimeSaved_exactlyOneWorkingDay() {
		Duration duration = Duration.of(480, ChronoUnit.MINUTES);
		String expectedResult = "1 Days, 0 Hours, 0 Minutes";

		String formatted = DurationFormatUtil.formatTimeSaved(duration);

		assertEquals(expectedResult, formatted);
	}

	@Test
	void formatTimeSaved_oneWrokingDayAndOneMinute() {
		Duration duration = Duration.of(481, ChronoUnit.MINUTES);
		String expectedResult = "1 Days, 0 Hours, 1 Minutes";

		String formatted = DurationFormatUtil.formatTimeSaved(duration);

		assertEquals(expectedResult, formatted);
	}

	@Test
	void formatTimeSaved_oneMinuteLessThanOneWorkingDay() {
		Duration duration = Duration.of(479, ChronoUnit.MINUTES);
		String expectedResult = "7 Hours, 59 Minutes";

		String formatted = DurationFormatUtil.formatTimeSaved(duration);

		assertEquals(expectedResult, formatted);
	}

	@Test
	void formatTimeSaved_oneHour() {
		Duration duration = Duration.of(60, ChronoUnit.MINUTES);
		String expectedResult = "1 Hours, 0 Minutes";

		String formatted = DurationFormatUtil.formatTimeSaved(duration);

		assertEquals(expectedResult, formatted);
	}

	@Test
	void formatTimeSaved_oneMinuteLessThanOneHour() {
		Duration duration = Duration.of(59, ChronoUnit.MINUTES);
		String expectedResult = "59 Minutes";

		String formatted = DurationFormatUtil.formatTimeSaved(duration);

		assertEquals(expectedResult, formatted);
	}
	
	@Test
	void formatTimeSaved_zeroSeconds() {
		Duration duration = Duration.of(0, ChronoUnit.SECONDS);
		String expectedResult = "0 Minutes";

		String formatted = DurationFormatUtil.formatTimeSaved(duration);

		assertEquals(expectedResult, formatted);
	}

	@Test
	void formatRunDuration_25hours() {
		long duration = Duration.of(25, ChronoUnit.HOURS).toMillis();
		String expectedResult = "25 Hours, 0 Minutes, 0 Seconds";
		
		String formatted = DurationFormatUtil.formatRunDuration(duration);
		
		assertEquals(expectedResult, formatted);
	}
	
	@Test
	void formatRunDuration_oneMinuteLessThanOneHour() {
		long duration = Duration.of(59, ChronoUnit.MINUTES).toMillis();
		String expectedResult = "59 Minutes, 0 Seconds";
		
		String formatted = DurationFormatUtil.formatRunDuration(duration);
		
		assertEquals(expectedResult, formatted);
	}
	
	@Test
	void formatRunDuration_oneSecondLessThanOneMinute() {
		long duration = Duration.of(59, ChronoUnit.SECONDS).toMillis();
		String expectedResult = "59 Seconds";
		
		String formatted = DurationFormatUtil.formatRunDuration(duration);
		
		assertEquals(expectedResult, formatted);
	}
	
	@Test
	void formatRunDuration_zeroSeconds() {
		long duration = 0;
		String expectedResult = "0 Seconds";
		
		String formatted = DurationFormatUtil.formatRunDuration(duration);
		
		assertEquals(expectedResult, formatted);
	}
}
