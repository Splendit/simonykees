package eu.jsparrow.sample.postRule.collectionRemoveAll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"nls", "unused"})
public class TestCollectionRemoveAllRule {
	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}

	public String testIfCollectionIsEmpty(String input){
		List<String> resultList = generateList(input);
		
		/* invocation comment */
		/* param comment */
		resultList /* expression comment */.clear();

		resultList // I don't want to break anything
.clear();
		
		StringBuilder sb = new StringBuilder();
		
		resultList.stream().forEach((s)->sb.append(s));
		
		return sb.toString();
	}
	
	public String testProperCollectionIsEmpty(String input){
		List<String> resultList = generateList(input);
		List<String> resultList2 = generateList(input);
		resultList2.add("d");
		
		resultList.removeAll(resultList2);
		
		StringBuilder sb = new StringBuilder();
		
		resultList2.stream().forEach((s)->sb.append(s));
		
		return sb.toString();
	}
	
	public String testConvertMultipleCollections(String input){
		List<String> resultList1 = generateList(input);
		List<String> resultList2 = generateList(input);
		List<String> resultList3 = generateList(input);
		
		resultList2.add("d");
		
		resultList1.clear();
		resultList2.clear();
		resultList3.clear();
		
		StringBuilder sb = new StringBuilder();
		
		resultList1.stream().forEach((s)->sb.append(s));
		resultList2.stream().forEach((s)->sb.append(s));
		resultList3.stream().forEach((s)->sb.append(s));
		
		return sb.toString();
	}
	
	public String testNestedIf(String input){
		List<String> resultList = generateList(input);
		List<String> resultList2 = generateList(input);
		resultList2.add("d");
		
		if(resultList2.isEmpty()) {			
			resultList.clear();
		}
		
		StringBuilder sb = new StringBuilder();
		
		resultList2.stream().forEach((s)->sb.append(s));
		
		return sb.toString();
	}
	
	public String testNestedFor(String input){
		List<String> resultList = generateList(input);
		List<String> resultList2 = generateList(input);
		resultList2.add("d");
		
		for (String s : resultList2) {
			if(!resultList.isEmpty()) {			
				resultList.clear();
			}
		}
		
		StringBuilder sb = new StringBuilder();
		
		resultList2.stream().forEach((s)->sb.append(s));
		
		return sb.toString();
	}
	
	public String testNestedLambda(String input){
		List<String> resultList = generateList(input);
		List<String> resultList2 = generateList(input);
		resultList2.add("d");
		
		resultList2.forEach(s -> {
			if(!resultList.isEmpty()) {			
				resultList.clear();
			}
		});
		
		StringBuilder sb = new StringBuilder();
		
		resultList2.stream().forEach((s)->sb.append(s));
		
		return sb.toString();
	}
	
	public String testConvertInSwitchCase(String input){
		List<String> resultList = generateList(input);
		List<String> resultList2 = generateList(input);
		resultList2.add("d");
		
		switch(resultList2.size()) {
		case 0:
			resultList.clear();
			break;
		case 1:
			resultList.clear();
			break;
		default:
			resultList.clear();	
		}
		
		StringBuilder sb = new StringBuilder();
		
		resultList2.stream().forEach((s)->sb.append(s));
		
		return sb.toString();
	}
	
	public String testConvertInWhileLoop(String input){
		List<String> resultList = generateList(input);
		List<String> resultList2 = generateList(input);
		resultList2.add("d");
		
		Iterator<String> iterator = resultList2.iterator();
		while(iterator.hasNext()) {
			String s = iterator.next();
			switch(s) {
			case "a":
				resultList.clear();
				break;
			case "d":
				resultList.clear();
				break;
			}
		}

		StringBuilder sb = new StringBuilder();
		resultList.stream().forEach((s)->sb.append(s));
		
		return sb.toString();
	}
	
	public String testEmptyCollection(String input){
		List<String> resultList = generateList(input);
		
		resultList.removeAll(new ArrayList<String>());
		
		StringBuilder sb = new StringBuilder();
		
		resultList.stream().forEach((s)->sb.append(s));
		
		return sb.toString();
	}
	
	public String testModifiedCollection(String input){
		List<String> resultList = generateList(input);
		
		resultList.removeAll(resultList.stream().collect(Collectors.toList()));
		
		StringBuilder sb = new StringBuilder();
		
		resultList.stream().forEach((s)->sb.append(s));
		
		return sb.toString();
	}
	
	public String testReferencedCollection(String input){
		List<String> resultList = generateList(input);
		List<String> resultList2 = resultList;
		
		resultList.removeAll(resultList2);
		
		StringBuilder sb = new StringBuilder();
		
		resultList.stream().forEach((s)->sb.append(s));
		
		return sb.toString();
	}
	
	public String testNumericCollection(String input){
		List<String> resultList = generateList(input);
		List<Number> numericList = 
				resultList
				.stream()
				.map(String::hashCode)
				.collect(Collectors.toList());
		
		resultList.clear();
		numericList.clear();
		
		StringBuilder sb = new StringBuilder();
		
		numericList.stream().forEach((n)->sb.append(n));
		
		return sb.toString();
	}
	
	public String testMultipleConvertionPerLine(String input){
		List<String> resultList = generateList(input);
		List<Number> numericList = 
				resultList
				.stream()
				.map(String::hashCode)
				.collect(Collectors.toList());
		
		resultList.clear();numericList.clear();numericList.clear();
		numericList.removeAll(resultList);
		
		StringBuilder sb = new StringBuilder();
		
		numericList.stream().forEach((n)->sb.append(n));
		resultList.stream().forEach((s) -> sb.append(s));
		
		return sb.toString();
	}
	
	public String testSavingComments(String input){
		List<String> resultList = generateList(input);
		
		// to not be lost
		resultList.clear();
		
		/* trailing comments */
		resultList.clear();
		
		// to not be lost
		resultList.clear();
		
		StringBuilder sb = new StringBuilder();
		
		resultList.stream().forEach((s)->sb.append(s));
		
		return sb.toString();
	}
}
