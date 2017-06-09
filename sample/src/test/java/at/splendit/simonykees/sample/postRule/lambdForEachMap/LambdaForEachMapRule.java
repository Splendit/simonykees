package at.splendit.simonykees.sample.postRule.lambdForEachMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings({"nls", "unused"})
public class LambdaForEachMapRule {
	
	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}

	public String unwrapOneExpression(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		list.stream().filter(s -> !s.isEmpty()).map(s -> s.substring(1)).forEach(subString -> sb.append(subString));
		
		return sb.toString();
	}

	public String unwrapMultipleExpressions(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		list.stream().filter(s -> !s.isEmpty()).map(s -> {
			int i = 10;
			return s.substring(1) + i;
		}).forEach(subString -> {
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
		
		list.stream().filter(s -> !s.isEmpty()).map(s -> {
			int offset = 10;
			return s.indexOf("i") + offset;
		}).forEach(pos -> sb.append(Integer.toString(pos)));
		return sb.toString();
	}
	
	public String mapToDifferentTypeSingleBodyExpression(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		list.stream().filter(s -> !s.isEmpty()).map(s -> s.indexOf("i") + 10).forEach(pos -> sb.append(Integer.toString(pos)));
		return sb.toString();
	}
	
	public String ifStatementAfterMappingVar(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		int offset = 10;
		list.stream().filter(s -> !s.isEmpty()).map(s -> {
			sb.append(s);
			sb.append("c");
			return s.indexOf("i") + 10;
		}).forEach(pos -> {
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
		list.stream().filter(s -> !s.isEmpty()).map(s -> {
			if(offset > 0) {
				sb.append(s);
			}
			return s.indexOf("i");
		}).forEach(pos -> {
			sb.append("c");
			sb.append(pos + "d");
		});
		return sb.toString();
	}
	
	public String saveComments(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		int offset = 10;
		list.stream().filter(s -> !s.isEmpty()).map(s -> {
			// I may be necessary here
			if(offset > 0) {
				sb.append(s);
			}
			return s.indexOf("i");
		}).forEach(pos -> {
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
		list.stream().filter(s -> !s.isEmpty()).map(s -> {
			int i;// not important
			// not used
			int j;
			return s.indexOf("i");
		}).forEach(pos -> {
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
		numbers.stream().filter(n -> n.doubleValue() > 0).map((Number n) -> (Double)n).forEach((Double d) -> {
			String s = d.toString();
			sb.append(s);
		});
		
		return sb.toString();
	}
}
