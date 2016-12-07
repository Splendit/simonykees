package at.splendit.simonykees.core.license;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

public interface ValidateInterface {
		
	public String generateLiceneeNumber();
	public String generateLicenseeName();
	public ValidationParameters generateValidationParameters();

}
