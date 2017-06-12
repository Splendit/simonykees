package at.splendit.simonykees.sample.preRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"nls", "unused", "rawtypes", "unchecked"})
public class LambdaForEachMapRule {
	
	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}

	public String unwrapOneExpression(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		list.stream().filter(s -> !s.isEmpty()).forEach(s -> {
			String subString = s.substring(1);
			sb.append(subString);
		});
		
		return sb.toString();
	}

	public String unwrapMultipleExpressions(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		list.stream().filter(s -> !s.isEmpty()).forEach(s -> {
			int i = 10;
			String subString = s.substring(1) + i;
			String lower = subString.toLowerCase();
			sb.append(lower);
		});
		
		return sb.toString();
	}
	
	public String unsplittableBody(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		list.stream().filter(s -> !s.isEmpty()).forEach(s -> {
			int i = 10;
			String subString = s.substring(1) + i;
			String lower = subString.toLowerCase();
			sb.append(lower + i);
		});
		return sb.toString();
	}
	
	public String unsplittableBody2(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		list.stream().filter(s -> !s.isEmpty()).forEach(s -> {
			int i = 10;
			int c = 0;
			String subString = s.substring(1) + i + c;
			String lower = subString.toLowerCase();
			sb.append(lower + c);
		});
		return sb.toString();
	}
	
	public String unsplittableBody3(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		list.stream().filter(s -> !s.isEmpty()).forEach(s -> {
			int i = 10;
			int c = 0;
			String subString = s.substring(1) + i + c;
			String lower = subString.toLowerCase();
			sb.append(lower);
			if(lower.isEmpty()) {
				sb.append(s);
			}
		});
		return sb.toString();
	}
	
	public String mapToDifferentType(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		list.stream().filter(s -> !s.isEmpty()).forEach(s -> {
			int offset = 10;
			int pos = s.indexOf("i") + offset;
			
			sb.append(Integer.toString(pos));
		});
		return sb.toString();
	}
	
	public String mapToDifferentTypeSingleBodyExpression(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		list.stream().filter(s -> !s.isEmpty()).forEach(s -> {
			int pos = s.indexOf("i") + 10;
			
			sb.append(Integer.toString(pos));
		});
		return sb.toString();
	}
	
	public String ifStatementAfterMappingVar(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		int offset = 10;
		list.stream().filter(s -> !s.isEmpty()).forEach(s -> {
			sb.append(s);
			sb.append("c");
			int pos = s.indexOf("i") + 10;
			sb.append("d");
			if(offset > 0) {
				sb.append(Integer.toString(pos));
			}
		});
		return sb.toString();
	}
	
	public String ifStatementBeforeMappingVar(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		int offset = 10;
		list.stream().filter(s -> !s.isEmpty()).forEach(s -> {
			if(offset > 0) {
				sb.append(s);
			}
			int pos = s.indexOf("i");
			sb.append("c");
			sb.append(pos + "d");
		});
		return sb.toString();
	}
	
	public String saveComments(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		int offset = 10;
		list.stream().filter(s -> !s.isEmpty()).forEach(s -> {
			// I may be necessary here
			if(offset > 0) {
				sb.append(s);
			}
			int pos = s.indexOf("i");
			// and here...
			sb.append("c");
			sb.append(pos + "d");
		});
		return sb.toString();
	}
	
	public String saveComments2(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		int offset = 10;
		list.stream().filter(s -> !s.isEmpty()).forEach(s -> {
			int i;// not important
			
			// not used
			int j;
			
			int pos = s.indexOf("i");
			sb.append("c");
			sb.append(pos + "d");
		});
		return sb.toString();
	}
	
	public String multipleDeclarationFragments(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		list.stream().filter(s -> !s.isEmpty()).forEach(s -> {
			sb.append(s);
			int pos = s.indexOf("i"), c = 0;
			sb.append("c");
			sb.append(pos + "d");
		});
		return sb.toString();
	}
	
	public String explicitParameterType() {
		List<Number> numbers = new ArrayList<>();
		numbers.add(2.3);
		numbers.add(4.5);
		
		StringBuilder sb = new StringBuilder();
		numbers.stream().filter(n -> n.doubleValue() > 0).forEach((Number n) -> {
			Double d = (Double)n;
			String s = d.toString();
			sb.append(s);
		});
		
		return sb.toString();
	}
	
	public String explicitParameterizedArgumentType() {
		List<Number> numbers = new ArrayList<>();
		numbers.add(2.3);
		numbers.add(4.5);
		
		StringBuilder sb = new StringBuilder();
		numbers.stream().filter(n -> n.doubleValue() > 0).forEach((Number n) -> {
			List<Number> nums = Arrays.asList(n);
			Double d = (Double)nums.get(0);
			String s = d.toString();
			sb.append(nums.toString());
		});
		
		return sb.toString();
	}
	
	public String explicitArrayArgumentType() {
		List<Number> numbers = new ArrayList<>();
		numbers.add(2.3);
		numbers.add(4.5);
		
		StringBuilder sb = new StringBuilder();
		numbers.stream().filter(n -> n.doubleValue() > 0).forEach((Number n) -> {
			Number[] nums = {n};
			Double d = (Double)nums[0];
			String s = d.toString();
			sb.append(nums.toString());
		});
		
		return sb.toString();
	}
	
	public String explicitPrimitiveType() {
		List<Number> numbers = new ArrayList<>();
		numbers.add(2.3);
		numbers.add(4.5);
		
		StringBuilder sb = new StringBuilder();
		numbers.stream().filter(n -> n.doubleValue() > 0).forEach((Number n) -> {
			int d = (int)n/2;
			sb.append(d);
		});
		
		return sb.toString();
	}
	
	public String rawType() {
		
		List rawList = generateRawListOfStrings();
		StringBuilder sb = new StringBuilder();
		rawList.stream().filter(o -> o != null).forEach((Object n) -> {
			String s = (String)n;
			Number d = (int)Integer.valueOf(s)/2;
			sb.append(d);
		});
		
		return sb.toString();
	}
	
	public String rawTypeFromMethodInvocation() {
		
		StringBuilder sb = new StringBuilder();
		this.generateRawListOfStrings().stream().filter(o -> o != null).forEach((Object n) -> {
			String s = (String)n;
			Number d = (int)Integer.valueOf(s)/2;
			sb.append(d);
		});
		
		return sb.toString();
	}
	
	private List generateRawListOfStrings() {
		List rawList = Arrays.asList("2.3", "4.5");
		return rawList;
	}
}
