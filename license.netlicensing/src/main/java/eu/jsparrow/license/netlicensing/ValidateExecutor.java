package eu.jsparrow.license.netlicensing;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.netlicensing.model.LicenseeModel;
import eu.jsparrow.license.netlicensing.model.SchedulerModel;

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
					logger.info(Messages.ValidateExecutor_validation_scheduler_started);
					LicenseValidator.doValidate(licensee);
				} else {
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
