package eu.jsparrow.sample.postRule.replaceSetRemoveAll;

import java.util.List;
import java.util.Set;

public class TestReplaceSetRemoveAllWithForEachNotTransformingRule {

	void removeAllItemsOfSetFromSet(Set<String> strings, Set<String> stringsToRemove) {
		strings.removeAll(stringsToRemove);
	}

	void removeAllItemsOfListFromList(List<String> strings, List<String> stringsToRemove) {
		strings.removeAll(stringsToRemove);
	}
}