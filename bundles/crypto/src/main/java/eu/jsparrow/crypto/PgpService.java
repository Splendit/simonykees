package eu.jsparrow.crypto;

public interface PgpService {

	String decrypt(String message, String key);
	
	String encrypt(String message, String key);
	
	String sign(String message, String key);
	
	String verifySignature(String message, String key);
}
