package eu.jsparrow.sample.preRule;

import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.List;

public class UseCollectionsSingletonListRule {
	
	public void baseCase() {
		List<String> list = Arrays.asList("value");
		List<String> emptyList = Arrays.asList();
	}
	
	
	public void usingStaticImport() {
		List<String> list = asList("value");
		List<String> emptyList = asList();
	}
	
	public void usingFullyQualifiedNames() {
		List<String> list = java.util.Arrays.asList("value");
		List<String> emptyList = java.util.Arrays.asList();
	}
	
	public void moreThanOneOperatorParameter() {
		List<String> list = Arrays.asList("1", "2");
		list = asList("1", "2");
	}
}
