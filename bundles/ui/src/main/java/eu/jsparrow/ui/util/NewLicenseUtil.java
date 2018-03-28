package eu.jsparrow.ui.util;

import eu.jsparrow.license.netlicensing.cleanslate.*;
import eu.jsparrow.license.netlicensing.cleanslate.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.cleanslate.model.*;

public class NewLicenseUtil {
	
	private NewLicenseUtil instance;
	
	private LicenseService service;
	
	private LicenseValidationResult result = null;

	private NewLicenseUtil() {
		service = new NetlicensingLicenseService();
	}
	
	public NewLicenseUtil get() {
		if(this.instance == null) {
			this.instance = new NewLicenseUtil();
		}
		return this.instance;
	}
	
	public boolean isValid() {
		LicenseModel model = null;
		try {
			model = service.loadFromPersistence();
		}
		catch(PersistenceException e) {
			// TODO: Show error message to user, because this means the secure storage is broken
			// Use demo license instead. 
		}
		// First start
		if(model == null) {
			model = new LicenseModelFactory().createDemoLicenseModel();
		}
		try {
			service.saveToPersistence(model);
		} catch (PersistenceException e) {
			// How to handle this?
		}
		result = service.validateLicense(model);
		return result.getStatus().isValid();
	}
	
	public boolean isFullNetlicenseLicense() {
		LicenseModel model =  result.getModel();
		if(model instanceof DemoLicenseModel) {
			return false;
		}
		return model instanceof NetlicensingLicenseModel;
	}
}
