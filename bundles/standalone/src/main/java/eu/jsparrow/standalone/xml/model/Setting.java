package eu.jsparrow.standalone.xml.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * XML model class used to parse Eclipse formatter files.
 *
 * @since 3.23.0
 */
public class Setting {

	@JsonProperty("id")
	private String id;
	
	@JsonProperty("value")
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
		return "Setting{" + "id='" + id + '\'' + ", value='" + value + '\'' + '}';
	}
}
