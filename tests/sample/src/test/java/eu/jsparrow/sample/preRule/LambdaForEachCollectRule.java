package eu.jsparrow.sample.preRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings({"nls", "unused"})
public class LambdaForEachCollectRule {
	
	List<String> listField = new ArrayList<>();
	
	public String convertForEachToCollect(String input) {
		List<String> oStrings = new ArrayList<>();
		List<String> objectList = new ArrayList<>();
		
		objectList.stream().map(o -> o.substring(0)) // comment after map
		.forEach( /* lambda param comment */ oString -> /* lambda body */ {
			// save me
			oStrings // comment after oStrings
			.add(oString);
		});
		
		return oStrings.stream().collect(Collectors.joining(","));
	}
	
	public String convertForEachExpressionToCollect(String input) {
		List<String> oStrings = new ArrayList<>();
		List<String> objectList = new ArrayList<>();
		
		objectList.stream().map(o -> o.toString())
		.forEach((String oString) -> 
			oStrings.add(oString)
		);
		
		return oStrings.stream().collect(Collectors.joining(","));
	}
	
	public String multiDeclarationFragment(String input) {
		List<String> oStrings = new ArrayList<>(), ostrings2, ostrings3 = new ArrayList<>();
		List<String> objectList = new ArrayList<>();
		
		objectList.stream().map(o -> o.substring(0))
		.forEach( oString -> {
			oStrings.add(oString);
		});
		
		return oStrings.stream().collect(Collectors.joining(","));
	}
	
	public String nonEmptyCollection(String input) {
		List<String> oStrings = new ArrayList<>();
		List<String> objectList = new ArrayList<>();
		
		oStrings.add("");
		objectList.stream().map(o -> o.substring(0))
		.forEach((String oString) -> 
			oStrings.add(oString)
		);
		
		return oStrings.stream().collect(Collectors.joining(","));
	}
	
	public String addAfterForEach(String input) {
		List<String> oStrings = new ArrayList<>();
		List<String> objectList = new ArrayList<>();
		
		objectList.stream().map(o -> o.substring(0))
		.forEach((String oString) -> 
			oStrings.add(oString)
		);
		
		oStrings.add("-");
		return oStrings.stream().collect(Collectors.joining(","));
	}
	
	public String blockBody(String input) {
		List<String> oStrings = new ArrayList<>();
		List<String> objectList = new ArrayList<>();
		
		objectList.stream().map(o -> o.substring(0))
		.forEach((String oString) -> {
			oStrings.add(oString);
		});
		
		return oStrings.stream().collect(Collectors.joining(","));
	}
	
	public String multipleBodyStatements(String input) {
		List<String> oStrings = new ArrayList<>();
		List<String> objectList = new ArrayList<>();
		
		objectList.stream().map(o -> o.substring(0))
		.forEach((String oString) -> {
			oStrings.add(oString);
			if(oString.isEmpty()) oStrings.add(input);
		});
		
		return oStrings.stream().collect(Collectors.joining(","));
	}
	
	public String referencingOtherLocalVars(String input) {
		List<String> oStrings = new ArrayList<>();
		List<String> objectList = new ArrayList<>();
		String s = "";
		objectList.stream().filter(oString -> oString.equals(s)).map(o -> o.substring(0))
		.forEach((String oString) -> {
			oStrings.add(oString);
		});
		
		return oStrings.stream().collect(Collectors.joining(","));
	}
	
	public String nestedBlocks(String input) {
		List<String> oStrings = new ArrayList<>();
		{
			List<String> objectList = new ArrayList<>();
			String s = "";
			if(s.isEmpty()) {
				objectList.stream().filter(oString -> oString.equals(s)).map(o -> o.substring(0))
				.forEach((String oString) -> {
					oStrings.add(oString);
				});
			}
		}

		
		return oStrings.stream().collect(Collectors.joining(","));
	}
	
	public String addingToFieldMethod(String input) {
		List<String> objectList = new ArrayList<>();
		String s = "";
		objectList.stream().filter(oString -> oString.equals(s)).map(o -> o.substring(0))
		.forEach((String oString) -> {
			listField.add(oString);
		});
		
		return listField.stream().collect(Collectors.joining(","));
	}
	
	public String thisAddInvocation(String input) {
		List<String> objectList = new ArrayList<>();
		String s = "";
		objectList.stream().filter(oString -> oString.equals(s)).map(o -> o.substring(0))
		.forEach((String oString) -> {
			add(oString);
		});
		
		return listField.stream().collect(Collectors.joining(","));
	}
	
	private void add(String string) {
		listField.add(string);
	}
	
	public String collectRawList(String input) {
		List raw = Arrays.asList(input);
		List<Object> typedList = new ArrayList<>();
		raw.stream().forEach(oString -> typedList.add(oString));
		
		return typedList.stream().map(o -> o.toString()).collect(Collectors.joining());
	}
}
