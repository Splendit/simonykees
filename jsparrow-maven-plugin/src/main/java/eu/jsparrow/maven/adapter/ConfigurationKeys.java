package eu.jsparrow.maven.adapter;

/**
 * Constants serving as keys for the equinox BundleContext configuration. Any
 * change here must be reflected also in
 * {@link eu.jsparrow.standalone.RefactoringInvoker}.
 *
 */
public class ConfigurationKeys {

	public static final String MAVEN_COMPILER_PLUGIN_ARTIFACT_ID = "maven-compiler-plugin"; //$NON-NLS-1$
	public static final String MAVEN_COMPILER_PLUGIN_CONFIGURATIN_SOURCE_NAME = "source"; //$NON-NLS-1$
	public static final String MAVEN_COMPILER_PLUGIN_PROPERTY_SOURCE_NAME = "maven.compiler.source"; //$NON-NLS-1$
	public static final String MAVEN_COMPILER_PLUGIN_DEFAULT_JAVA_VERSION = "1.5"; //$NON-NLS-1$
	public static final String SELECTED_PROFILE = "PROFILE.SELECTED"; //$NON-NLS-1$
	public static final String USE_DEFAULT_CONFIGURATION = "DEFAULT.CONFIG"; //$NON-NLS-1$
	public static final String STANDALONE_MODE_KEY = "STANDALONE.MODE"; //$NON-NLS-1$
	public static final String PROJECT_JAVA_VERSION = "PROJECT.JAVA.VERSION"; //$NON-NLS-1$
	public static final String INSTANCE_DATA_LOCATION_CONSTANT = "osgi.instance.area.default"; //$NON-NLS-1$
	public static final String FRAMEWORK_STORAGE_VALUE = "target/bundlecache"; //$NON-NLS-1$
	public static final String PROJECT_PATH_CONSTANT = "PROJECT.PATH"; //$NON-NLS-1$
	public static final String ALL_PROJECT_IDENTIFIERS = "ALL.PROJECT.IDENTIFIERS"; //$NON-NLS-1$
	public static final String PROJECT_NAME_CONSTANT = "PROJECT.NAME"; //$NON-NLS-1$
	public static final String OSGI_INSTANCE_AREA_CONSTANT = "osgi.instance.area"; //$NON-NLS-1$
	public static final String DEBUG_ENABLED = "debug.enabled"; //$NON-NLS-1$
	public static final String CONFIG_FILE_PATH = "CONFIG.FILE.PATH"; //$NON-NLS-1$
	public static final String LIST_RULES_SELECTED_ID = "LIST.RULES.SELECTED.ID"; //$NON-NLS-1$
	public static final String LICENSE_KEY = "LICENSE"; //$NON-NLS-1$
	public static final String AGENT_URL = "URL"; //$NON-NLS-1$
	public static final String DEV_MODE_KEY = "dev.mode.enabled"; //$NON-NLS-1$
	public static final String NATURE_IDS = "NATURE.IDS"; //$NON-NLS-1$
	public static final String SOURCE_FOLDER = "SOURCE.FOLDER"; //$NON-NLS-1$
	public static final String DEFAULT_SOURCE_FOLDER_PATH = "src/main/java"; //$NON-NLS-1$
	public static final String MAVEN_NATURE_ID = "org.eclipse.m2e.core.maven2Nature"; //$NON-NLS-1$
	public static final String ECLIPSE_PLUGIN_NATURE_ID = "org.eclipse.pde.PluginNature"; //$NON-NLS-1$
	public static final String JAVA_NATURE_ID = "org.eclipse.jdt.core.javanature"; //$NON-NLS-1$
	public static final String ECLIPSE_PLUGIN_PROJECT_NATURE_IDS = String.format("%s,%s,%s", MAVEN_NATURE_ID, //$NON-NLS-1$
			ECLIPSE_PLUGIN_NATURE_ID, JAVA_NATURE_ID);
	public static final String MAVEN_PROJECT_NATURE_IDS = MAVEN_NATURE_ID + "," + JAVA_NATURE_ID; //$NON-NLS-1$
	public static final String USER_DIR = "user.dir"; //$NON-NLS-1$

	private ConfigurationKeys() {
		/*
		 * Hidding public constructor
		 */
	}

}
