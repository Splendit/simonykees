package eu.jsparrow.crypto.service;

/**
 * enum representing diferent types for the java key store
 * 
 * @since 3.4.0
 */
@SuppressWarnings("nls")
public enum KeyStoreType {

	TYPE_JKS("jks"),
	TYPE_JCEKS("jceks"),
	TYPE_DKS("dks"),
	TYPE_PKCS11("pkcs11"),
	TYPE_PKCS12("pkcs12");

	private String type;

	KeyStoreType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
