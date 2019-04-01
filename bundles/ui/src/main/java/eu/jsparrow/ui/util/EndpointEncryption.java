package eu.jsparrow.ui.util;

/**
 * Provides implementation for encrypting and decrypting of the endpoint URL and
 * the license key.
 * 
 * @since 3.4.0
 *
 */
public class EndpointEncryption {

	public String decryptEndpoint(String encrypted) {
		/*
		 * TODO: implement decryption algorithm
		 */
		String[] parts = encrypted.split("-");
		if (parts.length < 2) {
			return "";
		}
		return parts[1];
	}

	public String decryptKey(String key) {
		/*
		 * TODO: implement the decryption algorithm
		 */
		String[] parts = key.split("-");
		if (parts.length < 2) {
			return "";
		}
		return parts[0];
	}

	public boolean isEncryptedKey(String key) {
		/*
		 * TODO: check if the key matches the pattern of the key encryption
		 */
		return key.matches("[A-Z0-9]{9}-[a-z0-9:\\/\\.]+"); //$NON-NLS-1$
	}

}
