package eu.jsparrow.ui.wizard.impl;

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
	
	public StyleContainer(String value, Font font, Color color) {
		this(value, font);
		this.color=color;
	}
	
	public StyleContainer(String value, Font font, Color color, boolean enabled) {
		this(value, font, color);
		this.enabled=enabled;
	}
	
	private String value;
	private Font font;
	private Color color;
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
	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
