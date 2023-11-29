package eu.jsparrow.independent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("nls")
public class ContextPropertyHelper {
	private static final String TRUE_VALUE = Boolean.TRUE.toString();
	public static final String ROOT_PROJECT_BASE_PATH = "ROOT.PROJECT.BASE.PATH";
	public static final String LICENSE_KEY = "LICENSE";
	public static final String DEBUG_ENABLED = "debug.enabled";
	public static final String STANDALONE_MODE_KEY = "STANDALONE.MODE";
	public static final String USE_DEFAULT_CONFIGURATION = "DEFAULT.CONFIG"; //$NON-NLS-1$
	public static final String SELECTED_SOURCES = "SELECTED.SOURCES"; //$NON-NLS-1$

	// Values
	public static final String SAMPLE_PROJECT_PATH = "/home/gregor/minimal-sample-projects/simple-maven-projects/example-project";

	private static final Map<String, String> JSPARROW_INDEPENDENT_CONFIG;

	static {
		HashMap<String, String> temp = new HashMap<>();
		temp.put(DEBUG_ENABLED, TRUE_VALUE);
		temp.put(STANDALONE_MODE_KEY, "REFACTOR");
		temp.put(LICENSE_KEY, "IT43A7PPH");
		temp.put(ROOT_PROJECT_BASE_PATH, SAMPLE_PROJECT_PATH);
		temp.put(USE_DEFAULT_CONFIGURATION, TRUE_VALUE);
		temp.put(SELECTED_SOURCES, "**");
		JSPARROW_INDEPENDENT_CONFIG = Collections.unmodifiableMap(temp);
	}

	public static String getProperty(String key) {
		String value = JSPARROW_INDEPENDENT_CONFIG.get(key);
		if (value != null) {
			return value;
		}
		value = Activator.getContext()
			.getProperty(key);

		return value;
	}

	private ContextPropertyHelper() {
		// private default constructor hiding implicit public one
	}

}
