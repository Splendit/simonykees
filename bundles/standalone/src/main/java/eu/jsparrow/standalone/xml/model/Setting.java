package eu.jsparrow.standalone.xml.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * XML model class used to parse Eclipse formatter files.
 * 
 * @since 3.23.0
 */
@XmlRootElement(name = "setting")
@XmlAccessorType(XmlAccessType.FIELD)
public class Setting {

	@XmlAttribute
	private String id;
	@XmlAttribute
	private String value;

	public String getId() {
		return id;
	}

	public String getValue() {
		return value;
	}

	@SuppressWarnings("nls")
	@Override
	public String toString() {
		return "Setting{" +
				"id='" + id + '\'' +
				", value='" + value + '\'' +
				'}';
	}
}
