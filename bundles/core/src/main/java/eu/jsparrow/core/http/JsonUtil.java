package eu.jsparrow.core.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Provides methods for serializing objects to JSON and vice versa. Additionally
 * it contains methods for sending statistics data.
 * 
 * @sicne 2.7.0
 *
 */
public class JsonUtil {

	private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

	private JsonUtil() {

	}

	/**
	 * 
	 * @param o
	 * @return returns Json String representation of Object o. returns null if
	 *         any error occurs.
	 */
	public static String generateJSON(Object o) {
		try {

			ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	/**
	 * Write the Json object to a new file.
	 * 
	 * @param value
	 *            the Json object.
	 * @param path
	 *            the file path.
	 */
	public static void writeJSON(Object value, String path) {
		try {
			File resultFile = new File(path);
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(resultFile, value);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static String generateFormatedJSON(Object o) {
		try {

			ObjectMapper mapper = new ObjectMapper();
			return mapper.writerWithDefaultPrettyPrinter()
				.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	private static void sendJson(String json, String targetUrl) {
		URL myurl;
		try {
			myurl = new URL(targetUrl);
			HttpURLConnection con = (HttpURLConnection) myurl.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);

			con.setRequestProperty("Content-Type", "application/json;"); //$NON-NLS-1$ //$NON-NLS-2$
			con.setRequestProperty("Accept", "application/json,text/plain"); //$NON-NLS-1$ //$NON-NLS-2$
			con.setRequestProperty("Method", "POST"); //$NON-NLS-1$ //$NON-NLS-2$
			OutputStream os;
			os = con.getOutputStream();

			os.write(json.getBytes(StandardCharsets.UTF_8.name()));
			os.close();

			StringBuilder sb = new StringBuilder();
			int httpResult = con.getResponseCode();
			if (httpResult == HttpURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8.name()));

				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line + System.lineSeparator());
				}
				br.close();
				logger.debug(sb.toString());

			} else {
				logger.debug("Response code: " + con.getResponseCode()); //$NON-NLS-1$
				logger.debug("Response message: " + con.getResponseMessage()); //$NON-NLS-1$
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public static void sendJsonToAwsStatisticsService(String json) {
		String targeturl = "https://nxiikrr4xl.execute-api.eu-central-1.amazonaws.com/testing/post-new-data"; //$NON-NLS-1$

		sendJson(json, targeturl);
	}
}
