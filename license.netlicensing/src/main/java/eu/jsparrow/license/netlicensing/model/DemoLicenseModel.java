package eu.jsparrow.license.netlicensing.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

public class DemoLicenseModel implements Serializable {

	private Date activationDate;

	public DemoLicenseModel() {
		this.activationDate = Date.from(Instant.now());
	}

	public DemoLicenseModel(Date activationDate) {
		this.activationDate = activationDate;
	}

	public Date getActivationDate() {
		return activationDate;
	}

	public void setActivationDate(Date activationDate) {
		this.activationDate = activationDate;
	}

	public static byte[] serialize(DemoLicenseModel model) throws IOException {
		final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		final ObjectOutputStream objectOutputStream;
		objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
		objectOutputStream.writeObject(model);
		objectOutputStream.close();
		return byteArrayOutputStream.toByteArray();
	}

	public static DemoLicenseModel deserialize(byte[] data) throws Exception {
		try {
			final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
			final ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

			@SuppressWarnings({ "unchecked" })
			final DemoLicenseModel obj = (DemoLicenseModel) objectInputStream.readObject();
			objectInputStream.close();
			return obj;
		} catch (IOException e) {
			throw new Error(e);
		} catch (ClassNotFoundException e) {
			throw new Exception(e);
		}
	}

}
