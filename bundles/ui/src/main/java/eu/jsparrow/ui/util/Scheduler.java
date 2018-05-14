package eu.jsparrow.ui.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

	private static final TimeUnit SCHEDULE_TIME_UNIT = TimeUnit.SECONDS;
	private static final long SCHEDULE_TIME_PERIOD = 600;
	private static final long SCHEDULE_INITIAL_DELAY = 0;

	private ScheduledExecutorService scheduledExecutor;

	private LicenseUtil licenseUtil;

	public Scheduler(LicenseUtil licenseUtil) {
		this.licenseUtil = licenseUtil;
		scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
	}

	public void start() {
		scheduledExecutor.scheduleAtFixedRate(this::runValidation, SCHEDULE_INITIAL_DELAY, SCHEDULE_TIME_PERIOD,
				SCHEDULE_TIME_UNIT);
	}

	private void runValidation() {
		licenseUtil.updateValidationResult();
	}

	public void shutDown() {
		scheduledExecutor.shutdown();
	}

}
