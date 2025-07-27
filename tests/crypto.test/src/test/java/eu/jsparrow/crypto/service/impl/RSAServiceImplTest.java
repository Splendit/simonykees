package eu.jsparrow.crypto.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.crypto.exception.RSAServiceException;
import eu.jsparrow.crypto.service.KeyStoreType;
import eu.jsparrow.crypto.service.RSAService;

public class RSAServiceImplTest {

	private static final String JEP_ALIAS = "jep"; //$NON-NLS-1$
	private static final String LICENSE_ALIAS = "license"; //$NON-NLS-1$

	private static final String KEYSTORE_PASSWORD = "s3cr3t"; //$NON-NLS-1$
	private static final String JEP_KEY_PASSWORD = "s3cr3t"; //$NON-NLS-1$

	private static final String KEYSTORE_RESOURCE_NAME = "/jep-keystore.jks"; //$NON-NLS-1$

	private static final String PLAIN_TEXT = "Hello"; //$NON-NLS-1$

	private PublicKey jepPublic;
	private PrivateKey jepPrivate;
	private PublicKey licensePublic;

	private RSAService rsaService;

	@BeforeEach
	public void setUp() throws Exception {
		InputStream keyStoreResource = getClass().getResourceAsStream(KEYSTORE_RESOURCE_NAME);

		KeyStoreServiceImpl keyStoreService = new KeyStoreServiceImpl();
		keyStoreService.loadKeyStore(keyStoreResource, KeyStoreType.TYPE_JKS, KEYSTORE_PASSWORD);

		jepPublic = keyStoreService.getPublicKey(JEP_ALIAS).get();
		jepPrivate = keyStoreService.getPrivateKey(JEP_ALIAS, JEP_KEY_PASSWORD).get();

		licensePublic = keyStoreService.getPublicKey(LICENSE_ALIAS).get();

		rsaService = new RSAServiceImpl();
	}

	@Test
	public void encryptDecrypt_shouldReturnOriginalString() throws Exception {
		String encrypted = rsaService.encrypt(PLAIN_TEXT, jepPublic);
		String decrypted = rsaService.decrypt(encrypted, jepPrivate);

		assertEquals(PLAIN_TEXT, decrypted);
	}

	@Test
	public void encryptDecrypt_wrongPrivateKeyForDecryption_shouldThrowException() throws Exception {
		assertThrows(RSAServiceException.class, () -> {
			String encrypted = rsaService.encrypt(PLAIN_TEXT, licensePublic);
			rsaService.decrypt(encrypted, jepPrivate);
		});
	}

	@Test
	public void signVerify_shouldVerify() throws Exception {
		String signature = rsaService.sign(PLAIN_TEXT, jepPrivate);
		boolean verified = rsaService.verify(PLAIN_TEXT, signature, jepPublic);

		assertTrue(verified);
	}

	@Test
	public void signVerify_wrongPublicKeyForVerification_shouldThrowException() throws Exception {
		String signature = rsaService.sign(PLAIN_TEXT, jepPrivate);
		boolean verified = rsaService.verify(PLAIN_TEXT, signature, licensePublic);

		assertFalse(verified);
	}
}
