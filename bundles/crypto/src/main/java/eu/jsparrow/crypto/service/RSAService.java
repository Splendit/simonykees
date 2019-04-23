package eu.jsparrow.crypto.service;

import java.security.PrivateKey;
import java.security.PublicKey;

import eu.jsparrow.crypto.exception.RSAServiceException;

/**
 * Service for encryption, decryption, signature and verification using RSA
 * 
 * @since 3.5.0
 */
public interface RSAService {

	/**
	 * encrypts the {@code plainText} with the {@code publicKey} and returns the
	 * encrypted cipher text
	 * 
	 * @param plainText
	 * @param publicKey
	 * @return encrypted cipher text
	 * @throws RSAServiceException
	 */
	String encrypt(String plainText, PublicKey publicKey) throws RSAServiceException;

	/**
	 * decrypts the {@code cipherText} with the {@code privateKey} and returns
	 * the unencrypted plain text
	 * 
	 * @param cipherText
	 * @param privateKey
	 * @return unencrypted plain text
	 * @throws RSAServiceException
	 */
	String decrypt(String cipherText, PrivateKey privateKey) throws RSAServiceException;

	/**
	 * Signs the {@code plainText} with the {@code privateKey} and returns the
	 * signature
	 * 
	 * @param plainText
	 * @param privateKey
	 * @return signature
	 * @throws RSAServiceException
	 */
	String sign(String plainText, PrivateKey privateKey) throws RSAServiceException;

	/**
	 * Verifies the {@code signature} using the {@code publicKey}
	 * 
	 * @param plainText
	 * @param signature
	 * @param publicKey
	 * @return true, if the signature is valid, false otherwise
	 * @throws RSAServiceException
	 */
	boolean verify(String plainText, String signature, PublicKey publicKey) throws RSAServiceException;
}
