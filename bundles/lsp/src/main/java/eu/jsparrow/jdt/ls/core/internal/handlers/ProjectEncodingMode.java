package eu.jsparrow.jdt.ls.core.internal.handlers;


public enum ProjectEncodingMode {
	IGNORE, /* Ignore project encoding settings */
	WARNING, /* Show warning if a project has no explicit encoding set */
	SETDEFAULT; /* Set the default workspace encoding settings */

	public static ProjectEncodingMode fromString(String value, ProjectEncodingMode defaultMode) {
		if (value != null) {
			String val = value.toUpperCase();
			try {
				return valueOf(val);
			} catch (Exception e) {
				// fall back to default mode
			}
		}
		return defaultMode;
	}
}
