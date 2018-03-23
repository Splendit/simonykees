package eu.jsparrow.license.netlicensing.cleanslate.persistence;

import java.lang.invoke.MethodHandles;
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.netlicensing.cleanslate.model.ValidationException;


@SuppressWarnings("nls")
public class AESEncryption implements IEncryption {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static final String ALGORITHM = "AES";

	private static final String TRANSFORMATION = "AES";

	private static final String KEY = "SOME_SECRET_KEY_";

	public byte[] encrypt(byte[] data) throws ValidationException {
		Key secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return cipher.doFinal(data);
		} catch (GeneralSecurityException e) {
			logger.error("Failed to encrypt data", e);
			throw new ValidationException(e);
		}
	}

	public byte[] decrypt(byte[] encryptedData) throws ValidationException {
		Cipher cipher;
		try {
			Key secretKey = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
			cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return cipher.doFinal(encryptedData);
		} catch (GeneralSecurityException e) {
			logger.error("Failed decrypt secure data", e);
			throw new ValidationException(e);
		}
	}
}
