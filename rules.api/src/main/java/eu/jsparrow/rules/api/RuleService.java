package eu.jsparrow.rules.api;

import java.util.List;

/**
 * Provides you with all the rules.
 * 
 * @author Hans-Jörg Schrödl
 *
 */
public interface RuleService {
	
	List<String> loadRules();  

}
