package eu.jsparrow.independent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("nls")
public class ContextPropertyHelper {
	private static final String TRUE_VALUE = Boolean.TRUE.toString();
	public static final String LICENSE_KEY = "LICENSE";
	public static final String DEBUG_ENABLED = "debug.enabled";
	public static final String STANDALONE_MODE_KEY = "STANDALONE.MODE";
	public static final String USE_DEFAULT_CONFIGURATION = "DEFAULT.CONFIG"; //$NON-NLS-1$
	public static final String SELECTED_SOURCES = "SELECTED.SOURCES"; //$NON-NLS-1$

	// Values

	private static final Map<String, String> JSPARROW_INDEPENDENT_CONFIG;

	static {
		HashMap<String, String> temp = new HashMap<>();
		temp.put(DEBUG_ENABLED, TRUE_VALUE);
		temp.put(STANDALONE_MODE_KEY, "REFACTOR");
		temp.put(LICENSE_KEY, "IT43A7PPH");
		temp.put(USE_DEFAULT_CONFIGURATION, TRUE_VALUE);
		temp.put(SELECTED_SOURCES, "**");
		JSPARROW_INDEPENDENT_CONFIG = Collections.unmodifiableMap(temp);
	}

	public static String getProperty(String key) {
		if (!isContextContainingJSparrowProperties()) {
			String value = JSPARROW_INDEPENDENT_CONFIG.get(key);
			if (value != null) {
				return value;
			}
		}
		return Activator.getContext()
			.getProperty(key);

	}

	public static boolean isContextContainingJSparrowProperties() {
		String propertyValue = Activator.getContext()
			.getProperty("context.containing.jsparrow.properties");
		if (propertyValue == null) {
			return false;
		}
		return Boolean.parseBoolean(propertyValue);
	}

	private ContextPropertyHelper() {
		// private default constructor hiding implicit public one
	}

}
