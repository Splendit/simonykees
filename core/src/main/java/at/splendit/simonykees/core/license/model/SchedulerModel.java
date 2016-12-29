package at.splendit.simonykees.core.license.model;

public class SchedulerModel {

	private long validateInterval;
	private boolean doValidate;
	private long initialDelay;

	public SchedulerModel(long validateInternval, long initialDelay, boolean doValidate) {
		setValidateInterval(validateInternval);
		setDoValidate(doValidate);
		setInitialDelay(initialDelay);
	}

	public long getValidateInterval() {
		return validateInterval;
	}

	public void setValidateInterval(long validateInterval) {
		this.validateInterval = validateInterval;
	}

	public boolean getDoValidate() {
		return doValidate;
	}

	public void setDoValidate(boolean doValidate) {
		this.doValidate = doValidate;
	}

	public long getInitialDelay() {
		return initialDelay;
	}

	private void setInitialDelay(long initialDelay) {
		this.initialDelay = initialDelay;
	}

}
