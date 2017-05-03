package at.splendit.simonykees.core.ui.preference.profile;

import java.util.List;

/**
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
public interface SimonykeesProfile {
	
	String getProfileName();

	List<String> getEnabledRuleIds();
	public void setEnabledRulesIds(List<String> enabledRulesIds);
	boolean containsRule(String id);
	
	boolean isBuiltInProfile();
	
}
