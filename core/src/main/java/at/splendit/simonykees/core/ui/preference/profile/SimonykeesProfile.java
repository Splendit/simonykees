package at.splendit.simonykees.core.ui.preference.profile;

import java.util.List;

/**
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
public interface SimonykeesProfile {
	
	String getProfileName();

	List<String> getEnabledRuleIds();
	boolean containsRule(String id);
	
	default boolean isBuiltInProfile() {
		return false;
	}
	
}
