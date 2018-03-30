package eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model;

public class MultiFeatureResponse extends NetlicensingResponse {
	public static final String LICENSING_MODEL = "MultiFeature";  //$NON-NLS-1$
	
	private String featureName;
	
	public MultiFeatureResponse(String featureName, boolean valid) {
		super(valid);
		this.featureName = featureName;
	}
	
	public String getFeatureName() {
		return featureName;
	}
}
