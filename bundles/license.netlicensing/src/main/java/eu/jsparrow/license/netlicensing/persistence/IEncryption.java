package eu.jsparrow.license.netlicensing.persistence;

import eu.jsparrow.license.api.exception.PersistenceException;

public interface IEncryption {

	byte[] encrypt(byte[] data) throws PersistenceException;
	
	byte[] decrypt(byte[] encryptedData) throws PersistenceException;
}
