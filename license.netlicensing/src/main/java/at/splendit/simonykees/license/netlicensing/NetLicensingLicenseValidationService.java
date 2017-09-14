package at.splendit.simonykees.license.netlicensing;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.osgi.service.component.annotations.Component;

import at.splendit.simonykees.i18n.Messages;
import at.splendit.simonykees.license.api.LicenseValidationService;

/**
 * provides an implementation for the declarative service specified by the
 * {@link at.splendit.simonykees.license.api.LicenseValidationService} interface
 * 
 * @author Matthias Webhofer, Andreja Sambolec
 * @since 1.2
 */
@Component
public class NetLicensingLicenseValidationService implements LicenseValidationService {

	private static final String DATE_FORMAT_PATTERN = "MMMM dd, yyyy"; //$NON-NLS-1$

	public NetLicensingLicenseValidationService() {
		// TODO Auto-generated constructor stub
	}

	public void startValidation() {
		LicenseManager.getInstance();
	}

	@Override
	public void stopValidation() {
		LicenseManager.getInstance().checkIn();
	}

	@Override
	public boolean isValid() {
		return LicenseManager.getInstance().getValidationData().isValid();
	}

	@Override
	public boolean isExpired() {
		LicenseStatus licenseStatus = LicenseManager.getInstance().getValidationData().getLicenseStatus();
		return (licenseStatus.equals(LicenseStatus.FLOATING_EXPIRED)
				|| licenseStatus.equals(LicenseStatus.NODE_LOCKED_EXPIRED)
				|| licenseStatus.equals(LicenseStatus.TRIAL_EXPIRED));
	}

	@Override
	public boolean updateLicenseeNumber(String licenseKey, String licenseName) {
		return LicenseManager.getInstance().updateLicenseeNumber(licenseKey.trim(), licenseName);
	}

	@Override
	public String getDisplayableLicenseInformation() {
		StringBuffer displayableLicenseInformation = new StringBuffer();

		LicenseManager licenseManger = LicenseManager.getInstance();
		LicenseChecker licenseData = licenseManger.getValidationData();
		LicenseType licenseType = licenseData.getType();
		ZonedDateTime expireationDate = licenseData.getExpirationDate();

		if (licenseType != null && expireationDate != null) {

			displayableLicenseInformation.append(Messages.SimonykeesPreferencePageLicense_jsparrow_licensed_as);
			displayableLicenseInformation.append(licenseType.getLicenseName());

			if (!LicenseType.TRY_AND_BUY.equals(licenseType)) {
				String licenseKey = licenseManger.getLicensee().getLicenseeNumber();

				displayableLicenseInformation.append(" "); //$NON-NLS-1$
				displayableLicenseInformation.append(Messages.SimonykeesPreferencePageLicense_under_key_label);
				displayableLicenseInformation.append(" "); //$NON-NLS-1$
				displayableLicenseInformation.append(licenseKey);
				displayableLicenseInformation.append("."); //$NON-NLS-1$

			} else {
				displayableLicenseInformation.append("."); //$NON-NLS-1$
			}

			displayableLicenseInformation.append(" "); //$NON-NLS-1$
			displayableLicenseInformation.append(Messages.SimonykeesPreferencePageLicense_jsparrow_valid_until);
			displayableLicenseInformation.append(extractDateFormat(expireationDate));
			displayableLicenseInformation.append("."); //$NON-NLS-1$
		}

		return displayableLicenseInformation.toString();
	}

	@Override
	public String getLicenseStautsUserMessage() {
		return LicenseManager.getInstance().getValidationData().getLicenseStatus().getUserMessage();
	}

	private String extractDateFormat(ZonedDateTime date) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
		String strDate = date.format(formatter);

		return strDate;
	}

	@Override
	public boolean isDemoType() {
		LicenseType licenseType = LicenseManager.getInstance().getValidationData().getType();
		if (LicenseType.TRY_AND_BUY.equals(licenseType)) {
			return true;
		}
		return false;
	}
}
