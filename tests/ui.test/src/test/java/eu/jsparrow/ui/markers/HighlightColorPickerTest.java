package eu.jsparrow.ui.markers;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class HighlightColorPickerTest {

	@Test
	void testCalcHighlightColor_shouldReturnDarkColor() {
		IEclipsePreferences pref = mock(IEclipsePreferences.class);
		String defaultColor = "255,255,255";
		String expected = "55,55,55";
		when(pref.get("currentLineColor", defaultColor)).thenReturn(expected);
		String actual = HighlightColorPicker.calcThemeHighlightColor(pref, defaultColor);
		assertAll(
				() -> assertEquals(expected, actual),
				() -> verify(pref).put("jsparrow.marker.color", actual),
				() -> verify(pref).flush());
	}

	@Disabled("Fails in the maven build")
	@Test
	void testFindDefaultThemeColor_shouldReturnLightColor() {
		IWorkbench workbench = mock(IWorkbench.class);
		IThemeManager themeManager = mock(IThemeManager.class);
		ITheme currentTheme = mock(ITheme.class);

		ColorRegistry registry = new ColorRegistry();
		registry.put("org.eclipse.ui.editors.currentLineColor", new RGB(254, 253, 247));

		when(workbench.getThemeManager()).thenReturn(themeManager);
		when(themeManager.getCurrentTheme()).thenReturn(currentTheme);
		when(currentTheme.getColorRegistry()).thenReturn(registry);

		String actual = HighlightColorPicker.findDefaultThemeColor(workbench);
		assertEquals("254,253,247", actual);
	}

}
