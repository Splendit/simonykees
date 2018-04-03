package eu.jsparrow.license.netlicensing.persistence;

import eu.jsparrow.license.netlicensing.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.exception.ValidationException;

public interface IEncryption {

	byte[] encrypt(byte[] data) throws PersistenceException;
	
	byte[] decrypt(byte[] encryptedData) throws PersistenceException;
}
