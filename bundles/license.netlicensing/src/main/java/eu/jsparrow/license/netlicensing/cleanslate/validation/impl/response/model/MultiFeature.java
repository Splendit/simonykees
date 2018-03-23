package eu.jsparrow.license.netlicensing.cleanslate.validation.impl.response.model;

public class MultiFeature extends LicensingModel {
	public static final String LICENSING_MODEL = "MultiFeature";  //$NON-NLS-1$
	
	private String featureName;
	
	public MultiFeature(String featureName, boolean valid) {
		super(valid);
		this.featureName = featureName;
	}
	
	public String getFeatureName() {
		return featureName;
	}
}
