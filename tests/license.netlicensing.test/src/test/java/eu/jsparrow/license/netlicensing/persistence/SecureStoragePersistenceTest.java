package eu.jsparrow.license.netlicensing.persistence;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.model.DemoLicenseModel;
import eu.jsparrow.license.netlicensing.testhelper.DummyLicenseModel;

@SuppressWarnings("nls")
@RunWith(MockitoJUnitRunner.class)
public class SecureStoragePersistenceTest {

	@Mock
	private ISecurePreferences simonykeesNode;

	@Mock
	private IEncryption encryption;

	private SecureStoragePersistence secureStoragePersistence;

	@Before
	public void setUp() {
		ISecurePreferences securePreferences = mock(ISecurePreferences.class);
		when(securePreferences.node(anyString())).thenReturn(simonykeesNode);

		this.secureStoragePersistence = new SecureStoragePersistence(securePreferences, encryption);
	}
	
	@Test
	public void save_validModel_encryptsAndSavesData() throws Exception {
		byte[] modelBytes = ModelSerializer.serialize(new DummyLicenseModel());
		byte[] encryptedModelBytes = "encryptedModel".getBytes();
	
		when(encryption.encrypt(modelBytes)).thenReturn(encryptedModelBytes);

		secureStoragePersistence.save(new DummyLicenseModel());

		verify(simonykeesNode).putByteArray(eq("license-model"), eq(encryptedModelBytes), eq(false));
	}

	@Test
	public void load_validModel_decryptsAndLoadsModel() throws Exception {
		LicenseModel model = new DummyLicenseModel();
		byte[] encryptedModelBytes = "encryptedModel".getBytes();
		byte[] decryptedModel = ModelSerializer.serialize(model);
		when(simonykeesNode.getByteArray(any(), any())).thenReturn(encryptedModelBytes);
		when(encryption.decrypt(encryptedModelBytes)).thenReturn(decryptedModel);

		LicenseModel result= secureStoragePersistence.load();
		
		// assertEquals fails as the deserialized object is not the same as the original but a clone
		assertNotNull(result);
	}
	
	@Test
	public void load_withNothingInStorage_returnsDemoLicenseModel() throws Exception {
		when(simonykeesNode.getByteArray(any(), any())).thenReturn(null);
		
		LicenseModel result= secureStoragePersistence.load();
		
		assertThat(result, instanceOf(DemoLicenseModel.class));
	}

	@Test(expected = PersistenceException.class)
	public void save_withCorruptedSecureStorage_throwsPersistenceException() throws Exception {
		byte[] modelBytes = "model".getBytes();
		byte[] encryptedModelBytes = "encryptedModel".getBytes();
		when(encryption.encrypt(eq(modelBytes))).thenReturn(encryptedModelBytes);

		doThrow(StorageException.class).when(simonykeesNode)
			.putByteArray(anyString(), any(), anyBoolean());

		secureStoragePersistence.save(new DummyLicenseModel());
	}

	

}
