package eu.jsparrow.license.netlicensing.persistence;

import java.lang.invoke.MethodHandles;
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.license.api.exception.PersistenceException;

/**
 * Implementor of {@link IEncryption} using AES.
 */
public class AESEncryption implements IEncryption {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final String ALGORITHM = "AES"; //$NON-NLS-1$

	private static final String TRANSFORMATION = "AES"; //$NON-NLS-1$

	private static final String KEY = "SOME_SECRET_KEY_"; //$NON-NLS-1$

	public byte[] encrypt(byte[] data) throws PersistenceException {
		logger.debug("Encrypting data '{}'", data); //$NON-NLS-1$
		Key secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
		Cipher cipher;
		try {
			// Cipher loads slowly the first time
			cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return cipher.doFinal(data);
		} catch (GeneralSecurityException e) {
			throw new PersistenceException(ExceptionMessages.Netlicensing_persistenceException_failedToEncrypt, e);
		}
	}

	public byte[] decrypt(byte[] encryptedData) throws PersistenceException {
		logger.debug("Decrypting data"); //$NON-NLS-1$
		Cipher cipher;
		try {
			Key secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
			cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return cipher.doFinal(encryptedData);
		} catch (GeneralSecurityException e) {
			throw new PersistenceException(ExceptionMessages.Netlicensing_persistenceException_failedToDecrypt, e);
		}
	}

}
