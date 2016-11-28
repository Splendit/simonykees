package at.splendit.simonykees.sample.postRule.collectionRemoveAll;

import java.util.Arrays;
import java.util.List;

@SuppressWarnings("nls")
public class TestCollectionRemoveAllRule {
	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}

	public String testIfCollectionIsEmpty(String input){
		List<String> resultList = generateList(input);
		
		resultList.clear();
		
		StringBuilder sb = new StringBuilder();
		
		resultList.stream().forEach((s)->sb.append(s));
		
		return sb.toString();
	}
}
