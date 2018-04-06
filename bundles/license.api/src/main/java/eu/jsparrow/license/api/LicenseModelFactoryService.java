package eu.jsparrow.license.api;

import java.time.ZonedDateTime;

public interface LicenseModelFactoryService {
	public LicenseModel createDemoLicenseModel();

	public LicenseModel createDemoLicenseModel(ZonedDateTime expirationDate);

	public LicenseModel createNewNodeLockedModel(String key, String secret);

	public LicenseModel createNewFloatingModel(String key, String secret);

	public LicenseModel createNewModel(LicenseType type, String key, String name,
			String secret, ZonedDateTime expireDate);
}
