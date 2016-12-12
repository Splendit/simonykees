package at.splendit.simonykees.core.license;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.SecurityMode;

/**
 * a singleton pattern to connect to NetLicensing API (RESTful).
 * @author elham.cheriki
 * @since 0.9.2
 */

public class RestApiConnection {
	private static final String REST_API_PATH = "/core/v2/rest";
	private static final String BASE_URL_PROD = "https://go.netlicensing.io";
	private static final String PASS_APIKEY = "bf7f1092-3c88-492f-8a52-d00823d225a8";
	private static final String BASE_URL = BASE_URL_PROD + REST_API_PATH;
	private static final Context context = new Context();
	private static RestApiConnection instance;

	private RestApiConnection() {

	}

	public synchronized static RestApiConnection getAPIRestConnection() {

		if (instance == null) {
			instance = new RestApiConnection();
		}
		return instance;
	}

	public synchronized Context getContext() {
		context.setBaseUrl(BASE_URL);
		context.setSecurityMode(SecurityMode.APIKEY_IDENTIFICATION);
		context.setApiKey(PASS_APIKEY);
		return context;

	}

}
