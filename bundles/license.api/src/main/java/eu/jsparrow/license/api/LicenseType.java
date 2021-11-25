package eu.jsparrow.license.api;

/** 
 * Available license types. A license can be of type: 
 * <ul> 
 * <li>Floating: Used for floating licences.</li>
 * <li>Node Locked: Used for node locked licenses.</li>
 * <li>Demo: Used for demo/free licenses.</li>
 * <li>None: Used for {@link LicenseModel}s where the type is not known.</li>
 * </ul>
 */
public enum LicenseType {
	FLOATING,
	NODE_LOCKED,
	PAY_PER_USE,
	DEMO,
	NONE;
}
