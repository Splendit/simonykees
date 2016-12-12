package at.splendit.simonykees.core.license;

public enum LicenseType {
	
	TRY_AND_BUY,
	FLOATING,
	NODE_LOCKED;
	
	public static LicenseType fromString(String value) {
		LicenseType licenseType = TRY_AND_BUY;
		
		// TODO: check if the cases really match with the returned values.
		
		switch(value.toLowerCase()) {
		case "tryandbuy":
			licenseType = TRY_AND_BUY;
			break;
		case "floating":
			licenseType = FLOATING;
			break;
		case "multifeature":
			licenseType = NODE_LOCKED;
			break;
		}
		
		return licenseType;
	}
	
}
