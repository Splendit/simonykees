package at.splendit.simonykees.core.License;

public class LicenseCheckerFactory {

	private static LicenseCheckerImpl instance;

	private LicenseCheckerFactory() {

	}

	public synchronized static LicenseCheckerImpl getInstance() {

		if (instance == null) {
			instance = new LicenseCheckerImpl();
		}
		return instance;
	}

}
