package eu.jsparrow.crypto.service.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.jsparrow.crypto.exception.RSAServiceException;
import eu.jsparrow.crypto.service.KeyStoreType;
import eu.jsparrow.crypto.service.RSAService;

public class RSAServiceImplTest {

	private static final String JEP_ALIAS = "jep"; //$NON-NLS-1$
	private static final String LICENSE_ALIAS = "license"; //$NON-NLS-1$

	private static final String KEYSTORE_PASSWORD = "s3cr3t"; //$NON-NLS-1$
	private static final String JEP_KEY_PASSWORD = "s3cr3t"; //$NON-NLS-1$

	private static final String KEYSTORE_RESOURCE_NAME = "/jep-keystore.jks"; //$NON-NLS-1$

	private PublicKey jepPublic;
	private PrivateKey jepPrivate;
	private PublicKey licensePublic;

	private RSAService rsaService;
	
	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		InputStream keyStoreResource = getClass().getResourceAsStream(KEYSTORE_RESOURCE_NAME);

		KeyStoreServiceImpl keyStoreService = new KeyStoreServiceImpl();
		keyStoreService.loadKeyStore(keyStoreResource, KeyStoreType.TYPE_JKS, KEYSTORE_PASSWORD);

		jepPublic = keyStoreService.getPublicKey(JEP_ALIAS)
			.get();
		jepPrivate = keyStoreService.getPrivateKey(JEP_ALIAS, JEP_KEY_PASSWORD)
			.get();

		licensePublic = keyStoreService.getPublicKey(LICENSE_ALIAS)
			.get();

		rsaService = new RSAServiceImpl();
	}

	@Test
	public void encryptDecrypt_shouldReturnOriginalString() throws Exception {
		String plain = "Hello"; //$NON-NLS-1$

		String encrypted = rsaService.encrypt(plain, jepPublic);
		String decrypted = rsaService.decrypt(encrypted, jepPrivate);

		assertEquals(plain, decrypted);
	}

	@Test
	public void encryptDecrypt_wrongPrivateKeyForDecryption_shouldThrowException() throws Exception {
		String plain = "hello"; //$NON-NLS-1$
		expectedException.expect(RSAServiceException.class);
		
		String encrypted = rsaService.encrypt(plain, licensePublic);
		rsaService.decrypt(encrypted, jepPrivate);
	}
	
	@Test
	public void signVerify_shouldVerify() throws Exception {
		String plain = "Hello"; //$NON-NLS-1$

		String signature = rsaService.sign(plain, jepPrivate);
		boolean verified = rsaService.verify(plain, signature, jepPublic);

		assertTrue(verified);
	}

	@Test
	public void signVerify_wrongPublicKeyForVerification_shouldThrowException() throws Exception {
		String plain = "Hello"; //$NON-NLS-1$

		String signature = rsaService.sign(plain, jepPrivate);
		boolean verified = rsaService.verify(plain, signature, licensePublic);
		
		assertFalse(verified);
	}
}
