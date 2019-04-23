package eu.jsparrow.crypto.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.jsparrow.crypto.exception.KeyStoreServiceException;
import eu.jsparrow.crypto.service.KeyStoreType;

public class KeyStoreServiceTest {

	private static final String JEP_ALIAS = "jep"; //$NON-NLS-1$

	private static final String KEYSTORE_TYPE = KeyStoreType.TYPE_JKS.getType();
	private static final String KEYSTORE_PASSWORD = "s3cr3t"; //$NON-NLS-1$
	private static final String JEP_KEY_PASSWORD = "s3cr3t"; //$NON-NLS-1$

	private static final String KEYSTORE_RESOURCE_NAME = "/jep-keystore.jks"; //$NON-NLS-1$

	private static final String EMPTY = "empty"; //$NON-NLS-1$

	private KeyStoreServiceImpl keyStoreService;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		InputStream keyStoreInputStream = getClass().getResourceAsStream(KEYSTORE_RESOURCE_NAME);
		KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
		keyStore.load(keyStoreInputStream, KEYSTORE_PASSWORD.toCharArray());

		keyStoreService = new KeyStoreServiceImpl();
		keyStoreService.keyStore = keyStore;
	}

	@Test
	public void getCertificate_jepAlias_shouldReturnCertificate() throws Exception {
		Optional<Certificate> cert = keyStoreService.getCertificate(JEP_ALIAS);

		assertTrue(cert.isPresent());
	}

	@Test
	public void getCertificate_nonExistingAlias_shouldReturnEmptyOptional() throws Exception {
		Optional<Certificate> cert = keyStoreService.getCertificate(EMPTY); // $NON-NLS-1$

		assertFalse(cert.isPresent());
	}

	@Test
	public void getCertificate_keyStoreIsNull_shouldThrowException() throws Exception {
		keyStoreService.keyStore = null;
		expectedException.expect(KeyStoreServiceException.class);
		expectedException.expectMessage("No key store has been loaded."); //$NON-NLS-1$

		keyStoreService.getCertificate(JEP_ALIAS);
	}

	@Test
	public void getCertificate_keyStoreIsUninitialized_shouldThrowException() throws Exception {
		keyStoreService.keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
		expectedException.expect(KeyStoreServiceException.class);
		expectedException.expectMessage("Uninitialized keystore"); //$NON-NLS-1$

		keyStoreService.getCertificate(JEP_ALIAS);
	}

	@Test
	public void getPrivateKey_jepAlias_shouldReturnPrivateKey() throws Exception {
		Optional<PrivateKey> privateKey = keyStoreService.getPrivateKey(JEP_ALIAS, JEP_KEY_PASSWORD);

		assertTrue(privateKey.isPresent());
	}

	@Test
	public void getPrivateKey_nonExistingAlias_shouldReturnEmptyOptional() throws Exception {
		Optional<PrivateKey> privateKey = keyStoreService.getPrivateKey(EMPTY, null); // $NON-NLS-1$

		assertFalse(privateKey.isPresent());
	}

	@Test
	public void getPrivateKey_wrongKeyPassword_shouldThrowException() throws Exception {
		expectedException.expect(KeyStoreServiceException.class);
		expectedException.expectMessage("Get Key failed: Given final block not properly padded"); //$NON-NLS-1$

		keyStoreService.getPrivateKey(JEP_ALIAS, "asdf"); //$NON-NLS-1$
	}

	@Test
	public void getPrivateKey_keyStoreIsNull_shouldThrowException() throws Exception {
		keyStoreService.keyStore = null;
		expectedException.expect(KeyStoreServiceException.class);
		expectedException.expectMessage("No key store has been loaded."); //$NON-NLS-1$

		keyStoreService.getPrivateKey(JEP_ALIAS, JEP_KEY_PASSWORD);
	}

	@Test
	public void getPrivateKey_keyStoreIsUninitialised_shouldThrowException() throws Exception {
		keyStoreService.keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
		expectedException.expect(KeyStoreServiceException.class);
		expectedException.expectMessage("Uninitialized keystore"); //$NON-NLS-1$

		keyStoreService.getPrivateKey(JEP_ALIAS, JEP_KEY_PASSWORD);
	}

	@Test
	public void getKeyPair_jepAlias_shouldReturnKeyPair() throws Exception {
		Optional<KeyPair> keyPair = keyStoreService.getKeyPair(JEP_ALIAS, JEP_KEY_PASSWORD);

		assertTrue(keyPair.isPresent());
	}

	@Test
	public void getKeyPair_nonExistingAlias_shouldReturnEmptyOptional() throws Exception {
		Optional<KeyPair> keyPair = keyStoreService.getKeyPair(EMPTY, JEP_KEY_PASSWORD); // $NON-NLS-1$

		assertFalse(keyPair.isPresent());
	}
}
