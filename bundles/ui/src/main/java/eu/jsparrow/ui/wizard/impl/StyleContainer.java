package eu.jsparrow.ui.wizard.impl;

import java.util.function.Consumer;

import org.eclipse.swt.custom.StyleRange;

/**
 * Simple Pojo that holds the properties for a SWT {@link StyleRange} and is
 * possible to create a {@link StyleRange} from it for the defined test at an
 * offset.
 * 
 * @author Martin Huter
 *
 */
public class StyleContainer {

	Consumer<StyleRange> additionalStyles = style -> {};
	private String value;
	private boolean enabled = true;

	public StyleContainer(String value) {
		this.value = value;
	}
	
	public StyleContainer(String value, Consumer<StyleRange> additionalStyles) {
		this(value);
		this.additionalStyles = additionalStyles;
	}
	
	public StyleContainer(String value, Consumer<StyleRange> additionalStyles, boolean enabled) {
		this(value, additionalStyles);
		this.enabled = enabled;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public StyleRange generateStyle(int offset) {
		StyleRange result = new StyleRange();
		result.start = offset;
		result.length = value.length();
		additionalStyles.accept(result);
		return result;
	}
}
