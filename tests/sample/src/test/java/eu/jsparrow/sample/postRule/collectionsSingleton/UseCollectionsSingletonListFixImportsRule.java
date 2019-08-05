package eu.jsparrow.sample.postRule.collectionsSingleton;

import java.util.List;
import java.util.*;

public class UseCollectionsSingletonListFixImportsRule {
	
	public void baseCase() {
		List<String> list = Collections.singletonList("value");
		List<String> emptyList = Collections.emptyList();
	}
	
	public void usingStaticImport() {
		List<String> list = Collections.singletonList("value");
		List<String> emptyList = Collections.emptyList();
	}
}
