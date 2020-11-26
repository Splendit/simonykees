package eu.jsparrow.standalone.xml.model;

/**
 * XML model class used to parse Eclipse formatter files.
 *
 * @since 3.23.0
 */
public class Setting {

	private String id;
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
