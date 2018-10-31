package eu.jsparrow.core.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtil {

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
			e.printStackTrace();
		}
		return null;
	}

	public static String generateFormatedJSON(Object o) {
		try {

			ObjectMapper mapper = new ObjectMapper();
			return mapper.writerWithDefaultPrettyPrinter()
				.writeValueAsString(o);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void sendJson(String json) {
		String targeturl = "https://nxiikrr4xl.execute-api.eu-central-1.amazonaws.com/testing/upload-data";

		URL myurl;
		try {
			myurl = new URL(targeturl);
			HttpURLConnection con = (HttpURLConnection) myurl.openConnection();
			con.setDoOutput(true);
			con.setDoInput(true);

			con.setRequestProperty("Content-Type", "application/json;");
			con.setRequestProperty("Accept", "application/json,text/plain");
			con.setRequestProperty("Method", "POST");
			OutputStream os;
			os = con.getOutputStream();

			os.write(json.toString()
				.getBytes("UTF-8"));
			os.close();

			StringBuilder sb = new StringBuilder();
			int HttpResult = con.getResponseCode();
			if (HttpResult == HttpURLConnection.HTTP_OK) {
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));

				String line = null;
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				br.close();
				System.out.println("" + sb.toString());

			} else {
				System.out.println(con.getResponseCode());
				System.out.println(con.getResponseMessage());
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
}
