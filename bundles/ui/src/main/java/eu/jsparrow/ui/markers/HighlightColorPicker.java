package eu.jsparrow.ui.markers;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.themes.ITheme;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static eu.jsparrow.ui.markers.JSparrowMarkerPropertyKeys.JSPARROW_MARKER_COLOR_KEY;

/**
 * Finds the highlight color of jSparrow markers based on the current line
 * highlight color.
 * 
 * @since 3.31.0
 *
 */
public class HighlightColorPicker {

	private static final Logger logger = LoggerFactory.getLogger(HighlightColorPicker.class);

	private HighlightColorPicker() {
		/**
		 * Hide default constructor
		 */
	}

	/**
	 * Computes the jSparrow Marker Highlight color and stores in the provided
	 * eclipse preferences.
	 * 
	 * @param preferences
	 *            the preferences store with path {@code org.eclipse.ui.editors}
	 * @param defaultLineColor
	 *            the default color if the given preferences node does not
	 *            contain {@value currentLineColor} node.
	 * @return the highlight color to be used.
	 */
	public static String calcThemeHighlightColor(IEclipsePreferences preferences, String defaultLineColor) {
		String currentLineColor = preferences.get("currentLineColor", defaultLineColor); //$NON-NLS-1$

		try {

			preferences.put(JSPARROW_MARKER_COLOR_KEY, currentLineColor);
			preferences.flush();
			
		} catch (BackingStoreException e) {
			logger.error("Cannot set the new jsparrow marker highlight color in the preference store", e); //$NON-NLS-1$
		}
		return currentLineColor;
	}

	/**
	 * 
	 * @param workbench
	 *            the Platform UI workbench
	 * @return the default theme highlight color for the current line.
	 */
	public static String findDefaultThemeColor(IWorkbench workbench) {
		ITheme currentTheme = workbench.getThemeManager()
			.getCurrentTheme();
		ColorRegistry colorRegistry = currentTheme.getColorRegistry();
		Color currentLineColor = colorRegistry.get("org.eclipse.ui.editors.currentLineColor"); //$NON-NLS-1$
		int r = currentLineColor.getRed();
		int g = currentLineColor.getGreen();
		int b = currentLineColor.getBlue();
		return String.format("%d,%d,%d", r, g, b); //$NON-NLS-1$
	}

}
