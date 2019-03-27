package eu.jsparrow.registration.validation;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.registration.helper.DummyRegistrationModel;
import eu.jsparrow.registration.helper.JsonHelper;

public class RegisterRequestTest {

	private RegisterRequest registerRequest;
	HttpClientWrapper httpClientWrapper;
	
	@BeforeEach
	public void setUp() {
		httpClientWrapper = mock(HttpClientWrapper.class);
		registerRequest = new RegisterRequest(httpClientWrapper);
	}

	@Test
	public void sendRegisterRequest_dummyRegistrationModel_shouldInvokePost() throws Exception {
		DummyRegistrationModel model = new DummyRegistrationModel();
		String expectedPostBody = JsonHelper.toJson(model);

		registerRequest.sendRegisterRequest(model);

		verify(httpClientWrapper).post(eq(expectedPostBody), anyString());
	}

	@Test
	public void sendActivateRequest_dummyRegistrationModel_shouldInvokePost() throws Exception {
		String activationKey = "expected-activation-key"; //$NON-NLS-1$
		String expectedJsonBody = JsonHelper.toJson("activationKey", activationKey); //$NON-NLS-1$

		registerRequest.sendActivateRequest(activationKey);

		verify(httpClientWrapper).post(eq(expectedJsonBody), anyString());
	}

}
