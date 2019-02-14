package eu.jsparrow.ui.wizard.impl;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;

public class StyleContainer {
	
	public StyleContainer(String value) {
		this.value=value;
	}
	
	public StyleContainer(String value, Font font) {
		this(value);
		this.font=font;
	}
	
	public StyleContainer(String value, Font font, Color foreground) {
		this(value, font);
		this.foreground=foreground;
	}
	
	public StyleContainer(String value, Font font, Color foreground, boolean enabled) {
		this(value, font, foreground);
		this.enabled=enabled;
	}
	
	private String value;
	private Font font;
	private Color foreground;
	private boolean enabled = true;
	
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Font getFont() {
		return font;
	}
	public void setFont(Font font) {
		this.font = font;
	}
	public Color getForeground() {
		return foreground;
	}
	public void setForeground(Color foreground) {
		this.foreground = foreground;
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
		result.length = value
			.length();
		result.font = font;
		result.foreground = foreground;
		return result;
	}
}
