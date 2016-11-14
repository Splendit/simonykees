/**
 * 
 */
package at.splendit.simonykees.core.ui.preference;

import org.eclipse.jface.preference.IPreferenceStore;

import at.splendit.simonykees.core.Activator;

/**
 * @author Ludwig Werzowa
 * @since 0.9.2
 */
public class SimonykeesPreferenceManager {
	
	private static IPreferenceStore store = Activator.getDefault().getPreferenceStore();

	public static boolean isRuleSelected(String ruleId) {
		return store.getBoolean(ruleId);
	}
	
}
