package eu.jsparrow.ui.markers;

import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.ui.Activator;

public class HighlightColorPicker {
	
	private static final Logger logger = LoggerFactory.getLogger(HighlightColorPicker.class);

	private static final String DEFAULT_HIGHLIGHT_COLOR = "57,59,52";
	
	public static String calcThemeHighlightColor(IThemeEngine themeEngine) {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode("org.eclipse.ui.editors");

		try {
			List<ITheme> themes = themeEngine.getThemes();
			ITheme activeTheme = themeEngine.getActiveTheme();
			String activeThemeLabel = activeTheme.getLabel();
			String lowerCase = activeThemeLabel.toLowerCase();
			if(lowerCase.contains("dark")) { //$NON-NLS-1$
//				return "57,59,52"; //$NON-NLS-1$
				String current_value = preferences.get("jsparrow.marker.color", "");
				String currentLineColor = preferences.get("currentLineColor", "");

				try {
					preferences.flush();
					preferences.flush();
					preferences.flush();
					preferences.put("jsparrow.marker.color", currentLineColor);
					preferences.flush();
					preferences.flush();
					preferences.flush();
				} catch (BackingStoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				String new_value = preferences.get("jsparrow.marker.color", "");
				return currentLineColor;
			}
			preferences.put("jsparrow.marker.color", "232,242,254");
			return "232,242,254"; //$NON-NLS-1$
		} catch (RuntimeException e) {
			logger.error("Cannot find Current CSS Theme. Using default jSparrow Marker Highlight Color", e);
		}
		preferences.put("jsparrow.marker.color", DEFAULT_HIGHLIGHT_COLOR);
		return DEFAULT_HIGHLIGHT_COLOR;

	}

}
