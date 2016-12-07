package at.splendit.simonykees.core.License;

public abstract class LicenseEntity {
	
	private LicenseStatus status;
	private LicenseType type;
	private Long expireDate;
	public LicenseEntity(LicenseStatus status, LicenseType type, Long expireDate) {
		super();
		this.status = status;
		this.type = type;
		this.expireDate = expireDate;
	}
	public LicenseStatus getStatus() {
		return status;
	}
	public void setStatus(LicenseStatus status) {
		this.status = status;
	}
	public LicenseType getType() {
		return type;
	}
	public void setType(LicenseType type) {
		this.type = type;
	}
	public Long getExpireDate() {
		return expireDate;
	}
	public void setExpireDate(Long expireDate) {
		this.expireDate = expireDate;
	}
	
	

}
