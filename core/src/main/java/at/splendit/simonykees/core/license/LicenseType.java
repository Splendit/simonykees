package at.splendit.simonykees.core.license;

public enum LicenseType {
	
	TRY_AND_BUY,
	FLOATING,
	NODE_LOCKED, 
	SUBSCRIPTION;
	
	@SuppressWarnings("nls")
	public static LicenseType fromString(String value) {
		LicenseType licenseType = TRY_AND_BUY;
		
		if(value != null) {
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
			case "subscription":
				licenseType = SUBSCRIPTION;
				break;
			}
		}
		
		return licenseType;
	}
	
	@SuppressWarnings("nls")
	public String toString() {
		switch (this) {
		case TRY_AND_BUY:
			return "TryAndBuy";
		case FLOATING:
			return "Floating";
		case NODE_LOCKED:
			return "MultiFeature";
		case SUBSCRIPTION:
			return "Subscription";

		default:
			return "";
		}
	}
	
}
