package eu.jsparrow.rules.common;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains the description for a rule. A description includes the
 * name displayed in the UI, a description text, tags associated with the rule
 * and remedation costs.
 * 
 * @author Hans-Jörg Schrödl
 *
 */
public class RuleDescription {

	private final String name;

	private final String description;

	private final List<Tag> tags;

	private final Duration remediationCost;

	public RuleDescription(String name, String description, Duration remediationCost, List<Tag> tags) {
		this.name = name;
		this.description = description;
		this.remediationCost = remediationCost;
		this.tags = tags;
	}

	public RuleDescription(String name, String description, Duration remediationCost, Tag... tags) {
		this(name, description, remediationCost, Arrays.asList(tags));
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * Gets the tags associated with this rule. See {@link Tag}.
	 * 
	 * @return the tags associated with this rule. 
	 */
	public List<Tag> getTags() {
		return tags;
	}

	/**
	 * Gets the remediation cost for this rule. The remedation cost is the
	 * amount of time it would take to manually fix an instance of a rule
	 * violation. That cost includes not only the time to change code, but also
	 * things like testing, integration testing, deployment...
	 * 
	 * @return the remedation cost for this rule
	 */
	public Duration getRemediationCost() {
		return remediationCost;
	}

}
