package eu.jsparrow.ui.util;

import eu.jsparrow.license.netlicensing.cleanslate.*;
import eu.jsparrow.license.netlicensing.cleanslate.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.cleanslate.model.*;
import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;

public class NewLicenseUtil {

	private NewLicenseUtil instance;

	private LicenseService service;

	private LicenseValidationResult result = null;

	private NewLicenseUtil() {
		service = new NetlicensingLicenseService();
	}

	public NewLicenseUtil get() {
		if (this.instance == null) {
			this.instance = new NewLicenseUtil();
		}
		return this.instance;
	}

	public boolean isValid() {
		LicenseModel model = null;
		try {
			model = service.loadFromPersistence();
		} catch (PersistenceException e) {
			// TODO: Show error message to user, because this means the secure
			// storage is broken
			// Use demo license instead.
			return false;
		}
		result = service.validateLicense(model);
		return result.getStatus()
			.isValid();
	}

	public boolean isFullNetlicenseLicense() {
		LicenseModel model = result.getModel();
		if (model instanceof DemoLicenseModel) {
			return false;
		}
		return model instanceof NetlicensingLicenseModel;
	}
	
	public void update(String key) {
		String secret = createSecretFromHardware();
		LicenseModel model = new LicenseModelFactory().createNewNodeLockedModel(key, secret);
		LicenseValidationResult validationResult = service.validateLicense(model);
		
		if(validationResult.getStatus().isValid()) {
			try {
				service.saveToPersistence(validationResult.getModel());
			}
			catch(PersistenceException e) {
				// TODO: Show bad bad error message, because we could not save the license!
				
			}
		}
		else {
			// TODO: Display that we did not update the license, because its not valid and such.
		}
	}
	
	private String createSecretFromHardware() {

			String diskSerial = ""; //$NON-NLS-1$
			SystemInfo systemInfo = new SystemInfo();

			HardwareAbstractionLayer hal = systemInfo.getHardware();
			HWDiskStore[] diskStores = hal.getDiskStores();

			if (diskStores.length > 0) {
				diskSerial = diskStores[0].getSerial();
			}

			return diskSerial;
	}
}
