package eu.jsparrow.standalone.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

/**
 * XML model class used to parse Eclipse formatter files.
 * 
 * @since 3.23.0
 */
@XmlRootElement(name = "profile")
@XmlAccessorType(XmlAccessType.FIELD)
public class Profile {

	@XmlAttribute
	private String kind;
	@XmlAttribute
	private String name;
	@XmlElement(name = "setting")
	private List<Setting> settings = new ArrayList<>();

	public String getKind() {
		return kind;
	}

	public String getName() {
		return name;
	}

	public List<Setting> getSettings() {
		return settings;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "Profile{" +
				"kind='" + kind + '\'' +
				", name='" + name + '\'' +
				", settings=" + settings +
				'}';
	}
}
