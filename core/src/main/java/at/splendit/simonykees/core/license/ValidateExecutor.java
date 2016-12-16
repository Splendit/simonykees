package at.splendit.simonykees.core.license;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.Status;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.license.model.LicenseeModel;
import at.splendit.simonykees.core.license.model.SchedulerModel;

public class ValidateExecutor {
	
	private static ScheduledExecutorService scheduler;

	protected synchronized static void startSchedule(SchedulerModel se, LicenseeModel le) {
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
		
		scheduler = scheduledExecutor;
	}
	
	public synchronized static void shutDownScheduler() {
		if(scheduler != null) {
			Activator.log(Status.INFO, "Shutting down validation scheduler", null);
			scheduler.shutdown();
		}
	}
	
	public static boolean isTerminated() {
		boolean isTerminated = true;
		if(scheduler != null) {
			isTerminated = scheduler.isTerminated();
		}
		return isTerminated;
	}
	
	public static boolean isShutDown() {
		boolean isShutDown = true;
		if(scheduler != null) {
			isShutDown = scheduler.isShutdown();
		}
		return isShutDown;
	}
}
