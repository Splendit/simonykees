package eu.jsparrow.license.netlicensing.persistence;

import java.lang.invoke.MethodHandles;
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.exception.PersistenceException;


@SuppressWarnings("nls")
public class AESEncryption implements IEncryption {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final String ALGORITHM = "AES";

	private static final String TRANSFORMATION = "AES";

	private static final String KEY = "SOME_SECRET_KEY_";
	
	public byte[] encrypt(byte[] data) throws PersistenceException {
		logger.debug("Encrypting data '{}'", data);
		Key secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
		Cipher cipher;
		try {
			// Cipher loads slowly the first time
			cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return cipher.doFinal(data);
		} catch (GeneralSecurityException e) {
			throw new PersistenceException("Failed to encrypt model",e);
		}
	}

	public byte[] decrypt(byte[] encryptedData) throws PersistenceException {
		logger.debug("Decrypting data");
		Cipher cipher;
		try {
			Key secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
			cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return cipher.doFinal(encryptedData);
		} catch (GeneralSecurityException e) {
			throw new PersistenceException("Failed decrypt secure data", e);
		}
	}
	

}
