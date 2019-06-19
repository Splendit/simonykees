package eu.jsparrow.registration.validation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.license.api.exception.ValidationException;

/**
 * Contains functionality for posting a HTTP request.
 * 
 * @since 3.0.0
 *
 */
public class HttpClientWrapper {

	private static final String APPLICATION_JSON = "application/json"; //$NON-NLS-1$
	private static final String TEXT_PLAIN = "text/plain"; //$NON-NLS-1$
	private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
	private static final String POST = "POST"; //$NON-NLS-1$

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	public String post(String jsonBody, String url) throws ValidationException {
		URL endpoint;
		try {
			endpoint = new URL(url);
		} catch (MalformedURLException e) {
			logger.error(e.getMessage(), e);
			throw new ValidationException("Failed to create endpoint URL", e); //$NON-NLS-1$
		}

		HttpURLConnection connection;
		try {
			connection = (HttpURLConnection) endpoint.openConnection();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ValidationException(e.getMessage());
		}

		String accept = String.format("%s,%s", APPLICATION_JSON, TEXT_PLAIN); //$NON-NLS-1$
		String contentType = String.format("%s; charset=%s", APPLICATION_JSON, UTF_8); //$NON-NLS-1$
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestProperty("Content-Type", contentType); //$NON-NLS-1$
		connection.setRequestProperty("Accept", accept); //$NON-NLS-1$
		connection.setRequestProperty("Method", POST); //$NON-NLS-1$

		OutputStream outputStream;
		try {
			outputStream = connection.getOutputStream();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new ValidationException(e.getMessage());
		}

		try {
			outputStream.write(jsonBody.getBytes(StandardCharsets.UTF_8.name()));
		} catch (IOException e) {
			String message = String.format("Failed to create request body: %s", e.getMessage()); //$NON-NLS-1$
			throw new ValidationException(message, e);
		}

		int responseCode;
		try {
			responseCode = connection.getResponseCode();
		} catch (IOException e) {
			String message = String.format("Failed to read response status: %s", e.getMessage()); //$NON-NLS-1$
			throw new ValidationException(message);
		}

		if (responseCode != HttpURLConnection.HTTP_OK) {
			String message = String.format("Unexpected response status code %s", responseCode); //$NON-NLS-1$
			throw new ValidationException(message);
		}

		StringBuilder stringBuilder = new StringBuilder();

		try (BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8.name()))) {

			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line + System.lineSeparator());
			}

			return stringBuilder.toString();

		} catch (IOException e) {
			String message = String.format("Failed to read response body: %s", e.getMessage()); //$NON-NLS-1$
			throw new ValidationException(message, e);
		}
	}
}
