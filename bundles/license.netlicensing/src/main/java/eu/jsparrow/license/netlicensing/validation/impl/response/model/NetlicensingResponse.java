package eu.jsparrow.license.netlicensing.validation.impl.response.model;

import com.labs64.netlicensing.domain.vo.Composition;

/**
 * The common information contained in the {@link Composition}s of a
 * NetLicensing Product Modules.
 *
 */
public abstract class NetlicensingResponse {

	public static final String VALID_KEY = "valid"; //$NON-NLS-1$

	private boolean valid;

	protected NetlicensingResponse(boolean valid) {
		this.valid = valid;
	}

	public boolean isValid() {
		return valid;
	}
}
