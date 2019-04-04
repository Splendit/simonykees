package eu.jsparrow.crypto.service;

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
