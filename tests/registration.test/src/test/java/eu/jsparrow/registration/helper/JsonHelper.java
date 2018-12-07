package eu.jsparrow.registration.helper;

import java.lang.invoke.MethodHandles;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.jsparrow.registration.validation.response.ActivateResponse;
import eu.jsparrow.registration.validation.response.RegisterResponse;

@SuppressWarnings("nls")
public class JsonHelper {
	private static Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().getClass());
	
	public static String toJson(Object object) {
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			logger.error("Cannot serialize the given object {} to json", object);
		}
		return "";
	}
	
	public static String toJson(String key, String value) {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectNode node = objectMapper.createObjectNode();
		node.put(key, value);
		return node.toString();
	}
	
	public static String getRegisterResponseBody(boolean success, String message) {
		RegisterResponse successRegister = new RegisterResponse(success, message);
		return toJson(successRegister);
	}

	public static String getActivationResponseBody(boolean success, Instant instant, String message) {
		ActivateResponse successRegister = new ActivateResponse(success, instant.toString(), message);
		return toJson(successRegister);
	}
}
