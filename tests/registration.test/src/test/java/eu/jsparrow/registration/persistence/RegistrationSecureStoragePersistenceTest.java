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

import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.api.persistence.IEncryption;

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
	public void save_validSecret_encryptsAndSavesData() throws Exception {
		String sampleSecret = "sample-secret";
		byte[] secretBytes = sampleSecret.getBytes();
		byte[] encryptedSecretBytes = "encrypted-secret".getBytes();

		when(encryption.encrypt(secretBytes)).thenReturn(encryptedSecretBytes);

		persistence.save(sampleSecret);

		verify(simonykeesRegistrationNode).putByteArray(eq("registration-model"), eq(encryptedSecretBytes), eq(false));
	}

	@Test
	public void load_validModle_decryptsAndLoadsSecret() throws Exception {
		String sampleSecret = "sample-secret";
		byte[] encryptedSecretBytes = "encryptedModel".getBytes();
		byte[] decryptedSecret = sampleSecret.getBytes();

		when(simonykeesRegistrationNode.getByteArray(any(), any())).thenReturn(encryptedSecretBytes);
		when(encryption.decrypt(encryptedSecretBytes)).thenReturn(decryptedSecret);

		String result = persistence.load();
		assertNotNull(result);
	}

	@Test
	public void load_withNothingInStorage_returnsEmptyRegistrationSecret() throws Exception {
		when(simonykeesRegistrationNode.getByteArray(any(), any())).thenReturn(null);

		String result = persistence.load();

		assertTrue(result.isEmpty());
	}
	
	@Test
	public void load_withCorruptedSecureStorage_returnsEmptyRegistrationSecret() throws Exception {
		when(simonykeesRegistrationNode.getByteArray(any(), any())).thenThrow(IllegalArgumentException.class);

		String result = persistence.load();

		assertTrue(result.isEmpty());
	}

	@Test(expected = PersistenceException.class)
	public void save_withCorruptedSecureStorage_throwsPersistenceException() throws Exception {
		String secretKey = "secretKey";
		byte[] secretBytes = secretKey.getBytes();
		byte[] encryptedSecretBytes = "encryptedModel".getBytes();
		when(encryption.encrypt(eq(secretBytes))).thenReturn(encryptedSecretBytes);

		doThrow(StorageException.class).when(simonykeesRegistrationNode)
			.putByteArray(anyString(), any(), anyBoolean());

		persistence.save(secretKey);
	}
}
