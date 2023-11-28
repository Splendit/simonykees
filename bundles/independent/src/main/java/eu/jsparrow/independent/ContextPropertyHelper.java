package eu.jsparrow.independent;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ContextPropertyHelper {
	public static final String ROOT_PROJECT_BASE_PATH = "ROOT.PROJECT.BASE.PATH"; //$NON-NLS-1$
	public static final String LICENSE_KEY = "LICENSE"; //$NON-NLS-1$
	public static final String DEBUG_ENABLED = "debug.enabled"; //$NON-NLS-1$
	public static final String STANDALONE_MODE_KEY = "STANDALONE.MODE"; //$NON-NLS-1$
	
	// Values
	public static final String SAMPLE_PROJECT_PATH = "/home/gregor/minimal-sample-projects/simple-maven-projects/example-project"; //$NON-NLS-1$

	
	private static final Map<String, String> CONFIG_DEFAULT_VALUES;

	static {
		HashMap<String, String> temp = new HashMap<>();
		temp.put(DEBUG_ENABLED, Boolean.TRUE.toString());
		temp.put(STANDALONE_MODE_KEY, "REFACTOR"); //$NON-NLS-1$
		temp.put(LICENSE_KEY, "IT43A7PPH"); //$NON-NLS-1$
		temp.put(ROOT_PROJECT_BASE_PATH, SAMPLE_PROJECT_PATH);
		CONFIG_DEFAULT_VALUES = Collections.unmodifiableMap(temp);
	}

	public static String getProperty(String key) {
		String value = Activator.getContext()
			.getProperty(key);
		if (value != null) {
			return value;
		}
		return CONFIG_DEFAULT_VALUES.get(key);
	}

	private ContextPropertyHelper() {
		// private default constructor hiding implicit public one
	}

}
