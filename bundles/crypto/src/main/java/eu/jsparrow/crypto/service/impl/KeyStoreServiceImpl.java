package eu.jsparrow.crypto.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;

import eu.jsparrow.crypto.exception.KeyStoreServiceException;
import eu.jsparrow.crypto.service.KeyStoreService;
import eu.jsparrow.crypto.service.KeyStoreType;
import eu.jsparrow.i18n.Messages;

/**
 * Implementation for {@link KeyStoreService}
 * 
 * @since 3.4.0
 */
@Component
public class KeyStoreServiceImpl implements KeyStoreService {

	KeyStore keyStore;

	@Override
	public void loadKeyStore(InputStream inputStream, KeyStoreType keyStoreType, String password)
			throws KeyStoreServiceException {
		try {
			char[] passwordArray = (password != null) ? password.toCharArray() : null;
			keyStore = KeyStore.getInstance(keyStoreType.getType());
			keyStore.load(inputStream, passwordArray);
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			keyStore = null;
			throw new KeyStoreServiceException(e.getMessage(), e);
		}
	}

	@Override
	public boolean isKeyStoreLoaded() {
		return keyStore != null;
	}

	@Override
	public Optional<KeyPair> getKeyPair(String alias, String password) throws KeyStoreServiceException {
		Optional<PrivateKey> privateKey = getPrivateKey(alias, password);
		Optional<PublicKey> publicKey = getPublicKey(alias);

		if (privateKey.isPresent() && publicKey.isPresent()) {
			KeyPair keyPair = new KeyPair(publicKey.get(), privateKey.get());
			return Optional.ofNullable(keyPair);
		}

		return Optional.empty();
	}

	@Override
	public Optional<PublicKey> getPublicKey(String alias) throws KeyStoreServiceException {
		return getCertificate(alias).map(Certificate::getPublicKey);
	}

	@Override
	public Optional<PrivateKey> getPrivateKey(String alias, String password) throws KeyStoreServiceException {
		if (!isKeyStoreLoaded()) {
			throw new KeyStoreServiceException(Messages.KeyStoreServiceImpl_keyStoreNotLoadedException);
		}

		try {
			char[] passwordArray = (password != null) ? password.toCharArray() : null;
			Key key = keyStore.getKey(alias, passwordArray);

			if (key instanceof PrivateKey) {
				return Optional.ofNullable((PrivateKey) key);
			}

		} catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
			throw new KeyStoreServiceException(e.getMessage(), e);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Certificate> getCertificate(String alias) throws KeyStoreServiceException {
		if (!isKeyStoreLoaded()) {
			throw new KeyStoreServiceException(Messages.KeyStoreServiceImpl_keyStoreNotLoadedException);
		}

		try {
			Certificate cert = keyStore.getCertificate(alias);
			return Optional.ofNullable(cert);
		} catch (KeyStoreException e) {
			throw new KeyStoreServiceException(e.getMessage(), e);
		}
	}

}
