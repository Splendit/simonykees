package eu.jsparrow.license.netlicensing.cleanslate.persistence;

import eu.jsparrow.license.netlicensing.cleanslate.model.ValidationException;

public interface IEncryption {

	byte[] encrypt(byte[] data) throws ValidationException;
	
	byte[] decrypt(byte[] encryptedData) throws ValidationException;
}
