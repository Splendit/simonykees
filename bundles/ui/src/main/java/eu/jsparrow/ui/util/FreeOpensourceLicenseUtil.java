package eu.jsparrow.ui.util;

import java.time.ZonedDateTime;

import org.eclipse.swt.widgets.Shell;

import eu.jsparrow.license.api.LicenseType;
import eu.jsparrow.license.api.LicenseValidationResult;

public class FreeOpensourceLicenseUtil implements LicenseUtilService {

	@Override
	public boolean checkAtStartUp(Shell shell) {
		return true;
	}

	@Override
	public boolean isFreeLicense() {
		return false;
	}

	@Override
	public LicenseUpdateResult update(String key) {
		LicenseUpdateResult licenseUpdateResult = new LicenseUpdateResult(true, "Free Opensource License");
		return licenseUpdateResult;
	}

	@Override
	public void stop() {

		
	}

	@Override
	public LicenseValidationResult getValidationResult() {
		LicenseValidationResult result = new LicenseValidationResult(
				LicenseType.FLOATING,
				"jSparrow Open Source License",
				true,
				"Open Source License",
				ZonedDateTime.now().plusYears(100)
				);
		return result;
	}

	@Override
	public void reserveQuantity(int credit) {
		
	}

	@Override
	public void updateValidationResult() {
		
	}

	@Override
	public boolean isProLicense() {
		return true;
	}

}
