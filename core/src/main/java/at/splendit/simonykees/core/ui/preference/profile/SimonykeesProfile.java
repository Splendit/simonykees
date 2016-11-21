/**
 * 
 */
package at.splendit.simonykees.core.ui.preference.profile;

import java.util.Collections;
import java.util.List;

/**
 * @author Ludwig Werzowa, Hannes Schweighofer
 * @since 0.9.2
 */
public interface SimonykeesProfile {
	
	String getProfileId();
	String getProfileName();
	default boolean isBuiltInProfile() {
		return false;
	}
	default List<String> getEnabledRuleIds() {
		return Collections.emptyList();
	}

}
