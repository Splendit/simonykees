package eu.jsparrow.license.netlicensing;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecureStoreTest {

	private static final Logger logger = LoggerFactory.getLogger(SecureStoreTest.class);

	private File file;

	@Rule
	public TestName name = new TestName();

	@Before
	public void setup() throws MalformedURLException, IOException {
		file = new File(System.getProperty("user.home"), //$NON-NLS-1$
				".eclipse/org.eclipse.equinox.security/jsparrow_store_" + name.getMethodName()); //$NON-NLS-1$
		if (file.exists()) {
			file.delete();
		}
	}

	@After
	public void tearDown() throws MalformedURLException, IOException {
		if (file.exists()) {
			file.delete();
		}
		file = null;
	}

	@SuppressWarnings("nls")
	@Test
	public void testPrivateSecureStore() throws IOException, StorageException {
		logger.info(file.toString());
		ISecurePreferences iSecurePreferences = SecurePreferencesFactory.open(file.toURI()
			.toURL(), null);
		iSecurePreferences.node("simonykees")
			.put("key", "value", false);
		logger.info(iSecurePreferences.node("simonykees")
			.get("key", ""));
		iSecurePreferences.flush();
	}

}
