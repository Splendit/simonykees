package eu.jsparrow.registration.validation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.time.Instant;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import eu.jsparrow.registration.validation.response.ActivateResponse;
import eu.jsparrow.registration.validation.response.RegisterResponse;

@SuppressWarnings("nls")
public class ResponseParserTest {
	
	private ResponseParser responseParser;
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Before
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
	public void parseRegisterResponse_emptyValue_shouldThrowException() throws IOException {
		String validResponseBody = "";
		
		thrown.expect(IOException.class);
		responseParser.parseRegisterResponse(validResponseBody);
		
		fail();
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
		
		thrown.expect(IOException.class);
		responseParser.parseActivateResponse(validResponseBody);
		
		fail();
	}
	
	@Test
	public void parseActivateResponse_invalidResponseBody_shouldThrowException() throws IOException {
		String validResponseBody = "invlid-json-value";
		
		thrown.expect(IOException.class);
		responseParser.parseActivateResponse(validResponseBody);
		
		fail();
	}
}
