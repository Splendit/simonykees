package at.splendit.simonykees.core.License;

public interface LicenseChecker {
	
	public LicenseType getType();
	public LicenseStatus getStatus();
	public Long getExpireDate();
	

}
