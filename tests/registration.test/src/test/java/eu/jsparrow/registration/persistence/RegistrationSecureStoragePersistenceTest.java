package eu.jsparrow.registration.persistence;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.StorageException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import eu.jsparrow.license.api.RegistrationModel;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.api.persistence.IEncryption;
import eu.jsparrow.registration.helper.DummyRegistrationModel;

@SuppressWarnings("nls")
@RunWith(MockitoJUnitRunner.Silent.class)
public class RegistrationSecureStoragePersistenceTest {

	/*
	 * TODO change to @RunWith(MockitoJUnitRunner.class) later on. the Silent
	 * class is for preventing compatibility issues, after upgrading to mockito
	 * 2
	 */

	private ISecurePreferences simonykeesRegistrationNode;
	private IEncryption encryption;
	private RegistrationSecureStoragePersistence persistence;

	@Before
	public void setUp() {
		simonykeesRegistrationNode = mock(ISecurePreferences.class);
		encryption = mock(IEncryption.class);
		ISecurePreferences securePreferences = mock(ISecurePreferences.class);
		when(securePreferences.node(anyString())).thenReturn(simonykeesRegistrationNode);

		persistence = new RegistrationSecureStoragePersistence(securePreferences, encryption);
	}

	@Test
	public void save_validModle_encryptsAndSavesData() throws Exception {
		byte[] modelBytes = ModelSerializer.serialize(new DummyRegistrationModel());
		byte[] encryptedModelBytes = "encryptedModel".getBytes();

		when(encryption.encrypt(modelBytes)).thenReturn(encryptedModelBytes);

		persistence.save(new DummyRegistrationModel());

		verify(simonykeesRegistrationNode).putByteArray(eq("registration-model"), eq(encryptedModelBytes), eq(false));
	}

	@Test
	public void load_validModle_decryptsAndLoadsModel() throws Exception {
		RegistrationModel model = new DummyRegistrationModel();
		byte[] encryptedModelBytes = "encryptedModel".getBytes();
		byte[] decryptedModel = ModelSerializer.serialize(model);

		when(simonykeesRegistrationNode.getByteArray(any(), any())).thenReturn(encryptedModelBytes);
		when(encryption.decrypt(encryptedModelBytes)).thenReturn(decryptedModel);

		RegistrationModel result = persistence.load();
		assertNotNull(result);
	}

	@Test
	public void load_withNothingInStorage_returnsEmptyRegistrationModel() throws Exception {
		when(simonykeesRegistrationNode.getByteArray(any(), any())).thenReturn(null);

		RegistrationModel result = persistence.load();

		assertTrue(result.getKey()
			.isEmpty());
	}

	@Test(expected = PersistenceException.class)
	public void save_withCorruptedSecureStorage_throwsPersistenceException() throws Exception {
		byte[] modelBytes = "model".getBytes();
		byte[] encryptedModelBytes = "encryptedModel".getBytes();
		when(encryption.encrypt(eq(modelBytes))).thenReturn(encryptedModelBytes);

		doThrow(StorageException.class).when(simonykeesRegistrationNode)
			.putByteArray(anyString(), any(), anyBoolean());

		persistence.save(new DummyRegistrationModel());
	}
}
