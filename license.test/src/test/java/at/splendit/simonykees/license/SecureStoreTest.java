package at.splendit.simonykees.license;

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

import at.splendit.simonykees.license.Activator;

public class SecureStoreTest {
	
	private File file;
	
	@Rule public TestName name = new TestName();
	
	@Before
	public void setup() throws MalformedURLException, IOException{
		file = new File(System.getProperty("user.home"), ".eclipse/org.eclipse.equinox.security/jsparrow_store_"+name.getMethodName());
		if(file.exists()){
			file.delete();
		}
	}
	
	@After
	public void tearDown() throws MalformedURLException, IOException{
		if(file.exists()){
			file.delete();
		}
		file = null;
	}

	@Test
	public void testPrivateSecureStore() throws IOException, StorageException{
		Activator.log(file.toString());
		ISecurePreferences iSecurePreferences = SecurePreferencesFactory.open(file.toURI().toURL(), null);
		iSecurePreferences.node("simonykees").put("key", "value", false);
		Activator.log(iSecurePreferences.node("simonykees").get("key", ""));
		iSecurePreferences.flush();
	}
	
}
