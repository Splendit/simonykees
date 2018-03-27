package eu.jsparrow.license.netlicensing.cleanslate.persistence;

import eu.jsparrow.license.netlicensing.cleanslate.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.cleanslate.exception.ValidationException;

public interface IEncryption {

	byte[] encrypt(byte[] data) throws PersistenceException;
	
	byte[] decrypt(byte[] encryptedData) throws PersistenceException;
}
