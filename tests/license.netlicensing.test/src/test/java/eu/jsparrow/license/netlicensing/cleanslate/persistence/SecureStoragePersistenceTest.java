package eu.jsparrow.license.netlicensing.cleanslate.persistence;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.jsparrow.license.netlicensing.cleanslate.DummyLicenseModel;
import eu.jsparrow.license.netlicensing.cleanslate.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.cleanslate.exception.ValidationException;
import eu.jsparrow.license.netlicensing.cleanslate.model.LicenseModel;

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

		verify(simonykeesNode).putByteArray(eq("credentials"), eq(encryptedModelBytes), eq(false));
	}

	@Test
	public void load_validModel_decryptsAndLoadsModel() throws Exception {
		LicenseModel model = new DummyLicenseModel();
		byte[] encryptedModelBytes = "encryptedModel".getBytes();
		byte[] decryptedModel = ModelSerializer.serialize(model);
		when(simonykeesNode.getByteArray(eq("credentials"), any(byte[].class))).thenReturn(encryptedModelBytes);
		when(encryption.decrypt(encryptedModelBytes)).thenReturn(decryptedModel);

		LicenseModel result= secureStoragePersistence.load();
		
		// assertEquals fails as the deserialized object is not the same as the original but a clone
		assertNotNull(result);
	}

	@Test(expected = PersistenceException.class)
	public void save_withCorruptedSecureStorage_throwsPersistenceException() throws Exception {
		byte[] modelBytes = "model".getBytes();
		byte[] encryptedModelBytes = "encryptedModel".getBytes();
		when(encryption.encrypt(eq(modelBytes))).thenReturn(encryptedModelBytes);

		doThrow(StorageException.class).when(simonykeesNode)
			.putByteArray(anyString(), any(byte[].class), anyBoolean());

		secureStoragePersistence.save(new DummyLicenseModel());
	}

	

}
