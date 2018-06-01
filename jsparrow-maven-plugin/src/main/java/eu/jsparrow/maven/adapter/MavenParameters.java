package eu.jsparrow.maven.adapter;

import java.util.Optional;

/**
 * A class for wrapping the parameters injected in the maven plugin.
 * 
 * @author Ardit Ymeri
 * @since 2.5.0
 *
 */
public class MavenParameters {

	private String profile = ""; //$NON-NLS-1$
	private String mode;
	private boolean useDefaultConfig = false;
	private String ruleId;
	private String license = ""; //$NON-NLS-1$
	private String url = ""; //$NON-NLS-1$
	private boolean devMode;

	public MavenParameters(String mode, String license, String url, String profile, boolean useDefault,
			boolean devMode) {
		this(mode, license, url);
		this.profile = profile;
		this.useDefaultConfig = useDefault;
		this.devMode = devMode;
	}

	public MavenParameters(String mode, String license, String url) {
		this(mode);
		this.license = license;
		this.url = url;
	}

	public MavenParameters(String mode) {
		this.mode = mode;
	}

	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}

	public String getProfile() {
		return profile;
	}

	public String getMode() {
		return mode;
	}

	public boolean getUseDefaultConfig() {
		return useDefaultConfig;
	}

	public Optional<String> getRuleId() {
		return Optional.ofNullable(ruleId)
			.filter(s -> !s.isEmpty());
	}

	public String getLicense() {
		return license;
	}

	public String getUrl() {
		return url;
	}

	public boolean isDevMode() {
		return devMode;
	}

}
