package at.splendit.simonykees.core.License;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ValidateExecutor {

	public static void main(String[] arg0) {
		SchedulerEntity se = new SchedulerEntity();
		LicenseeEntity le = new LicenseeEntity("will be Implemented", "will be Implemented");
		se.setDoValidate(true);
		se.setValidateInterval(5);
		startSchedule(se, le);

	}

	protected synchronized static void startSchedule(SchedulerEntity se, LicenseeEntity le) {
		final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

		scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				if (se.getDoValidate()) {
					LicenseValidator.doValidate(le);
					System.out.println("start Validate");
				} else {
					System.out.println("shutDownValidate");
					scheduledExecutor.shutdown();

				}

			}
		}, 0, se.getValidateInterval(), TimeUnit.SECONDS);

	}
}
