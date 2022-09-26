package eu.jsparrow.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestReplaceSetRemoveAllWithForEachRule {

	void removeAllItemsOfListFromSet(Set<String> strings, List<String> stringsToRemove) {
		stringsToRemove.forEach(strings::remove);
	}

	void removeAllItemsOfArrayListFromHashSet(HashSet<String> strings, ArrayList<String> stringsToRemove) {
		stringsToRemove.forEach(strings::remove);
	}
}