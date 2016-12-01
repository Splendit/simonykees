package at.splendit.simonykees.core.License;

public class SchedulerEntity {
	
	private long validateInterval;
	private boolean doValidate;

	
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
		
	
}
