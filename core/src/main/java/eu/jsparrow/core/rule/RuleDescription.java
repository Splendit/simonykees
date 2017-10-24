package eu.jsparrow.core.rule;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class RuleDescription {

	private String name;

	private String description;

	private final List<Tag> tags;

	private Duration remediationCost;
	
	public RuleDescription(String name, String description, Duration remediationCost, Tag... tags) {
		this.name = name;
		this.description = description;
		this.remediationCost = remediationCost;
		this.tags = Arrays.asList(tags);
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
