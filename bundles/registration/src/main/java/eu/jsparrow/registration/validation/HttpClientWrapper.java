package eu.jsparrow.registration.validation;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

import eu.jsparrow.license.api.exception.ValidationException;

/**
 * A wrapper for {@link HttpClient}. Contains functionality for posting a HTTP request.  
 * 
 * @since 3.0.0
 *
 */
public class HttpClientWrapper {
	
	private static final String CONTENT_TYPE = "application/json"; //$NON-NLS-1$
	private static final String ENCODING = "UTF-8"; //$NON-NLS-1$
	
	public String post(String jsonBody, String url) throws ValidationException {
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(url);
		client.getParams().setContentCharset(ENCODING);
		
		method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, ENCODING);
		RequestEntity requestEntity;
		try {
			requestEntity = new StringRequestEntity(jsonBody, CONTENT_TYPE, ENCODING);
		} catch (UnsupportedEncodingException e) {
			throw new ValidationException("Failed to create request body", e); //$NON-NLS-1$
		}
		method.setRequestEntity(requestEntity);
		method.releaseConnection();
		
		try {
			int status = client.executeMethod(method);
			if(status != HttpStatus.SC_OK) {
				String message = String.format("Unexpected status code %s", status); //$NON-NLS-1$
				throw new ValidationException(message);
			}
		} catch (IOException e) {
			throw new ValidationException("Failed to send post request", e); //$NON-NLS-1$
		}
		
		String response;
		try {
			response = method.getResponseBodyAsString();
		} catch (IOException e) {
			throw new ValidationException("Failed to read response body"); //$NON-NLS-1$
		}
		return response;
	}

}
