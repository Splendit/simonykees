package eu.jsparrow.license.netlicensing;

import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.exception.PersistenceException;

/**
 * Implementors provide capabilities to save or load a {@link LicenseModel} from
 * some storage location.
 */
public interface LicensePersistence {

	/**
	 * Load a license model from the persistent storage.
	 * 
	 * @return the license model loaded from the storage
	 * @throws PersistenceException
	 *             if the license model could not be loaded
	 */
	LicenseModel load() throws PersistenceException;

	/**
	 * Saves a given license model to the persistent storage.
	 * 
	 * @param model
	 *            model to save
	 * @throws PersistenceException
	 *             if the license model could not be saved
	 */
	void save(LicenseModel model) throws PersistenceException;
}
