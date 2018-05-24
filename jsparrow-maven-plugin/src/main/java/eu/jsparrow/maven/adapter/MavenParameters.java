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

	private String profile;
	private String mode;
	private boolean useDefaultConfig;
	private String ruleId;
	private String license;
	private String url;
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

	public Optional<String> getProfile() {
		return Optional.ofNullable(profile);
	}

	public String getMode() {
		return mode;
	}

	public Optional<Boolean> getUseDefaultConfig() {
		return Optional.ofNullable(useDefaultConfig);
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
