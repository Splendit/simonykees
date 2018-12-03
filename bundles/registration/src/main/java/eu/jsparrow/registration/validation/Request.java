package eu.jsparrow.registration.validation;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.jsparrow.license.api.RegistrationModel;


public class Request {
	
	private static final String CONTENT_TYPE = "application/json";
	private static final String ENCODING = "UTF-8";
	
	private static final String REGISTER_API_ENDPOINT = "";
	private static final String ACTIVATE_API_ENDPOINT = "";
	private static final String VALIDATE_API_ENDPOINT = "";
	

	public String sendRegisterRequest(RegistrationModel model) {
		String json = toJson(model);		
		return post(json, REGISTER_API_ENDPOINT);
	}

	public String sendActivateRequest(RegistrationModel model) {
		String json = toJson(model);
		return post(json, ACTIVATE_API_ENDPOINT);
	}

	public String sendValidateRequest(RegistrationModel model) {
		String json = toJson(model);
		return post(json, VALIDATE_API_ENDPOINT);
	}
	
	private String post(String body, String url) {
		HttpClient client = new HttpClient();
		PostMethod method = new PostMethod(url);
		client.getParams().setContentCharset(ENCODING);
		
		
		method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, ENCODING);
		RequestEntity requestEntity;
		try {
			requestEntity = new StringRequestEntity(body, CONTENT_TYPE, ENCODING);
		} catch (UnsupportedEncodingException e) {
			//TODO: log the exception
			return "";
		}
		method.setRequestEntity(requestEntity);
		method.releaseConnection();
		
		try {
			int status = client.executeMethod(method);
		} catch (IOException e) {
			
			return "";
		}
		
		String response;
		try {
			response = method.getResponseBodyAsString();
		} catch (IOException e) {
			
			return "";
		}
		return response;
	}
	
	private String toJson(RegistrationModel model) {
		ObjectMapper objectMapper = new ObjectMapper();
		String json;
		try {
			json = objectMapper.writeValueAsString(model);
		} catch (JsonProcessingException e) {
			//TODO: log the exception
			return "";
		}
		return json;
	}
}
