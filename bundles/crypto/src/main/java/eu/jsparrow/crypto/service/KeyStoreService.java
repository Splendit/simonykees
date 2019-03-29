package eu.jsparrow.crypto.service;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

public interface KeyStoreService {

	String TYPE_JKS = "jks"; //$NON-NLS-1$
	String TYPE_JCEKS = "jceks"; //$NON-NLS-1$
	String TYPE_DKS = "dks"; //$NON-NLS-1$
	String TYPE_PKCS11 = "pkcs11"; //$NON-NLS-1$
	String TYPE_PKCS12 = "pkcs12"; //$NON-NLS-1$
	
	/**
	 * Loads 
	 * @param keyStoreType
	 * @param is
	 * @param password
	 */
	void loadKeyStore(InputStream is, String keyStoreType, String password);
	boolean isKeyStoreLoaded();
	KeyPair getKeyPair(String alias, String password);
	PublicKey getPublicKey(String alias);
	PrivateKey getPrivateKey(String alias, String password);
	Certificate getCertificate(String alias);
}
