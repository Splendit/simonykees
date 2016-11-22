package at.splendit.simonykees.core.ui.preference;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.i18n.Messages;

/**
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
public class SimonykeesPreferenceManager {

	private static IPreferenceStore store = Activator.getDefault().getPreferenceStore();

	public static boolean isRuleSelected(String ruleId) {
		return store.getBoolean(ruleId);
	}

	/**
	 * Convenience method to get the needed entryNamesAndValues array for
	 * {@link ComboFieldEditor}.
	 * 
	 * @return String[][] array with profile names (displayed in UI) and values
	 *         (keys for the preference page)
	 */
	public static String[][] getProfileNamesAndValues() {
		String[] profileIds = parseString(store.getString(SimonykeesPreferenceConstants.PROFILE_LIST));
		String[][] retVal = new String[profileIds.length][profileIds.length];
		for (int i = 0; i < profileIds.length; i++) {
			String profileId = profileIds[i];
			String profileName = store
					.getString(String.format("%s.%s", profileId, SimonykeesPreferenceConstants.PROFILE_NAME)); //$NON-NLS-1$
			boolean builtIn = store
					.getBoolean(String.format("%s.%s", profileId, SimonykeesPreferenceConstants.PROFILE_IS_BUILT_IN)); //$NON-NLS-1$

			/*
			 * for the displayed profile name: "{profile name} [built-in]" or
			 * "{profile name}"
			 */
			retVal[i] = new String[] {
					builtIn ? String.format("%s [%s]", profileName, Messages.SimonykeesPreferenceManager_builtIn) //$NON-NLS-1$
							: profileName,
					profileId };
		}

		return retVal;
	}

	/**
	 * Returns the built-in status of a given profile.
	 * 
	 * @param profileId
	 *            the profile id
	 * @return whether or not the given profileId belongs to a built-in profile
	 */
	public static boolean isProfileBuiltIn(String profileId) {
		return store.getBoolean(getProfileBuiltInKey(profileId));
	}

	public static String flattenArray(List<String> items) {
		return StringUtils.join(items, "|"); //$NON-NLS-1$
	}

	public static String[] parseString(String stringList) {
		return StringUtils.split(stringList, "|"); //$NON-NLS-1$
	}

	/**
	 * Convenience method that returns the key for a specific rule in a specific
	 * profile.
	 * 
	 * @param profileId
	 * @param ruleId
	 * @return preference page key
	 */
	public static String getProfileRuleKey(String profileId, String ruleId) {
		return String.format("%s.%s", profileId, ruleId); //$NON-NLS-1$
	}

	/**
	 * Convenience method that returns the "name"-key for a specific profile.
	 * 
	 * @param profileId
	 * @return preference page key
	 */
	public static String getProfileNameKey(String profileId) {
		return String.format("%s.%s", profileId, SimonykeesPreferenceConstants.PROFILE_NAME); //$NON-NLS-1$
	}

	/**
	 * Convenience method that returns the "builtIn"-key for a specific profile.
	 * 
	 * @param profileId
	 * @return
	 */
	public static String getProfileBuiltInKey(String profileId) {
		return String.format("%s.%s", profileId, SimonykeesPreferenceConstants.PROFILE_IS_BUILT_IN); //$NON-NLS-1$
	}

}
