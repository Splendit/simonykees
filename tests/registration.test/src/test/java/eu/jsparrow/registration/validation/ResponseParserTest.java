package eu.jsparrow.registration.validation;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.registration.validation.response.ActivateResponse;
import eu.jsparrow.registration.validation.response.RegisterResponse;
import eu.jsparrow.registration.validation.response.ResponseParser;

@SuppressWarnings("nls")
public class ResponseParserTest {
	
	private ResponseParser responseParser;

	@BeforeEach
	public void setUp() {
		responseParser = new ResponseParser();
	}
	
	@Test
	public void parseRegisterResponse_validRespnseBody_deserializeModel() throws IOException {
		String validResponseBody = "{ \"created\": true, \"message\": \"success\" }";
		
		RegisterResponse registerResponse = responseParser.parseRegisterResponse(validResponseBody);
		
		assertTrue(registerResponse.isCreated());
		assertEquals("success", registerResponse.getMessage());
	}
	
	@Test
	public void parseActivateResponse_validResponseBody_shouldParseActivateResponse() throws IOException {
		String now = Instant.now().toString();
		String validResponseBody = "{ \"active\": true, \"message\": \"success\", \"activationTimestamp\": \""+ now + "\" }";
		
		ActivateResponse activateResponse = responseParser.parseActivateResponse(validResponseBody);
		
		assertTrue(activateResponse.isActive());
		assertEquals("success", activateResponse.getMessage());
		assertEquals(now, activateResponse.getActivationTimestamp());
	}

	@Test
	public void parseActivateResponse_emptyResponseBody_shouldThrowException() throws IOException {
		String validResponseBody = "";

		assertThrows(IOException.class, () -> responseParser.parseActivateResponse(validResponseBody));
	}
	
	@Test
	public void parseRegisterResponse_emptyValue_shouldThrowException() throws IOException {
		String validResponseBody = "";
		
		assertThrows(IOException.class, () -> responseParser.parseActivateResponse(validResponseBody));
	}

	@Test
	public void parseActivateResponse_invalidResponseBody_shouldThrowException() throws IOException {
		String validResponseBody = "invlid-json-value";

		assertThrows(IOException.class, () -> responseParser.parseActivateResponse(validResponseBody));
	}
}
