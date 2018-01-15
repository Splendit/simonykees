package eu.jsparrow.standalone.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

import eu.jsparrow.core.config.YAMLConfigException;
import eu.jsparrow.standalone.Activator;
import eu.jsparrow.standalone.StandaloneMode;

public class StandaloneTest {

	private static final String STANDALONE_MODE_KEY = "STANDALONE.MODE"; //$NON-NLS-1$
	
	private Map<String, String> configuration;
	private BundleContext context;
	
	@Before
	public void setUpClass() {
		configuration = new HashMap<>();
		configuration.put(STANDALONE_MODE_KEY, StandaloneMode.TEST.name());
		context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
	}
	
	@Test
	public void setExitErrorMessage_shouldReturnErrorMessage() {
		String key = "eu.jsparrow.standalone.exit.message"; //$NON-NLS-1$
		String testMessage = "Test"; //$NON-NLS-1$
		
		Activator.setExitErrorMessage(context, testMessage);
		
		String result = System.getProperty(key);
		
		assertEquals(testMessage, result);
	}
	
	@Test
	public void loadConfiguration_selectedProfileExists() {
		
	}
	
	@Test(expected = YAMLConfigException.class)
	public void loadConfiguration_selectedProfileDoesNotExist() {
		
	}
}
