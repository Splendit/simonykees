package at.splendit.simonykees.core.license;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.Status;

import at.splendit.simonykees.core.Activator;

public class ValidateExecutor {

	protected synchronized static void startSchedule(SchedulerEntity se, LicenseeEntity le) {
		final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

		scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				if (se.getDoValidate()) {
					Activator.log(Status.INFO, "Validation scheduler started", null);
					LicenseValidator.doValidate(le);
				} else {
					Activator.log(Status.INFO, "Shutting down validation scheduler", null);
					scheduledExecutor.shutdown();
				}

			}
		}, 0, se.getValidateInterval(), TimeUnit.SECONDS);

	}
}
