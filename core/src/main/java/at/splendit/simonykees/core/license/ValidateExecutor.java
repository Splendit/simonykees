package at.splendit.simonykees.core.license;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.Status;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.license.model.LicenseeModel;
import at.splendit.simonykees.core.license.model.SchedulerModel;

public class ValidateExecutor {

	private static ScheduledExecutorService scheduler;

	protected synchronized static void startSchedule(SchedulerModel schedulingInfo, LicenseeModel licensee) {
		final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

		scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				if (schedulingInfo.getDoValidate()) {
					Activator.log(Status.INFO, Messages.ValidateExecutor_validation_scheduler_started, null);
					LicenseValidator.doValidate(licensee);
				} else {
					Activator.log(Status.INFO, Messages.ValidateExecutor_shutting_down_validation_scheduler, null);
					scheduledExecutor.shutdown();
				}
			}
		}, 
				schedulingInfo.getInitialDelay(), 
				schedulingInfo.getValidateInterval(), 
				TimeUnit.SECONDS);

		scheduler = scheduledExecutor;
	}

	synchronized static void shutDownScheduler() {
		if (scheduler != null) {
			Activator.log(Messages.ValidateExecutor_shutting_down_validation_scheduler);
			scheduler.shutdown();
		}
	}

	public static boolean isTerminated() {
		boolean isTerminated = true;
		if (scheduler != null) {
			isTerminated = scheduler.isTerminated();
		}
		return isTerminated;
	}

	public static boolean isShutDown() {
		boolean isShutDown = true;
		if (scheduler != null) {
			isShutDown = scheduler.isShutdown();
		}
		return isShutDown;
	}
}
