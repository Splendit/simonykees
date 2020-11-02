package eu.jsparrow.standalone.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * XML model class used to parse Eclipse formatter files.
 * 
 * @since 3.23.0
 */
@XmlRootElement(name = "profiles")
@XmlAccessorType(XmlAccessType.FIELD)
public class Profiles {

	@XmlElement(name = "profile")
	private List<Profile> profileList = new ArrayList<>();

	public List<Profile> getProfileList() {
		return profileList;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "Profiles{" +
				"profileList=" + profileList +
				'}';
	}
}