package eu.jsparrow.registration.validation;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
	public void parseRegisterResponse_emptyValue_shouldThrowException() throws IOException {
		String validResponseBody = "";
		
		assertThrows(IOException.class, () -> responseParser.parseRegisterResponse(validResponseBody));
	}
	
	/**
	 * Parameters for following Test
	 * @return
	 * @throws Exception
	 */
	public static Stream<Arguments> response() throws Exception {
		return Stream.of(
					Arguments.of(""),
					Arguments.of("invlid-json-value")
				);
	}

	@ParameterizedTest(name = "{index}: test with responseBody:[{0}]")
	@MethodSource("response")
	public void parseActivateResponse_emptyResponseBody_shouldThrowException(String validResponseBody) throws IOException {
		assertThrows(IOException.class, () -> responseParser.parseActivateResponse(validResponseBody));
	}
}
