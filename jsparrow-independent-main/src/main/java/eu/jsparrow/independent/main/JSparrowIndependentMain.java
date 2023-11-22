package eu.jsparrow.independent.main;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
//TODO: uncomment 
//import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;

@SuppressWarnings("nls")
public class JSparrowIndependentMain {

	public static final String SAMPLE_PROJECT_PATH = "/home/gregor/eclipse/rcp-2022-03/workspace/example-project";
	private static final Logger log = Logger.getLogger("JSparrowIndependentMain");
	
	public static void main(String[] args) {
		log.info("JSparrowIndependentMain#main#main begin");

		Map<String, String> configuration = new HashMap<>();
		configuration.put("debug.enabled", "true");
		configuration.put("STANDALONE.MODE", "REFACTOR");
		// TODO:
		// as soon as the new mode is present:
		// configuration.put("STANDALONE.MODE", "SELECT_SOURCES");
		//
		configuration.put("LICENSE", "IT43A7PPH");
		configuration.put("ROOT.PROJECT.BASE.PATH", SAMPLE_PROJECT_PATH);

		configuration.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
		configuration.put(Constants.FRAMEWORK_STORAGE, "osgi.instance.area.default");
		configuration.put("osgi.instance.area.default", System.getProperty("user.dir"));

		/*
		 * This is solution B from this article:
		 * https://spring.io/blog/2009/01/19/exposing-the-boot-classpath-in-
		 * osgi/
		 */
		configuration.put(Constants.FRAMEWORK_BOOTDELEGATION,
				"javax.*,org.xml.*,sun.*,com.sun.*,jdk.internal.reflect,jdk.internal.reflect.*"); //$NON-NLS-1$

		// TODO: uncomment following code:
		// BundleStarter bundleStarter = new BundleStarter();
		// try {
		// bundleStarter.runStandalone(configuration);
		// } catch (BundleException | InterruptedException e) {
		// e.printStackTrace();
		// }

		log.info("JSparrowIndependentMain#main#main end");

	}

}
