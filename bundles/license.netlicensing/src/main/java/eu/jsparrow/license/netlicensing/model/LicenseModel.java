package eu.jsparrow.license.netlicensing.model;

import java.io.Serializable;
import java.time.ZonedDateTime;

public interface LicenseModel extends Serializable {
	
	public ZonedDateTime getExpirationDate();

}
