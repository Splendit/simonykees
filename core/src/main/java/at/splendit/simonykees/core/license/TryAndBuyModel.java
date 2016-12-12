package at.splendit.simonykees.core.license;

import java.time.ZonedDateTime;

import com.labs64.netlicensing.domain.vo.ValidationParameters;

public class TryAndBuyModel extends LicenseModel {

	public TryAndBuyModel(String productModuleNumber, ZonedDateTime expireDate) {
		super(productModuleNumber, LicenseType.TRY_AND_BUY, expireDate);
	}

	@Override
	protected ValidationParameters getValidationParameters() {
		ValidationParameters validationParams = new ValidationParameters();
		return validationParams;
	}

}
