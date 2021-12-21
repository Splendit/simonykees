package eu.jsparrow.rules.common;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


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
	
	private static final Map<Integer, Integer> remediationPriceMapping = initRemediationPriceMapping();

	public RuleDescription(String name, String description, Duration remediationCost, List<Tag> tags) {
		this.name = name;
		this.description = description;
		this.remediationCost = remediationCost;
		this.tags = tags;
	}

	private static Map<Integer, Integer> initRemediationPriceMapping() {
		Map<Integer, Integer> map = new HashMap<>();
		map.put(1, 1);
		map.put(2,  2);
		map.put(5, 5);
		map.put(10, 10);
		map.put(15, 15);
		map.put(20, 20);
		map.put(30, 20);
		return Collections.unmodifiableMap(map);
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

	public int getCredit() {
		int remediation = (int)remediationCost.toMinutes();
		return remediationPriceMapping.get(remediation);
		
	}

	@Override
	public int hashCode() {
		return Objects.hash(description, name, remediationCost, tags);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof RuleDescription)) {
			return false;
		}
		RuleDescription other = (RuleDescription) obj;
		return Objects.equals(description, other.description) && Objects.equals(name, other.name)
				&& Objects.equals(remediationCost, other.remediationCost) && Objects.equals(tags, other.tags);
	}
}
