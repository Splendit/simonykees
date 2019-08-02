package eu.jsparrow.sample.postRule.allRules;

import java.util.Collections;
import java.util.List;

public class UseCollectionsSingletonListRule {

	public void baseCase() {
		List<String> list = Collections.singletonList("value");
		List<String> emptyList = Collections.emptyList();
	}

	public void usingStaticImport() {
		List<String> list = Collections.singletonList("value");
		List<String> emptyList = Collections.emptyList();
	}

	public void usingFullyQualifiedNames() {
		List<String> list = Collections.singletonList("value");
		List<String> emptyList = Collections.emptyList();
	}

	public void moreThanOneOperatorParameter() {
		List<String> list = Collections.singletonList("1");
	}

}
