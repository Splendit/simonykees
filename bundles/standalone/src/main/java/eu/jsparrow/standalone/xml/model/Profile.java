package eu.jsparrow.standalone.xml.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

/**
 * XML model class used to parse Eclipse formatter files.
 * 
 * @since 3.23.0
 */
public class Profile {

	private String kind;
	private String name;
	private int version;

	@JsonProperty("setting")
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<Setting> settings = new ArrayList<>();

	public String getKind() {
		return kind;
	}

	public String getName() {
		return name;
	}

	public int getVersion() {
		return version;
	}

	public List<Setting> getSettings() {
		return settings;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "Profile{" + "kind='" + kind + '\'' + ", name='" + name + '\'' + ", version=" + version + ", settings="
				+ settings + '}';
	}
}