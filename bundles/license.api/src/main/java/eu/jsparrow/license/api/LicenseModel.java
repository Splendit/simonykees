package eu.jsparrow.license.api;

import java.io.Serializable;
import java.time.ZonedDateTime;

public interface LicenseModel extends Serializable {
	
	public ZonedDateTime getExpirationDate();
	
	public LicenseType getType();
}
