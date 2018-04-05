package eu.jsparrow.license.netlicensing.persistence;

import java.io.*;
import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.ExceptionMessages;
import eu.jsparrow.license.api.LicenseModel;
import eu.jsparrow.license.api.exception.PersistenceException;

public class ModelSerializer {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
			.lookupClass());
	
	private ModelSerializer() {}
	
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
