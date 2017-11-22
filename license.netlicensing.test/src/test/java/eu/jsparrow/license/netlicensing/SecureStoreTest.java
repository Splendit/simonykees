package eu.jsparrow.license.netlicensing;

import java.io.File;
import java.io.IOException;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.osgi.util.NLS;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;

public class SecureStoreTest {

	private static final Logger logger = LoggerFactory.getLogger(SecureStoreTest.class);

	private File file;

	@Rule
	public TestName name = new TestName();

	@Before
	public void setup() throws IOException {
		file = new File(System.getProperty("user.home"), //$NON-NLS-1$
				".eclipse/org.eclipse.equinox.security/jsparrow_store_" + name.getMethodName()); //$NON-NLS-1$
		if (file.exists()) {
			if (!file.delete()) {
				String loggerError = NLS.bind(Messages.Activator_couldNotDeleteFileWithPath, file.getAbsolutePath());
				logger.error(loggerError);
			}
		}
	}

	@After
	public void tearDown() throws IOException {
		if (file.exists()) {
			if (!file.delete()) {
				String loggerError = NLS.bind(Messages.Activator_couldNotDeleteFileWithPath, file.getAbsolutePath());
				logger.error(loggerError);
			}
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
