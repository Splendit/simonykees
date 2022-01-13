package eu.jsparrow.rules.common.markers;

import eu.jsparrow.rules.common.RuleDescription;

/**
 * A common type for all jSparrow Marker resolvers.
 * 
 * @since 4.6.0
 *
 */
public interface Resolver {

	RuleDescription getDescription();
}
