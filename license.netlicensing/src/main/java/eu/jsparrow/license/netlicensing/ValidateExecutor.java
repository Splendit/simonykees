package eu.jsparrow.license.netlicensing;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.netlicensing.model.LicenseeModel;
import eu.jsparrow.license.netlicensing.model.SchedulerModel;

/**
 * Responsible for starting and shutting down the validate scheduler.
 * 
 * @author Ardit Ymeri, Matthias Webhofer
 * @since 1.0
 *
 */
public class ValidateExecutor {

	private static final Logger logger = LoggerFactory.getLogger(ValidateExecutor.class);
	private static ScheduledExecutorService scheduler;
	private static boolean validationAttempt = false;
	private static boolean jSparrowRunning = false;
	private static ReadWriteLock lock = new ReentrantReadWriteLock();

	private ValidateExecutor() {
		/*
		 * Hiding public constructor
		 */
	}

	protected static void startSchedule(SchedulerModel schedulingInfo, LicenseeModel licensee) {
		final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

		lock.readLock()
			.lock();
		boolean tempValidationAttempt = validationAttempt;
		boolean tempJSparrowRunning = jSparrowRunning;
		lock.readLock()
			.unlock();

		if (isShutDown() && (tempValidationAttempt || tempJSparrowRunning)) {

			scheduledExecutor.scheduleWithFixedDelay(() -> {

				lock.readLock()
					.lock();
				boolean tempValidationAttemptThread = validationAttempt;
				boolean tempJSparrowRunningThread = jSparrowRunning;
				lock.readLock()
					.unlock();

				if (schedulingInfo.getDoValidate() && (tempValidationAttemptThread || tempJSparrowRunningThread)) {

					lock.writeLock()
						.lock();
					validationAttempt = false;
					lock.writeLock()
						.unlock();

					logger.info(Messages.ValidateExecutor_validation_scheduler_started);
					LicenseValidator.doValidate(licensee);
				} else {
					logger.info(Messages.ValidateExecutor_shutting_down_validation_scheduler);
					scheduledExecutor.shutdown();
				}
			}, schedulingInfo.getInitialDelay(), schedulingInfo.getValidateInterval(), TimeUnit.SECONDS);

			lock.writeLock()
				.lock();
			scheduler = scheduledExecutor;
			lock.writeLock()
				.unlock();
		}
	}

	static synchronized void shutDownScheduler() {
		if (scheduler != null) {
			logger.info(Messages.ValidateExecutor_shutting_down_validation_scheduler);
			scheduler.shutdown();
		}
	}

	public static boolean isTerminated() {
		boolean isTerminated = true;

		lock.readLock()
			.lock();
		if (scheduler != null) {
			isTerminated = scheduler.isTerminated();
		}
		lock.readLock()
			.unlock();

		return isTerminated;
	}

	public static boolean isShutDown() {
		boolean isShutDown = true;

		lock.readLock()
			.lock();
		if (scheduler != null) {
			isShutDown = scheduler.isShutdown();
		}
		lock.readLock()
			.unlock();

		return isShutDown;
	}

	public static void validationAttempt() {
		lock.writeLock()
			.lock();
		validationAttempt = true;
		lock.writeLock()
			.unlock();
	}

	public static void setJSparrowRunning(boolean running) {
		lock.writeLock()
			.lock();
		jSparrowRunning = running;
		lock.writeLock()
			.unlock();
	}
}
