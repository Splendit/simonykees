package eu.jsparrow.registration.validation;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyString;

import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.registration.helper.DummyRegistrationModel;

public class RegisterRequestTest {
	
	private RegisterRequest registerRequest;
	HttpClientWrapper httpClientWrapper;
	
	@Before
	public void setUp() {
		httpClientWrapper = mock(HttpClientWrapper.class);
		registerRequest = new RegisterRequest(httpClientWrapper);
	}
	
	@Test
	public void sendRegisterRequest_dummyRegistrationModel_shouldInvokePost() throws Exception {
		DummyRegistrationModel model = new DummyRegistrationModel();
		registerRequest.sendRegisterRequest(model);
		verify(httpClientWrapper).post(anyString(), anyString());
	}
	
	@Test
	public void sendActivateRequest_dummyRegistrationModel_shouldInvokePost() throws Exception {
		DummyRegistrationModel model = new DummyRegistrationModel();
		registerRequest.sendActivateRequest(model);
		verify(httpClientWrapper).post(anyString(), anyString());
	}

}
