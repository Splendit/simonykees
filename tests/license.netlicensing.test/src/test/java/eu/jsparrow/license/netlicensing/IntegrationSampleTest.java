package eu.jsparrow.license.netlicensing;

import java.time.ZonedDateTime;

import org.junit.*;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.exception.PersistenceException;

@Ignore
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
