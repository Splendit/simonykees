package eu.jsparrow.license.netlicensing;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.ZonedDateTime;

import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.LicenseValidationResult;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.api.exception.ValidationException;
import eu.jsparrow.license.netlicensing.model.NetlicensingLicenseModel;

import eu.jsparrow.license.netlicensing.validation.impl.NetlicensingLicenseCache;

public class IntegrationSampleTest {

	private NetlicensingLicensePersistenceService persistenceService;
	
	@Before
	public void setUp() {
		persistenceService = new NetlicensingLicensePersistenceService();
	}
	
	@Test
	public void whenSaving_expiredDemoLicense() throws PersistenceException {
		LicenseModel model = new NetlicensingLicenseModelFactoryService().createDemoLicenseModel(ZonedDateTime.now().minusDays(5));
		persistenceService.saveToPersistence(model);
		
	}
	
	@Test
	public void whenSaving_validDemoLicense() throws PersistenceException {
		LicenseModel model = new NetlicensingLicenseModelFactoryService().createDemoLicenseModel(ZonedDateTime.now().plusDays(5));
		persistenceService.saveToPersistence(model);
	}
}
