package eu.jsparrow.core.rule;

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

	public List<Tag> getTags() {
		return tags;
	}

	public Duration getRemediationCost() {
		return remediationCost;
	}

}
