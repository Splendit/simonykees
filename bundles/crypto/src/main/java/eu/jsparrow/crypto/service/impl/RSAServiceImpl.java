package eu.jsparrow.crypto.service.impl;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import eu.jsparrow.crypto.exception.RSAServiceException;
import eu.jsparrow.crypto.service.RSAService;

public class RSAServiceImpl implements RSAService {

	private static final String ENCRYPTION_ALGORITHM = "RSA/ECB/PKCS1Padding"; //$NON-NLS-1$
	private static final String SIGNATURE_ALGORITHM = "SHA256withRSA"; //$NON-NLS-1$

	@Override
	public String encrypt(String plainText, PublicKey publicKey) throws RSAServiceException {

		try {
			Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);

			byte[] binaryCipherText = cipher.doFinal(plainText.getBytes());
			String cipherText = Base64.getEncoder()
				.encodeToString(binaryCipherText);

			return cipherText;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			throw new RSAServiceException(e.getMessage(), e);
		}
	}

	@Override
	public String decrypt(String cipherText, PrivateKey privateKey) throws RSAServiceException {

		try {
			byte[] binaryCipherText = Base64.getDecoder()
				.decode(cipherText);

			Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, privateKey);

			byte[] decryptedBinaryCipherText = cipher.doFinal(binaryCipherText);
			String decryptedCipherText = new String(decryptedBinaryCipherText);

			return decryptedCipherText;
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			throw new RSAServiceException(e.getMessage(), e);
		}
	}

	@Override
	public String sign(String plainText, PrivateKey privateKey) throws RSAServiceException {
		try {
			byte[] binaryPlainText = plainText.getBytes();

			Signature privateSignature = Signature.getInstance(SIGNATURE_ALGORITHM);
			privateSignature.initSign(privateKey);
			privateSignature.update(binaryPlainText);

			byte[] binarySignature = privateSignature.sign();
			String signature = Base64.getEncoder()
				.encodeToString(binarySignature);

			return signature;
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			throw new RSAServiceException(e.getMessage(), e);
		}
	}

	@Override
	public boolean verify(String plainText, String signature, PublicKey publicKey) throws RSAServiceException {
		try {
			byte[] binaryPlainText = plainText.getBytes();
			byte[] binarySignature = Base64.getDecoder()
				.decode(signature);

			Signature publicSignature = Signature.getInstance(SIGNATURE_ALGORITHM);
			publicSignature.initVerify(publicKey);
			publicSignature.update(binaryPlainText);

			boolean valid = publicSignature.verify(binarySignature);

			return valid;
		} catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
			throw new RSAServiceException(e.getMessage(), e);
		}
	}

}
