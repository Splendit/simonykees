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

	private LicenseManager licenseManager;
	
	public NetLicensingLicenseValidationService() {
		this.licenseManager = LicenseManager.getInstance();
	}
	
	// Only required to inject mocks
	void setLicenseManager(LicenseManager licenseManager) {
		this.licenseManager = licenseManager;
	}

	@Override
	public void startValidation() {
		if(!LicenseManager.isRunning()) {
			licenseManager.initManager();
		}
	}

	@Override
	public void stopValidation() {
		licenseManager.checkIn();
	}

	@Override
	public boolean isValid() {
		return licenseManager
			.getValidationData()
			.isValid();
	}

	@Override
	public boolean isExpired() {
		LicenseStatus licenseStatus = licenseManager
			.getValidationData()
			.getLicenseStatus();
		return (licenseStatus == LicenseStatus.FLOATING_EXPIRED || licenseStatus == LicenseStatus.NODE_LOCKED_EXPIRED
				|| licenseStatus == LicenseStatus.FREE_EXPIRED);
	}

	@Override
	public boolean updateLicenseeNumber(String licenseKey, String licenseName) {
		return licenseManager
			.updateLicenseeNumber(licenseKey.trim(), licenseName);
	}

	@Override
	public String getDisplayableLicenseInformation() {
		StringBuilder displayableLicenseInformation = new StringBuilder();

		LicenseChecker licenseData = licenseManager.getValidationData();
		LicenseType licenseType = licenseData.getType();
		ZonedDateTime expireationDate = licenseData.getExpirationDate();

		if (licenseType != null && expireationDate != null) {

			displayableLicenseInformation.append(Messages.SimonykeesPreferencePageLicense_jsparrow_licensed_as);
			displayableLicenseInformation.append(licenseType.getLicenseName());

			if (LicenseType.TRY_AND_BUY != licenseType) {
				String licenseKey = licenseManager.getLicensee()
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
	public String getLicenseStatusUserMessage() {
		return licenseManager
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
		LicenseType licenseType = licenseManager
			.getValidationData()
			.getType();
		return isValid() && (LicenseType.NODE_LOCKED == licenseType || LicenseType.FLOATING == licenseType);
	}

	@Override
	public boolean isDemoType() {
		LicenseType licenseType = licenseManager
			.getValidationData()
			.getType();
		return LicenseType.TRY_AND_BUY == licenseType;
	}

	@Override
	public void setJSparrowRunning(boolean running) {
		LicenseManager.setJSparrowRunning(running);
	}
}
