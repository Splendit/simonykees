package eu.jsparrow.license.api;

import eu.jsparrow.license.api.exception.PersistenceException;

/**
 * Implementors provide methods to save or load a {@link LicenseModel} from some
 * persistent storage.
 */
public interface LicensePersistenceService {

	/**
	 * Load a license model from the persistent storage.
	 * 
	 * @return a license model loaded from the persistent storage
	 * @throws PersistenceException if loading the model failed
	 */
	public LicenseModel loadFromPersistence() throws PersistenceException;

	/**
	 * Save a license model to the persistent storage. 
	 * 
	 * @param model the model to save to the persistent storage
	 * @throws PersistenceException if saving the model failed
	 */
	public void saveToPersistence(LicenseModel model) throws PersistenceException;
}
