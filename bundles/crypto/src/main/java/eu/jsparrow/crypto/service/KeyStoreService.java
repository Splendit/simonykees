package eu.jsparrow.crypto.service;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.Optional;

import eu.jsparrow.crypto.exception.KeyStoreServiceException;

/**
 * Interface for describing a service to access java key stores
 * 
 * @since 3.4.0
 */
public interface KeyStoreService {

	/**
	 * Loads the Java Key Store and makes it available for other methods in the
	 * {@link KeyStoreService}
	 * 
	 * @param inputStream
	 *            input stream for the key store file
	 * @param keyStoreType
	 *            type of the key store defined in {@link KeyStoreType}
	 * @param password
	 *            password for the keystore
	 * @throws KeyStoreException
	 */
	void loadKeyStore(InputStream inputStream, KeyStoreType keyStoreType, String password)
			throws KeyStoreServiceException;

	/**
	 * Checks, whether the key store has been loaded or not
	 * 
	 * @return {@code true}, if the key store is available, {@code false} otherwise
	 */
	boolean isKeyStoreLoaded();

	/**
	 * Gets the {@link KeyPair} for the given {@code alias} from the key store
	 * 
	 * @param alias
	 *            alias of the entry
	 * @param password
	 *            password for the key pair represented by {@code alias}
	 * @return the key pair for the given {@code alias} or
	 *         {@link Optional#empty()} if the alias can't be found
	 * @throws KeyStoreServiceException
	 */
	Optional<KeyPair> getKeyPair(String alias, String password) throws KeyStoreServiceException;

	/**
	 * Looks for the entry {@code alias} in the key store and returns its public
	 * key
	 * 
	 * @param alias
	 *            alias of the entry
	 * @return the public key for the given {@code alias} or
	 *         {@link Optional#empty()} if the alias can't be found
	 * @throws KeyStoreServiceException
	 */
	Optional<PublicKey> getPublicKey(String alias) throws KeyStoreServiceException;

	/**
	 * Looks for the entry {@code alias} in the key store and returns its
	 * private key
	 * 
	 * @param alias
	 *            alias of the entry
	 * @param password
	 *            password for the private key represented by {@code alias}
	 * @return the private key for the given {@code alias} or
	 *         {@link Optional#empty()} if the alias can't be found
	 * @throws KeyStoreServiceException
	 */
	Optional<PrivateKey> getPrivateKey(String alias, String password) throws KeyStoreServiceException;

	/**
	 * Looks for the entry {@code alias} in the key store and returns its
	 * certificate
	 * 
	 * @param alias
	 *            alias of the entry
	 * @return the certificate for the given {@code alias} or
	 *         {@link Optional#empty()} if the alias can't be found
	 * @throws KeyStoreServiceException
	 */
	Optional<Certificate> getCertificate(String alias) throws KeyStoreServiceException;
}
