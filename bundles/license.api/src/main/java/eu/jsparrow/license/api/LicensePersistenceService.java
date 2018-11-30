package eu.jsparrow.license.api;

import eu.jsparrow.license.api.exception.PersistenceException;

/**
 * Implementors provide methods to save or load a {@link LicenseModel} from some
 * persistent storage.
 */
public interface LicensePersistenceService<T> {

	/**
	 * Load a model from the persistent storage.
	 * 
	 * @return a model loaded from the persistent storage
	 * @throws PersistenceException if loading the model failed
	 */
	public T loadFromPersistence() throws PersistenceException;

	/**
	 * Save a model to the persistent storage. 
	 * 
	 * @param model the model to save to the persistent storage
	 * @throws PersistenceException if saving the model failed
	 */
	public void saveToPersistence(T model) throws PersistenceException;
}
