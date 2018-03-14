package eu.jsparrow.standalone;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;

import com.google.common.base.Strings;

import eu.jsparrow.core.config.YAMLConfig;
import eu.jsparrow.core.config.YAMLConfigException;
import eu.jsparrow.core.config.YAMLProfile;

/**
 * File that contains permanent configuration for the standalone. Things like: 
 * <ul>
 * <li>which license mode to use</li>
 * <li>which license key to use</li>
 * </ul>  
 * @author Hans-Jörg Schrödl
 *
 */
public class YAMLStandaloneConfig {
		
	private String key;
	
	public YAMLStandaloneConfig() {
		key = ""; //$NON-NLS-1$
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
	
	public static YAMLStandaloneConfig load(File file) throws YAMLStandaloneConfigException{
		YAMLStandaloneConfig config = null;
		try (FileInputStream fis = new FileInputStream(file)) {
			Yaml yaml = new Yaml();
			config = yaml.loadAs(fis, YAMLStandaloneConfig.class);
		}
		catch (IOException e) {
			// Config not created, return default config
			return new YAMLStandaloneConfig();
		} catch (YAMLException e) {
			// Config destroyed
			throw new YAMLStandaloneConfigException(e.getLocalizedMessage(), e);
		}
		return config;
	}
}
