package eu.jsparrow.ui.preference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.IPreferenceStore;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.ui.Activator;
import eu.jsparrow.ui.preference.profile.DefaultActiveMarkers;
import eu.jsparrow.ui.preference.profile.DefaultProfile;
import eu.jsparrow.ui.preference.profile.EmptyProfile;
import eu.jsparrow.ui.preference.profile.FreeRulesProfile;
import eu.jsparrow.ui.preference.profile.Profile;
import eu.jsparrow.ui.preference.profile.SimonykeesProfile;
import eu.jsparrow.ui.util.LicenseUtil;

/**
 * Central point to access property values.
 * 
 * @author Ludwig Werzowa, Hannes Schweighofer, Matthias Webhofer
 * @since 0.9.2
 */
public class SimonykeesPreferenceManager {

	private static IPreferenceStore store = Activator.getDefault()
		.getPreferenceStore();

	private static List<SimonykeesProfile> profiles = new ArrayList<>();

	private static SimonykeesProfile defaultProfile = new DefaultProfile();
	private static SimonykeesProfile freeRulesProfile = new FreeRulesProfile();
	private static SimonykeesProfile emptyProfile = new EmptyProfile();

	private SimonykeesPreferenceManager() {
		// Hide default constructor
	}

	public static String getDefaultProfileList() {
		StringBuilder sb = new StringBuilder();
		sb.append(emptyProfile.getProfileName());
		sb.append(SimonykeesPreferenceConstants.NAME_RULES_DELIMITER);
		sb.append("|"); //$NON-NLS-1$
		sb.append(defaultProfile.getProfileName());
		sb.append(SimonykeesPreferenceConstants.NAME_RULES_DELIMITER);
		sb.append(StringUtils.join(defaultProfile.getEnabledRuleIds(),
				SimonykeesPreferenceConstants.RULE_RULE_DELIMITER));
		if (LicenseUtil.get()
			.isFreeLicense()) {
			sb.append("|"); //$NON-NLS-1$
			sb.append(freeRulesProfile.getProfileName());
			sb.append(SimonykeesPreferenceConstants.NAME_RULES_DELIMITER);
			sb.append(StringUtils.join(freeRulesProfile.getEnabledRuleIds(),
					SimonykeesPreferenceConstants.RULE_RULE_DELIMITER));
		}
		return sb.toString();
	}

	public static String getDefaultProfileName() {
		return defaultProfile.getProfileName();
	}

	public static String getFreeRulesProfileName() {
		return freeRulesProfile.getProfileName();
	}

	public static String getEmptyProfileName() {
		return emptyProfile.getProfileName();
	}

	public static List<SimonykeesProfile> getProfiles() {
		return profiles;
	}

	public static void addProfile(String name, List<String> ruleIds) {
		profiles.add(new Profile(name, ruleIds));
	}

	public static void removeProfile(String name) {
		getProfileFromName(name).ifPresent(profiles::remove);
	}

	public static void updateProfile(int index, String name, List<String> ruleIds, boolean isSetAsDefault) {
		if (profiles.get(index) instanceof Profile) {
			((Profile) profiles.get(index)).setProfileName(name);
			if (isSetAsDefault) {
				setCurrentProfileId(name);
			}
		}
		profiles.get(index)
			.setEnabledRulesIds(ruleIds);
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
	 * Sets the current profileId.
	 * 
	 */
	public static void setCurrentProfileId(String currentProfileId) {
		store.setValue(SimonykeesPreferenceConstants.PROFILE_ID_CURRENT, currentProfileId);
	}

	/**
	 * Returns the current selection for enabling dashboard on startup.
	 * 
	 * @return the boolean value if dashboard should be enabled
	 */
	public static boolean getEnableDashboard() {
		return store.getBoolean(SimonykeesPreferenceConstants.ENABLE_DASHBOARD);
	}

	/**
	 * Sets the current selection for enabling dashboard on startup.
	 */
	public static void setEnableDashboard(boolean enabled) {
		store.setValue(SimonykeesPreferenceConstants.ENABLE_DASHBOARD, enabled);
	}

	/**
	 * Returns the current selection for disabling register suggestion when
	 * starting select rules wizard.
	 * 
	 * @return the boolean value if register suggestion should be disabled
	 */
	public static boolean getDisableRegisterSuggestion() {
		return store.getBoolean(SimonykeesPreferenceConstants.DISABLE_REGISTER_SUGGESTION);
	}

	/**
	 * Sets the current selection for disabling register suggestion when
	 * starting select rules wizard.
	 */
	public static void setDisableRegisterSuggestion(boolean disabled) {
		store.setValue(SimonykeesPreferenceConstants.DISABLE_REGISTER_SUGGESTION, disabled);
	}

	/**
	 * Returns the current selection for enabling recursive package resolving
	 * 
	 * @return true for recursive package resolving, false otherwise
	 */
	public static boolean getResolvePackagesRecursively() {
		return store.getBoolean(SimonykeesPreferenceConstants.RESOLVE_PACKAGES_RECURSIVELY);
	}

	public static void setResolvePackagesRecursively(boolean enabled) {
		store.setValue(SimonykeesPreferenceConstants.RESOLVE_PACKAGES_RECURSIVELY, enabled);
	}

	/**
	 * Get the ids of all profiles.
	 * 
	 * @return a list of all {@link SimonykeesProfile#getProfileId()}
	 */
	public static List<String> getAllProfileIds() {
		if (profiles.isEmpty()) {
			loadProfilesFromStore();
		}
		return profiles.stream()
			.map(SimonykeesProfile::getProfileName)
			.collect(Collectors.toList());
	}

	private static String getAllProfiles() {
		return store.getString(SimonykeesPreferenceConstants.PROFILE_LIST);
	}

	private static void setAllProfiles() {
		store.setValue(SimonykeesPreferenceConstants.PROFILE_LIST, getStringFromProfiles());
	}

	private static List<SimonykeesProfile> loadProfilesFromStore() {
		// profiles are saved in store as collected list
		// ex. Profil1^rule1~rule2|profil 2^rule3~rule5~rule2
		String[] profilesArray = parseString(getAllProfiles());
		for (String profileInfo : profilesArray) {
			String name = StringUtils.substring(profileInfo, 0,
					StringUtils.indexOf(profileInfo, SimonykeesPreferenceConstants.NAME_RULES_DELIMITER));
			List<String> rules = Arrays.asList(
					StringUtils
						.substring(profileInfo,
								StringUtils.indexOf(profileInfo, SimonykeesPreferenceConstants.NAME_RULES_DELIMITER)
										+ 1)
						.split(SimonykeesPreferenceConstants.RULE_RULE_DELIMITER));
			if (name.equals(Messages.Profile_DefaultProfile_profileName)) {
				profiles.add(defaultProfile);
			} else if (name.equals(Messages.Profile_FreeRulesProfile_profileName) && LicenseUtil.get()
				.isFreeLicense()) {
				profiles.add(freeRulesProfile);
			} else if (name.equals(Messages.EmptyProfile_profileName)) {
				profiles.add(emptyProfile);
			} else {
				profiles.add(new Profile(name, rules));
			}
		}
		return profiles;

	}

	public static String getStringFromProfiles() {
		List<String> profilesAsString = new ArrayList<>();
		profiles.stream()
			.map(profile -> profile.getProfileName() + SimonykeesPreferenceConstants.NAME_RULES_DELIMITER
					+ StringUtils.join(profile.getEnabledRuleIds(), SimonykeesPreferenceConstants.RULE_RULE_DELIMITER))
			.forEach(profilesAsString::add);
		return flattenArray(profilesAsString);
	}

	public static Optional<SimonykeesProfile> getProfileFromName(String name) {
		return profiles.stream()
			.filter(profile -> profile.getProfileName()
				.equals(name))
			.findFirst();
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

	public static void loadCurrentProfiles() {
		if (!profiles.isEmpty()) {
			setAllProfiles();
		}
		profiles.clear();
		loadProfilesFromStore();
	}

	public static void performDefaults() {
		store.setValue(SimonykeesPreferenceConstants.PROFILE_LIST,
				store.getDefaultString(SimonykeesPreferenceConstants.PROFILE_LIST));

		store.setValue(SimonykeesPreferenceConstants.PROFILE_ID_CURRENT,
				store.getDefaultString(SimonykeesPreferenceConstants.PROFILE_ID_CURRENT));

		store.setValue(SimonykeesPreferenceConstants.RESOLVE_PACKAGES_RECURSIVELY, true);
		store.setValue(SimonykeesPreferenceConstants.ACTIVE_MARKERS, getDefaultActiveMarkers());

		profiles.clear();
		defaultProfile = new DefaultProfile();
		if (LicenseUtil.get()
			.isFreeLicense()) {
			freeRulesProfile = new FreeRulesProfile();
		}
		emptyProfile = new EmptyProfile();
		loadProfilesFromStore();
	}

	/**
	 * If cancel is pressed in Preferences page, no changes should be stored and
	 * profiles list has to be returned to state before any change was made.
	 */
	public static void resetProfilesList() {
		profiles.clear();
		loadProfilesFromStore();
	}
	
	public static List<String> getAllActiveMarkers() {
		String value = store.getString(SimonykeesPreferenceConstants.ACTIVE_MARKERS);
		return Arrays.asList(value.split(","));
	}
	
	public static void setAllActiveMarkers(List<String>activeMarkers) {
		String newValue = String.join(",", activeMarkers);
		store.setValue(SimonykeesPreferenceConstants.ACTIVE_MARKERS, newValue);		
	}
	
	public static String getDefaultActiveMarkers() {
		DefaultActiveMarkers defaultMarkers = new DefaultActiveMarkers();
		return String.join(",", defaultMarkers.getActiveMarkers());
	}
	
	public static  void addActiveMarker(String marker) {
		List<String> activeMarkers = new ArrayList<>(getAllActiveMarkers());
		if(activeMarkers.contains(marker)) {
			return;
		}
		activeMarkers.add(marker);
		setAllActiveMarkers(activeMarkers);
	}
	
	public static void removeActiveMarker(String marker) {
		List<String> activeMarkers = new ArrayList<>(getAllActiveMarkers());
		if(activeMarkers.remove(marker)) {
			setAllActiveMarkers(activeMarkers);
		}
	}
}
