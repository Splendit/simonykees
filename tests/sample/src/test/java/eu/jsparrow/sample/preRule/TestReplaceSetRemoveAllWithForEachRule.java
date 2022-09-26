package eu.jsparrow.sample.preRule;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestReplaceSetRemoveAllWithForEachRule {

	void removeAllItemsOfListFromSet(Set<String> strings, List<String> stringsToRemove) {
		strings.removeAll(stringsToRemove);
	}

	void removeAllItemsOfArrayListFromHashSet(HashSet<String> strings, ArrayList<String> stringsToRemove) {
		strings.removeAll(stringsToRemove);
	}
}