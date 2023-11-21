package eu.jsparrow.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

/**
 * File that contains permanent configuration for the standalone. Things like:
 * <ul>
 * <li>which license mode to use</li>
 * <li>which license key to use</li>
 * </ul>
 * 
 * @author Hans-Jörg Schrödl
 *
 */
public class YAMLStandaloneConfig {

	private String key;
	private String url;

	public YAMLStandaloneConfig() {
		key = ""; //$NON-NLS-1$
		url = ""; //$NON-NLS-1$
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public static YAMLStandaloneConfig load(File file) throws YAMLStandaloneConfigException {
		YAMLStandaloneConfig config = null;
		try (FileInputStream fis = new FileInputStream(file)) {
			Yaml yaml = new Yaml();
			config = yaml.loadAs(fis, YAMLStandaloneConfig.class);
		} catch (IOException e) {
			// Config not created, return default config
			return new YAMLStandaloneConfig();
		} catch (YAMLException e) {
			// Config malformed
			throw new YAMLStandaloneConfigException(e.getLocalizedMessage(), e);
		}
		return config;
	}
}
