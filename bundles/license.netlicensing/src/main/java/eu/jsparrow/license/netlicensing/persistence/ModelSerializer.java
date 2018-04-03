package eu.jsparrow.license.netlicensing.persistence;

import java.io.*;
import java.lang.invoke.MethodHandles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.netlicensing.exception.PersistenceException;
import eu.jsparrow.license.netlicensing.model.LicenseModel;

public class ModelSerializer {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
			.lookupClass());
	
	static byte[] serialize(LicenseModel model) throws PersistenceException {
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); ObjectOutput out = new ObjectOutputStream(bos)) {
			out.writeObject(model);
			out.flush();
			return bos.toByteArray();
		} catch (IOException e) {
			logger.error("Failed to serialize licenseModel {}", model, e);
			throw new PersistenceException(e);
		}
	}

	static LicenseModel deserialize(byte[] data) throws PersistenceException {
		try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
				ObjectInputStream in = new ObjectInputStream(bis)) {
			return (LicenseModel) in.readObject();
		} catch (ClassNotFoundException | IOException | ClassCastException e) {
			logger.error("Failed to deserialize to licensemodel", e);
			throw new PersistenceException(e);
		}
	}
}
