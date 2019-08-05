package eu.jsparrow.sample.postRule.collectionsSingleton;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

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
		List<String> list = Arrays.asList("1", "2");
		list = asList("1", "2");
	}
}
