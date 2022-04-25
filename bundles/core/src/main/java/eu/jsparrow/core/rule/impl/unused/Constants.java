package eu.jsparrow.core.rule.impl.unused;

/**
 * Keys for removing unused code configuration options.
 * 
 * @since 4.8.0
 *
 */
@SuppressWarnings("nls")
public class Constants {

	private Constants() {
		/*
		 * Hide default constructor.
		 */
	}

	public static final String PRIVATE_FIELDS = "private-fields";
	public static final String PROTECTED_FIELDS = "protected-fields";
	public static final String PACKAGE_PRIVATE_FIELDS = "package-private-fields";
	public static final String PUBLIC_FIELDS = "public-fields";

	public static final String PRIVATE_METHODS = "private-methods";
	public static final String PROTECTED_METHODS = "protected-methods";
	public static final String PACKAGE_PRIVATE_METHODS = "package-private-methods";
	public static final String PUBLIC_METHODS = "public-methods";

	public static final String LOCAL_CLASSES = "local-classes";
	public static final String PRIVATE_CLASSES = "private-classes";
	public static final String PROTECTED_CLASSES = "protected-classes";
	public static final String PACKAGE_PRIVATE_CLASSES = "package-private-classes";
	public static final String PUBLIC_CLASSES = "public-classes";

	public static final String SCOPE = "scope";
	public static final String REMOVE_INITIALIZERS_SIDE_EFFECTS = "remove-initializers-side-effects";
	public static final String REMOVE_TEST_CODE = "remove-test-code";

}
