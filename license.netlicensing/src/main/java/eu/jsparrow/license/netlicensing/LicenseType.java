package eu.jsparrow.license.netlicensing;

import eu.jsparrow.i18n.Messages;

/**
 * Enumeration of the license models that a client can have.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public enum LicenseType {

	TRY_AND_BUY(Messages.LicenseType_try_and_buy),
	FLOATING(Messages.LicenseType_floating),
	NODE_LOCKED(Messages.LicenseType_node_locked),
	SUBSCRIPTION(Messages.LicenseType_subscription);

	@SuppressWarnings("nls")
	public static LicenseType fromString(String value) {
		LicenseType licenseType = TRY_AND_BUY;

		if (value != null) {
			switch (value.toLowerCase()) {
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
			default:
				licenseType = TRY_AND_BUY;
			}
		}

		return licenseType;
	}

	@SuppressWarnings("nls")
	@Override
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

	private LicenseType(String licenseName) {
		this.licenseName = licenseName;
	}

	private String licenseName;

	public String getLicenseName() {
		return licenseName;
	}
}
