package eu.jsparrow.license.netlicensing.persistence;

import java.io.*;
import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.exception.PersistenceException;

/**
 * This class is a utility class to serialize or deserialize
 * {@link LicenseModel} using byte streams.
 *
 */
public class ModelSerializer {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private ModelSerializer() {
	}

	/**
	 * Serialize the given object to a byte array.
	 * 
	 * @param model
	 *            model to serialize
	 * @return the model serialized as byte straem
	 * @throws PersistenceException
	 *             if the model could not be serialized
	 */
	static byte[] serialize(LicenseModel model) throws PersistenceException {
		logger.debug("Serializing {} to byte array", model); //$NON-NLS-1$
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(model);
			out.flush();
			return bos.toByteArray();
		} catch (IOException e) {
			throw new PersistenceException(ExceptionMessages.Netlicensing_persistenceException_couldNotSerialize, e);
		}
	}

	/**
	 * Deserialize the given data to a {@link LicenseModel}
	 * 
	 * @param data
	 *            a model as byte array
	 * @return the deserialized model
	 * @throws PersistenceException
	 *             if the byte array could not be deserialized
	 */
	static LicenseModel deserialize(byte[] data) throws PersistenceException {
		logger.debug("Deserializing data '{}' to license model", data); //$NON-NLS-1$
		try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
				ObjectInputStream in = new ObjectInputStream(bis)) {
			return (LicenseModel) in.readObject();
		} catch (ClassNotFoundException | IOException | ClassCastException e) {
			throw new PersistenceException(ExceptionMessages.Netlicensing_persistenceException_couldNotDeserialize, e);
		}
	}
}
