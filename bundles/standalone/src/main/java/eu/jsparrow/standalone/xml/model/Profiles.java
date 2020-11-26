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
public class Profiles {

	private int version;

	@JsonProperty("profile")
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<Profile> profileList = new ArrayList<>();

	public int getVersion() {
		return version;
	}

	public List<Profile> getProfileList() {
		return profileList;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "Profiles{" + "version=" + version + ", profileList=" + profileList + '}';
	}
}