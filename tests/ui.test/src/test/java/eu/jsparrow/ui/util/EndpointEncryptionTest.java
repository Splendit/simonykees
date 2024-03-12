package eu.jsparrow.ui.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

@SuppressWarnings("nls")
public class EndpointEncryptionTest {

	private static final String EXPECTED_KEY = "ABCDEFGIH";
	private static final String EXPECTED_ENDPOINT_URL = "https://jsparrow.eu/netlicensing";

	private EndpointEncryption endpointEncryption;

	private String encrypted;

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Before
	public void setUp() throws Exception {
		try (InputStream inputStream = getClass().getResourceAsStream("/encrypted-key-endpoint.txt"); //$NON-NLS-1$
				InputStreamReader isr = new InputStreamReader(inputStream);
				BufferedReader br = new BufferedReader(isr)) {
			encrypted = br.lines()
				.collect(Collectors.joining());
		}

		endpointEncryption = new EndpointEncryption();
	}

	@Test
	public void decryptEndpoint_shouldReturnCorrectEndpoint() throws Exception {
		String endpoint = endpointEncryption.decryptEndpoint(encrypted);

		assertEquals(EXPECTED_ENDPOINT_URL, endpoint);
	}

	@Test
	public void decryptEndpoint_tooShortSignature_shouldThrowException() throws Exception {
		encrypted = prepareTooShortSignature(encrypted);
		expectedException.expect(EndpointEncryptionException.class);
		expectedException.expectMessage("Bad signature length: got 4 but was expecting 512");

		endpointEncryption.decryptEndpoint(encrypted);
	}

	@Test
	public void decryptEndpoint_invalidSignature_shouldThrowException() throws Exception {
		encrypted = prepareInvalidSignature(encrypted);
		expectedException.expect(EndpointEncryptionException.class);
		expectedException.expectMessage("Invalid signature");

		endpointEncryption.decryptEndpoint(encrypted);
	}

	@Test
	public void decryptKey_shouldReturnCorrectKey() throws Exception {
		String key = endpointEncryption.decryptKey(encrypted);

		assertEquals(EXPECTED_KEY, key);
	}

	@Test
	public void isEncryptedKey_shouldReturnTrue() throws Exception {
		boolean isEncryptedKey = endpointEncryption.isEncryptedKey(encrypted);

		assertTrue(isEncryptedKey);
	}

	@Test
	public void isEncryptedKey_randomStringWithoutSplitChar_shouldReturnFalse() throws Exception {
		encrypted = "orfkijndksjfoaihnjswfoihnjawoisoiwfoiwfjoijdsklfjaldjsklfkjasdfkljas";

		boolean isEncryptedKey = endpointEncryption.isEncryptedKey(encrypted);

		assertFalse(isEncryptedKey);
	}

	@Test
	public void isEncryptedKey_randomStringWithSplitChar_shouldReturnFalse() throws Exception {
		encrypted = "orfkijndksjfoaihnjswfoihnjawoi:soiwfoiwfjoijdsklfjaldjsklfkjasdfkljas";

		boolean isEncryptedKey = endpointEncryption.isEncryptedKey(encrypted);

		assertFalse(isEncryptedKey);
	}

	@Test
	public void isEncryptedKey_licensePartNotB64_shouldReturnFalse() throws Exception {
		encrypted = "asdf:" + Base64.getEncoder()
			.encode("asdf".getBytes());

		boolean isEncryptedKey = endpointEncryption.isEncryptedKey(encrypted);

		assertFalse(isEncryptedKey);
	}

	@Test
	public void isEncryptedKey_endpointPartNotB64_shouldReturnFalse() throws Exception {
		encrypted = Base64.getEncoder()
			.encode("asdf".getBytes()) + ":asdf";

		boolean isEncryptedKey = endpointEncryption.isEncryptedKey(encrypted);

		assertFalse(isEncryptedKey);
	}

	private String prepareTooShortSignature(String key) {
		String invalidSignature = Base64.getEncoder()
			.encodeToString("asdf".getBytes());
		String[] splitEncrypted = key.split(":");
		return splitEncrypted[0] + ":" + invalidSignature;
	}

	private String prepareInvalidSignature(String key) {
		String[] splitEncrypted = key.split(":");
		return splitEncrypted[0] + ":" + splitEncrypted[1].replace("+", "a");
	}
}
