package at.splendit.simonykees.core.license;

public enum LicenseType {
	
	TRIAL,
	FLOATING,
	NODE_LOCKED;
	
	public static LicenseType fromString(String value) {
		LicenseType licenseType = TRIAL;
		
		// TODO: check if the cases really match with the returned values.
		
		switch(value.toLowerCase()) {
		case "trial":
			licenseType = TRIAL;
			break;
		case "floating":
			licenseType = FLOATING;
			break;
		case "nodelocked":
			licenseType = NODE_LOCKED;
			break;
		}
		
		return licenseType;
	}
	
}
