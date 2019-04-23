package eu.jsparrow.ui.util;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Optional;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import eu.jsparrow.crypto.exception.KeyStoreServiceException;
import eu.jsparrow.crypto.exception.RSAServiceException;
import eu.jsparrow.crypto.service.KeyStoreService;
import eu.jsparrow.crypto.service.KeyStoreType;
import eu.jsparrow.crypto.service.RSAService;

/**
 * Provides implementation for encrypting and decrypting of the endpoint URL and
 * the license key.
 * 
 * @since 3.4.0
 *
 */
public class EndpointEncryption {

	private static final String KEY_URL_SEPARATOR = "-"; //$NON-NLS-1$
	private static final String ENCRYPTED_SIGNATURE_SEPARATOR = ":"; //$NON-NLS-1$

	private static final String KEYSTORE_RESSOURCE = "/jep-keystore.p12"; //$NON-NLS-1$
	private static final String KEYSTORE_PASS = ")D:BnY.=$>yD7.bmo&x."; //$NON-NLS-1$

	private static final String LOCAL_KEY_ALIAS = "jep"; //$NON-NLS-1$
	private static final String LOCAL_KEY_PASS = KEYSTORE_PASS;

	private static final String REMOTE_KEY_ALIAS = "jsparrow-license"; //$NON-NLS-1$

	private BundleContext bundleContext;

	private KeyStoreService keyStoreService;
	private RSAService rsaService;

	private enum ExtractValue {
		KEY(0),
		ENDPOINT(1);

		private int value;

		private ExtractValue(int value) {
			this.value = value;
		}

		public int value() {
			return this.value;
		}
	}

	public EndpointEncryption() {
		bundleContext = FrameworkUtil.getBundle(getClass())
			.getBundleContext();

		ServiceReference<KeyStoreService> keyStoreServiceRef = bundleContext.getServiceReference(KeyStoreService.class);
		keyStoreService = bundleContext.getService(keyStoreServiceRef);

		ServiceReference<RSAService> rsaServiceRef = bundleContext.getServiceReference(RSAService.class);
		rsaService = bundleContext.getService(rsaServiceRef);
	}

	/**
	 * This method takes the encrypted license, checks its signature, decrypts
	 * it and returns the endpoint part of the decrypted license
	 * 
	 * @param encrypted
	 *            encrypted license
	 * @return the endpoint part from the decrypted license
	 * @throws EndpointEncryptionException
	 *             if something goes wrong
	 */
	public String decryptEndpoint(String encrypted) throws EndpointEncryptionException {
		return decrypt(encrypted, ExtractValue.ENDPOINT);
	}

	/**
	 * This method takes the encrypted license, checks its signature, decrypts
	 * it and returns the license key part of the decrypted license
	 * 
	 * @param encrypted
	 *            encrypted license
	 * @return the license key part from the decrypted license
	 * @throws EndpointEncryptionException
	 *             if something goes wrong
	 */
	public String decryptKey(String encrypted) throws EndpointEncryptionException {
		return decrypt(encrypted, ExtractValue.KEY);
	}

	/**
	 * Checks if the given key has the correct, valid format.
	 * 
	 * @param key
	 * @return
	 */
	public boolean isEncryptedKey(String key) {

		// regex for checking base64 matches
		final String BASE_64_REGEX = "^(?:[A-Za-z0-9+/]{4})+(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$"; //$NON-NLS-1$

		String[] licenseSignature = key.split(ENCRYPTED_SIGNATURE_SEPARATOR);
		if (licenseSignature.length != 2) {
			return false;
		}

		String license = licenseSignature[0];
		String signature = licenseSignature[1];

		boolean isSignatureB64 = signature.matches(BASE_64_REGEX);
		boolean isLicenseB64 = license.matches(BASE_64_REGEX);

		return isSignatureB64 && isLicenseB64;
	}

	private String decrypt(String encrypted, ExtractValue extract) throws EndpointEncryptionException {
		try {
			loadKeyStore();

			/*
			 * the encrypted string looks like this:
			 * <encrypted-key-and-endpoint>:<signature>
			 */
			String[] signatureKey = encrypted.split(ENCRYPTED_SIGNATURE_SEPARATOR);
			if (signatureKey.length != 2) {
				throw new EndpointEncryptionException("Incorrect format"); //$NON-NLS-1$
			}

			String encryptedKeyUrl = signatureKey[0];
			String signature = signatureKey[1];

			if (!checkSignature(encryptedKeyUrl, signature)) {
				throw new EndpointEncryptionException("Invalid signature"); //$NON-NLS-1$
			}

			Optional<PrivateKey> privateKey = keyStoreService.getPrivateKey(LOCAL_KEY_ALIAS, LOCAL_KEY_PASS);
			if (!privateKey.isPresent()) {
				throw new EndpointEncryptionException("Key not found"); //$NON-NLS-1$
			}

			/*
			 * The decrypted String looks like this:
			 * <license-key>-<endpoint-url>
			 */
			String decrypted = rsaService.decrypt(encryptedKeyUrl, privateKey.get());

			String[] parts = decrypted.split(KEY_URL_SEPARATOR);
			if (parts.length < 2) {
				throw new EndpointEncryptionException("Incorrect format"); //$NON-NLS-1$
			}

			return parts[extract.value()];
		} catch (KeyStoreServiceException | RSAServiceException e) {
			throw new EndpointEncryptionException(e.getMessage(), e);
		}
	}

	private boolean checkSignature(String plainText, String signature)
			throws KeyStoreServiceException, RSAServiceException {
		loadKeyStore();

		Optional<PublicKey> publicKey = keyStoreService.getPublicKey(REMOTE_KEY_ALIAS);

		if (!publicKey.isPresent()) {
			return false;
		}

		return rsaService.verify(plainText, signature, publicKey.get());
	}

	private void loadKeyStore() throws KeyStoreServiceException {
		if (keyStoreService.isKeyStoreLoaded()) {
			return;
		}

		InputStream keyStoreInputStream = getClass().getResourceAsStream(KEYSTORE_RESSOURCE);
		keyStoreService.loadKeyStore(keyStoreInputStream, KeyStoreType.TYPE_PKCS12, KEYSTORE_PASS);
	}
}
