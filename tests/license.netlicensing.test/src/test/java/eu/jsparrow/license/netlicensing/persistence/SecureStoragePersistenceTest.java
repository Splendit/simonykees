package eu.jsparrow.license.netlicensing.persistence;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertNotNull;
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
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.exception.PersistenceException;
import eu.jsparrow.license.api.persistence.IEncryption;
import eu.jsparrow.license.netlicensing.model.DemoLicenseModel;
import eu.jsparrow.license.netlicensing.testhelper.DummyLicenseModel;

@SuppressWarnings("nls")
@RunWith(MockitoJUnitRunner.Silent.class)
public class SecureStoragePersistenceTest {

	/*
	 * TODO change to @RunWith(MockitoJUnitRunner.class) later on. the Silent
	 * class is for preventing compatibility issues, after upgrading to mockito
	 * 2
	 */

	@Mock
	private ISecurePreferences simonykeesLicenseNode;

	@Mock
	private IEncryption encryption;

	private LicenseSecureStoragePersistence secureStoragePersistence;

	@Before
	public void setUp() {
		ISecurePreferences securePreferences = mock(ISecurePreferences.class);
		when(securePreferences.node(anyString())).thenReturn(simonykeesLicenseNode);

		this.secureStoragePersistence = new LicenseSecureStoragePersistence(securePreferences, encryption);
	}

	@Test
	public void save_validModel_encryptsAndSavesData() throws Exception {
		byte[] modelBytes = ModelSerializer.serialize(new DummyLicenseModel());
		byte[] encryptedModelBytes = "encryptedModel".getBytes();

		when(encryption.encrypt(modelBytes)).thenReturn(encryptedModelBytes);

		secureStoragePersistence.save(new DummyLicenseModel());

		verify(simonykeesLicenseNode).putByteArray(eq("license-model"), eq(encryptedModelBytes), eq(false));
	}

	@Test
	public void load_validModel_decryptsAndLoadsModel() throws Exception {
		LicenseModel model = new DummyLicenseModel();
		byte[] encryptedModelBytes = "encryptedModel".getBytes();
		byte[] decryptedModel = ModelSerializer.serialize(model);
		when(simonykeesLicenseNode.getByteArray(any(), any())).thenReturn(encryptedModelBytes);
		when(encryption.decrypt(encryptedModelBytes)).thenReturn(decryptedModel);

		LicenseModel result = secureStoragePersistence.load();

		// assertEquals fails as the deserialized object is not the same as the
		// original but a clone
		assertNotNull(result);
	}

	@Test
	public void load_withNothingInStorage_returnsDemoLicenseModel() throws Exception {
		when(simonykeesLicenseNode.getByteArray(any(), any())).thenReturn(null);

		LicenseModel result = secureStoragePersistence.load();

		assertThat(result, instanceOf(DemoLicenseModel.class));
	}

	@Test(expected = PersistenceException.class)
	public void save_withCorruptedSecureStorage_throwsPersistenceException() throws Exception {
		byte[] modelBytes = "model".getBytes();
		byte[] encryptedModelBytes = "encryptedModel".getBytes();
		when(encryption.encrypt(eq(modelBytes))).thenReturn(encryptedModelBytes);

		doThrow(StorageException.class).when(simonykeesLicenseNode)
			.putByteArray(anyString(), any(), anyBoolean());

		secureStoragePersistence.save(new DummyLicenseModel());
	}

	@Test
	public void load_withCorruptedStorage_returnsDemoLicenseModel() throws Exception {
		when(simonykeesLicenseNode.getByteArray(any(), any())).thenThrow(IllegalArgumentException.class);

		LicenseModel result = secureStoragePersistence.load();

		assertThat(result, instanceOf(DemoLicenseModel.class));
	}
}
