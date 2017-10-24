package eu.jsparrow.license.netlicensing;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.osgi.service.component.annotations.Component;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.license.api.LicenseValidationService;

/**
 * provides an implementation for the declarative service specified by the
 * {@link eu.jsparrow.license.api.LicenseValidationService} interface
 * 
 * @author Matthias Webhofer, Andreja Sambolec
 * @since 1.2
 */
@Component
public class NetLicensingLicenseValidationService implements LicenseValidationService {

	private static final String DATE_FORMAT_PATTERN = "MMMM dd, yyyy"; //$NON-NLS-1$

	public NetLicensingLicenseValidationService() {
	}

	@Override
	public void startValidation() {
		LicenseManager.getInstance();
	}

	@Override
	public void stopValidation() {
		LicenseManager.getInstance()
			.checkIn();
	}

	@Override
	public boolean isValid() {
		return LicenseManager.getInstance()
			.getValidationData()
			.isValid();
	}

	@Override
	public boolean isExpired() {
		LicenseStatus licenseStatus = LicenseManager.getInstance()
			.getValidationData()
			.getLicenseStatus();
		return (licenseStatus == LicenseStatus.FLOATING_EXPIRED || licenseStatus == LicenseStatus.NODE_LOCKED_EXPIRED
				|| licenseStatus == LicenseStatus.FREE_EXPIRED);
	}

	@Override
	public boolean updateLicenseeNumber(String licenseKey, String licenseName) {
		return LicenseManager.getInstance()
			.updateLicenseeNumber(licenseKey.trim(), licenseName);
	}

	@Override
	public String getDisplayableLicenseInformation() {
		StringBuilder displayableLicenseInformation = new StringBuilder();

		LicenseManager licenseManger = LicenseManager.getInstance();
		LicenseChecker licenseData = licenseManger.getValidationData();
		LicenseType licenseType = licenseData.getType();
		ZonedDateTime expireationDate = licenseData.getExpirationDate();

		if (licenseType != null && expireationDate != null) {

			displayableLicenseInformation.append(Messages.SimonykeesPreferencePageLicense_jsparrow_licensed_as);
			displayableLicenseInformation.append(licenseType.getLicenseName());

			if (LicenseType.TRY_AND_BUY != licenseType) {
				String licenseKey = licenseManger.getLicensee()
					.getLicenseeNumber();

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
		return LicenseManager.getInstance()
			.getValidationData()
			.getLicenseStatus()
			.getUserMessage();
	}

	private String extractDateFormat(ZonedDateTime date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT_PATTERN);
		return date.format(formatter);
	}

	@Override
	public boolean isFullValidLicense() {
		LicenseType licenseType = LicenseManager.getInstance()
			.getValidationData()
			.getType();
		return isValid() && (LicenseType.NODE_LOCKED == licenseType || LicenseType.FLOATING == licenseType);
	}

	@Override
	public boolean isDemoType() {
		LicenseType licenseType = LicenseManager.getInstance()
			.getValidationData()
			.getType();
		return LicenseType.TRY_AND_BUY == licenseType;
	}
}
