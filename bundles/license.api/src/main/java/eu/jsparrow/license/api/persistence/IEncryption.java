package eu.jsparrow.license.api.persistence;

import eu.jsparrow.license.api.exception.PersistenceException;

/**
 * Implementors encrypt or decrypt data.
 * 
 */
public interface IEncryption {

	/**
	 * Encrypts the given data.
	 * 
	 * @param data
	 *            data to encrypt
	 * @return the encrypted data
	 * @throws PersistenceException
	 *             if data could not be encrypted
	 */
	byte[] encrypt(byte[] data) throws PersistenceException;

	/**
	 * Decrypts the given data.
	 * 
	 * @param encryptedData
	 *            data to decrypt
	 * @return the decrypted data
	 * @throws PersistenceException
	 *             if the data could not be decrypted
	 */
	byte[] decrypt(byte[] encryptedData) throws PersistenceException;
}
