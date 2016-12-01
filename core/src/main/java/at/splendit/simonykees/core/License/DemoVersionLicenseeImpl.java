package at.splendit.simonykees.core.License;

import java.math.BigInteger;
import java.security.SecureRandom;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

public class DemoVersionLicenseeImpl implements ValidateInterface {

	@Override
	public String generateLiceneeNumber() {

		return new SecureRandomGenerator().getNumber();
	}

	@Override
	public String generateLicenseeName() {
		return new SecureRandomGenerator().getNumber();
	}

	@Override
	public ValidationParameters generateValidationParameters() {
		
		return new ValidationParameters();
	}

	private final class SecureRandomGenerator {

		private SecureRandom random = new SecureRandom();

		public String getNumber() {
			return new BigInteger(130, random).toString(32);
		}

	}

}