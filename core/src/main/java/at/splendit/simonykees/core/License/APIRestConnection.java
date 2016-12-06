package at.splendit.simonykees.core.License;

import com.labs64.netlicensing.domain.vo.Context;
import com.labs64.netlicensing.domain.vo.SecurityMode;

/**
 * a singleton pattern to connect to NetLicensing API (RESTful).
 * @author elham.cheriki
 * @since 0.9.2
 */

public class APIRestConnection {
	private static final String REST_API_PATH = "/core/v2/rest";
	private static final String BASE_URL_PROD = "https://go.netlicensing.io";
	private static final String PASS_APIKEY = "5e6b99b7-4a6f-4a33-b94e-67f3552e0925";
	private static final String BASE_URL = BASE_URL_PROD + REST_API_PATH;
	private static final Context context = new Context();
	private static APIRestConnection instance;

	private APIRestConnection() {

	}

	public synchronized static APIRestConnection getAPIRestConnection() {

		if (instance == null) {
			instance = new APIRestConnection();
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
