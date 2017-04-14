package at.splendit.simonykees.license;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.i18n.Messages;
import at.splendit.simonykees.license.model.LicenseeModel;
import at.splendit.simonykees.license.model.SchedulerModel;

/**
 * Responsible for starting and shutting down the validate scheduler.
 *  
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class ValidateExecutor {

	private static final Logger logger = LoggerFactory.getLogger(ValidateExecutor.class);
	
	private static ScheduledExecutorService scheduler;

	protected synchronized static void startSchedule(SchedulerModel schedulingInfo, LicenseeModel licensee) {
		final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

		scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				if (schedulingInfo.getDoValidate()) {
					//Activator.log(Status.INFO, Messages.ValidateExecutor_validation_scheduler_started, null);
					logger.info(Messages.ValidateExecutor_validation_scheduler_started);
					LicenseValidator.doValidate(licensee);
				} else {
					//Activator.log(Status.INFO, Messages.ValidateExecutor_shutting_down_validation_scheduler, null);
					logger.info(Messages.ValidateExecutor_shutting_down_validation_scheduler);
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
			//Activator.log(Messages.ValidateExecutor_shutting_down_validation_scheduler);
			logger.info(Messages.ValidateExecutor_shutting_down_validation_scheduler);
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
