package at.splendit.simonykees.core.ui.preference;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.i18n.Messages;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.ui.preference.profile.SimonykeesProfile;

/**
 * Central point to access property values.
 * 
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
public class SimonykeesPreferenceManager {

	private static IPreferenceStore store = Activator.getDefault().getPreferenceStore();

	/**
	 * Whether or not a specific rule is selected in the given profile.
	 * 
	 * @param profileId
	 *            the {@link SimonykeesProfile#getProfileId()} of a profile
	 * @param ruleId
	 *            the {@link RefactoringRule#getId()} of a rule
	 * 
	 * @return whether or not the given rule is selected in the given profile
	 */
	public static boolean isRuleSelectedInProfile(String profileId, String ruleId) {
		return store.getBoolean(getProfileRuleKey(profileId, ruleId));
	}

	/**
	 * Convenience method to get the needed entryNamesAndValues array for
	 * {@link ComboFieldEditor}.
	 * 
	 * @return String[][] array with profile names (with built-in suffix) and
	 *         values (keys for the preference page)
	 */
	public static String[][] getAllProfileNamesAndIdsArray() {
		String[] profileIds = getAllProfileIds();
		String[][] retVal = new String[profileIds.length][profileIds.length];
		for (int i = 0; i < profileIds.length; i++) {
			String profileId = profileIds[i];

			/*
			 * this is the displayed profile name: "{profile name} [built-in]"
			 * or "{profile name}"
			 */
			retVal[i] = new String[] { getProfileNameWithBuiltInSuffix(profileId), profileId };
		}

		return retVal;
	}

	/**
	 * Returns a {@link List} of all profile names as displayed in the UI (with
	 * built-in suffix).
	 * 
	 * @return all profile names with built-in suffix.
	 */
	public static List<String> getAllProfileNamesWithBuiltInSuffix() {
		return Arrays.stream(getAllProfileIds()).map(profileId -> getProfileNameWithBuiltInSuffix(profileId))
				.collect(Collectors.toList());
	}

	/**
	 * Returns a {@link LinkedHashMap} (ordered by insertion) with profile names
	 * as keys and profile ids as value.
	 * 
	 * @return ordered map with profile names and ids.
	 */
	public static Map<String, String> getAllProfileNamesAndIdsMap() {
		return Arrays.stream(getAllProfileIds()).collect(Collectors
				.toMap(profileId -> getProfileNameWithBuiltInSuffix(profileId), profileId -> profileId, (u, v) -> {
					throw new IllegalStateException(String.format("Duplicate key %s", u)); //$NON-NLS-1$
				}, LinkedHashMap::new));
	}

	/**
	 * Returns the current profileId.
	 * 
	 * @return the current profileId
	 */
	public static String getCurrentProfileId() {
		return store.getString(SimonykeesPreferenceConstants.PROFILE_ID_CURRENT);
	}

	/**
	 * Get the ids of all profiles.
	 * 
	 * @return a list of all {@link SimonykeesProfile#getProfileId()}
	 */
	private static String[] getAllProfileIds() {
		return parseString(store.getString(SimonykeesPreferenceConstants.PROFILE_LIST));
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

	/**
	 * This is the counterpart to {@link #parseString(String)}.
	 * 
	 * Takes a {@link List} of items and flattens them into a String, separated
	 * by "|".
	 * 
	 * @param items
	 *            List of items to flatten
	 * @return the given {@link List} as flat String
	 */
	public static String flattenArray(List<String> items) {
		return StringUtils.join(items, "|"); //$NON-NLS-1$
	}

	/**
	 * This is the counterpart to {@link #flattenArray(List)}.
	 * 
	 * Takes a (property stored as) flat String and splits it into a String
	 * array.
	 * 
	 * @param stringList
	 *            a flat String separated by "|"
	 * @return the flat String as a String[]
	 */
	private static String[] parseString(String stringList) {
		return StringUtils.split(stringList, "|"); //$NON-NLS-1$
	}

	/**
	 * Returns the profile name of a given profileId.
	 * 
	 * @param profileId
	 *            the profile id
	 * @return the profile name for a given profileId
	 */
	private static String getProfileName(String profileId) {
		return store.getString(getProfileNameKey(profileId));
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

	/**
	 * This method returns the profile name as displayed by the UI.
	 * 
	 * @param profileId
	 *            the profile id
	 * @return either "{profile name} [built-in]" or "{profile name}"
	 */
	private static String getProfileNameWithBuiltInSuffix(String profileId) {
		String profileName = getProfileName(profileId);
		return isProfileBuiltIn(profileId)
				? String.format("%s [%s]", profileName, Messages.SimonykeesPreferenceManager_builtIn) //$NON-NLS-1$
				: profileName;
	}

}
