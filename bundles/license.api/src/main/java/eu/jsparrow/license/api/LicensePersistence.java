package eu.jsparrow.license.api;

import eu.jsparrow.license.api.exception.PersistenceException;

/**
 * Implementors provide capabilities to save or load a {@link LicenseModel} from
 * some storage location.
 */
public interface LicensePersistence<T> {

	/**
	 * Load a model from the persistent storage.
	 * 
	 * @return the model loaded from the storage
	 * @throws PersistenceException
	 *             if the license model could not be loaded
	 */
	T load() throws PersistenceException;

	/**
	 * Saves a given model to the persistent storage.
	 * 
	 * @param model
	 *            model to save
	 * @throws PersistenceException
	 *             if the model could not be saved
	 */
	void save(T model) throws PersistenceException;
}
