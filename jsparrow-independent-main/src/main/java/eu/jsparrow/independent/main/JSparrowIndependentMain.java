package eu.jsparrow.independent.main;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

@SuppressWarnings("nls")
public class JSparrowIndependentMain {

	private static final String TRUE_AS_STRING = Boolean.TRUE.toString();
	public static final String ROOT_PROJECT_BASE_PATH = "ROOT.PROJECT.BASE.PATH";
	//public static final String SAMPLE_PROJECT_PATH = "/home/gregor/minimal-sample-projects/simple-maven-projects/example-project";
	public static final String SAMPLE_PROJECT_PATH = "/home/gregor/opensource/et_redux/";

	
	public static void main(String[] args) {
		Map<String, String> configuration = new HashMap<>();

		String val = Instant.now()
			.toString();

		final String tempWorkspacePath = "/home/gregor/minimal-sample-projects/temp-workspace/" + val;

		configuration.put("context.containing.jsparrow.properties", TRUE_AS_STRING);
		configuration.put("debug.enabled", TRUE_AS_STRING);
		// configuration.put("STANDALONE.MODE", "REFACTOR");
		configuration.put("STANDALONE.MODE", "SELECT_SOURCES");
		configuration.put("RULES.FILTER", "DiamondOperator"); //$NON-NLS-1$
		configuration.put("LICENSE", "IT43A7PPH");
		configuration.put("DEFAULT.CONFIG", TRUE_AS_STRING);
		configuration.put("SELECTED.SOURCES", "**");

		configuration.put(ROOT_PROJECT_BASE_PATH, SAMPLE_PROJECT_PATH);
		configuration.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
		configuration.put(Constants.FRAMEWORK_STORAGE, "target/bundlecache");

		configuration.put("osgi.instance.area.default", tempWorkspacePath);
		System.setProperty("user.dir", tempWorkspacePath);

		configuration.put("start.jsparrow.independent.handler", TRUE_AS_STRING);

		/*
		 * This is solution B from this article:
		 * https://spring.io/blog/2009/01/19/exposing-the-boot-classpath-in-
		 * osgi/
		 */
		configuration.put(Constants.FRAMEWORK_BOOTDELEGATION,
				"javax.*,org.xml.*,sun.*,com.sun.*,jdk.internal.reflect,jdk.internal.reflect.*"); //$NON-NLS-1$

		BundleStarter bundleStarter = new BundleStarter();
		try {
			bundleStarter.runStandalone(configuration);
		} catch (BundleException | InterruptedException | IOException e) {
			e.printStackTrace();
		}

	}

}
