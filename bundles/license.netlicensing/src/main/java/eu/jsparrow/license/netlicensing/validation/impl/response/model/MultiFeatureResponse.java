package eu.jsparrow.license.netlicensing.validation.impl.response.model;

import com.labs64.netlicensing.domain.vo.Composition;
import com.labs64.netlicensing.domain.vo.ValidationResult;

/**
 * Represents the information contained in the {@link Composition} corresponding
 * to a Multi-Feature Product Module of a NetLicensing'
 * {@link ValidationResult}.
 * 
 * @see <a href=
 *      "https://netlicensing.io/wiki/multi-feature">Multi-Feature
 *      License Model</a>
 *
 */
public class MultiFeatureResponse extends NetlicensingResponse {
	public static final String LICENSING_MODEL = "MultiFeature"; //$NON-NLS-1$

	private String featureName;

	public MultiFeatureResponse(String featureName, boolean valid) {
		super(valid);
		this.featureName = featureName;
	}

	public String getFeatureName() {
		return featureName;
	}
}
