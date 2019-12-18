package eu.jsparrow.sample.postRule.allRules;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UseCollectionsSingletonListRule {

	public void baseCase() {
		final List<String> list = Collections.singletonList("value");
		final List<String> emptyList = Collections.emptyList();
	}

	public void usingStaticImport() {
		final List<String> list = Collections.singletonList("value");
		final List<String> emptyList = Collections.emptyList();
	}

	public void usingFullyQualifiedNames() {
		final List<String> list = Collections.singletonList("value");
		final List<String> emptyList = Collections.emptyList();
	}

	public void moreThanOneOperatorParameter() {
		List<String> list = Arrays.asList("1", "2");
		list = asList("1", "2");
	}
}
