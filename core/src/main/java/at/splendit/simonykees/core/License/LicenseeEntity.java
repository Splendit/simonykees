package at.splendit.simonykees.core.License;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

public class LicenseeEntity {
	public static final String PRODUCT_NUMBER = "test-01";

	private String LicenseeName;
	private String LicenseeNumber;
	private ValidationParameters LicenseeVAlidationParam;

	public LicenseeEntity(String licenseeName, String licenseeNumber, ValidationParameters licenseeVAlidationParam) {
		super();
		LicenseeName = licenseeName;
		LicenseeNumber = licenseeNumber;
		LicenseeVAlidationParam = licenseeVAlidationParam;
	}

	public String getLicenseeName() {
		return LicenseeName;
	}

	public void setLicenseeName(String licenseeName) {
		LicenseeName = licenseeName;
	}

	public String getLicenseeNumber() {
		return LicenseeNumber;
	}

	public void setLicenseeNumber(String licenseeNumber) {
		LicenseeNumber = licenseeNumber;
	}

	public ValidationParameters getLicenseeVAlidationParam() {
		return LicenseeVAlidationParam;
	}

	public void setLicenseeVAlidationParam(ValidationParameters licenseeVAlidationParam) {
		LicenseeVAlidationParam = licenseeVAlidationParam;
	}
	

}
