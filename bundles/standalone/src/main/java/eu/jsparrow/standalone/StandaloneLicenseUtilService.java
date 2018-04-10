package eu.jsparrow.standalone;

public interface StandaloneLicenseUtilService {
	boolean validate(String key);

	void stop();
}
