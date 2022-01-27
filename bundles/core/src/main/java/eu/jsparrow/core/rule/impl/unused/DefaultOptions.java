package eu.jsparrow.core.rule.impl.unused;

import static eu.jsparrow.core.rule.impl.unused.Constants.PACKAGE_PRIVATE_CLASSES;
import static eu.jsparrow.core.rule.impl.unused.Constants.PACKAGE_PRIVATE_FIELDS;
import static eu.jsparrow.core.rule.impl.unused.Constants.PACKAGE_PRIVATE_METHODS;
import static eu.jsparrow.core.rule.impl.unused.Constants.PRIVATE_CLASSES;
import static eu.jsparrow.core.rule.impl.unused.Constants.PRIVATE_FIELDS;
import static eu.jsparrow.core.rule.impl.unused.Constants.PRIVATE_METHODS;
import static eu.jsparrow.core.rule.impl.unused.Constants.PROTECTED_CLASSES;
import static eu.jsparrow.core.rule.impl.unused.Constants.PROTECTED_FIELDS;
import static eu.jsparrow.core.rule.impl.unused.Constants.PROTECTED_METHODS;
import static eu.jsparrow.core.rule.impl.unused.Constants.PUBLIC_CLASSES;
import static eu.jsparrow.core.rule.impl.unused.Constants.PUBLIC_FIELDS;
import static eu.jsparrow.core.rule.impl.unused.Constants.PUBLIC_METHODS;
import static eu.jsparrow.core.rule.impl.unused.Constants.REMOVE_INITIALIZERS_SIDE_EFFECTS;
import static eu.jsparrow.core.rule.impl.unused.Constants.REMOVE_TEST_CODE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultOptions {
	
	private static final Map<String, Boolean> DEFAULT_OPTIONS = initDefaultOptions();
	

	public static final String PROJECT_SCOPE = "Project"; //$NON-NLS-1$
	public static final String WORKSPACE_SCOPE = "Workspace"; //$NON-NLS-1$
	public static final String DEFAULT_SCOPE = WORKSPACE_SCOPE;
	
	private DefaultOptions() {
		/*
		 * Hide default constructor
		 */
	}
	
	public static Map<String, Boolean> getDefaultOptions() {
		return DEFAULT_OPTIONS;
	}

	private static Map<String, Boolean> initDefaultOptions() {
		Map<String, Boolean> options = new HashMap<>();
		options.put(PRIVATE_FIELDS,  true);
		options.put(PROTECTED_FIELDS,  false);
		options.put(PACKAGE_PRIVATE_FIELDS,  false);
		options.put(PUBLIC_FIELDS,  false);
		
		options.put(PRIVATE_METHODS,  true);
		options.put(PROTECTED_METHODS,  false);
		options.put(PACKAGE_PRIVATE_METHODS,  false);
		options.put(PUBLIC_METHODS,  false);
		
		options.put(PRIVATE_CLASSES,  true);
		options.put(PROTECTED_CLASSES,  false);
		options.put(PACKAGE_PRIVATE_CLASSES,  false);
		options.put(PUBLIC_CLASSES,  false);
		
		options.put(REMOVE_INITIALIZERS_SIDE_EFFECTS, false);
		options.put(REMOVE_TEST_CODE, false);

		return Collections.unmodifiableMap(options);
	}
	
	

}
