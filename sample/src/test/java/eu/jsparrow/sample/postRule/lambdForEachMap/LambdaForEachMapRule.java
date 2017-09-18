package eu.jsparrow.sample.postRule.lambdForEachMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import eu.jsparrow.sample.utilities.Person;

@SuppressWarnings({"nls", "unused", "rawtypes", "unchecked"})
public class LambdaForEachMapRule {
	
	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}
	
	public String unwrapFromCollection(String input) {
		List<String> list = Arrays.asList(input + "non", "non-empty");
		StringBuilder sb = new StringBuilder();
		
		list.stream().map(s -> s.substring(1)).forEach(subString -> sb.append(subString));
		
		return sb.toString();
	}

	public String unwrapOneExpression(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		list.stream().filter(s -> !s.isEmpty()).map(s -> s.substring(1)).forEach(subString -> sb.append(subString));
		
		return sb.toString();
	}
	
	public String longTypedStream(String input) {
		List<Long> list = Arrays.asList(5L, 3L, 2L);
		StringBuilder sb = new StringBuilder();
		
		list.stream().filter(l -> l > 0 ).mapToLong(l -> 100L/l).forEach(longVal -> sb.append(longVal));
		
		return sb.toString();
	}
	
	public String doubleTypedStream(String input) {
		List<Double> list = Arrays.asList(5D, 3D, 2D);
		StringBuilder sb = new StringBuilder();
		
		list.stream().filter(d -> d > 0 ).mapToDouble(d -> 100D/d).forEach(longVal -> sb.append(longVal));
		
		return sb.toString();
	}
	
	public String intTypedStream(String input) {
		List<Integer> list = Arrays.asList(5, 3, 2);
		StringBuilder sb = new StringBuilder();
		
		list.stream().filter(i -> i > 0).mapToInt(i -> 100 - i).forEach(longVal -> sb.append(longVal));
		
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
		
		list.stream().filter(s -> !s.isEmpty()).mapToInt(s -> {
			int offset = 10;
			return s.indexOf("i") + offset;
		}).forEach(pos -> sb.append(Integer.toString(pos)));
		return sb.toString();
	}
	
	public String mapToDifferentTypeSingleBodyExpression(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		list.stream().filter(s -> !s.isEmpty()).mapToInt(s -> s.indexOf("i") + 10).forEach(pos -> sb.append(Integer.toString(pos)));
		return sb.toString();
	}
	
	public String ifStatementAfterMappingVar(String input) {
		List<String> list = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		int offset = 10;
		list.stream().filter(s -> !s.isEmpty()).mapToInt(s -> {
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
		list.stream().filter(s -> !s.isEmpty()).mapToInt(s -> {
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
		list.stream().filter(s -> !s.isEmpty()).mapToInt(s -> {
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
		list.stream().filter(s -> !s.isEmpty()).mapToInt(s -> {
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
		numbers.stream().filter(n -> n.doubleValue() > 0).mapToInt((Number n) -> (int)n/2).forEach((d) -> sb.append(d));
		
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
	
	public String finalLocalVariable() {
		
		List<Object> rawList = generateRawListOfStrings();
		StringBuilder sb = new StringBuilder();
		rawList.stream().filter(o -> o != null).map((Object n) -> (String)n).forEach((final String s) -> {
			Number d = (int)Integer.valueOf(s)/2;
			sb.append(d);
		});
		
		return sb.toString();
	}
	
	public String annotatedLocalVariable() {
		
		List<Object> rawList = generateRawListOfStrings();
		StringBuilder sb = new StringBuilder();
		rawList.stream().filter(o -> o != null).forEach((Object n) -> {
			@Deprecated
			final String s = (String)n;
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
	
	public <T> void mapToGenericType(List<Person> refs) {
		List<List<T>> keys = new ArrayList<>();
		refs.stream().forEach(ref -> {
			final List<T> testKey = refToKey(ref);
			keys.add(testKey);
		});
	}
	
	public <T> void mapToNestedType(List<Person> refs) {
		List<List<String>> keys = new ArrayList<>();
		refs.stream().forEach(ref -> {
			final List<String> testKey = Collections.singletonList(ref.getName());
			keys.add(testKey);
		});
	}
	
	public void parameterizedMapMethod() {
		StringBuilder sb = new StringBuilder();
		List<Wrapper> wrappers = new ArrayList<>();
		wrappers.stream().forEach(wrapp -> {
			InnerClass innerClass = wrapp.getInnerClass();
			useInnerClass(innerClass);
			sb.append(innerClass.getName());
		});
	}
	
	public void multipleVariablesInitializedWithGenericMethod() {
		/*
		 * Similar to corner case in SIM-728
		 */
		StringBuilder sb = new StringBuilder();
		List<Wrapper> wrappers = new ArrayList<>();
		wrappers.forEach(wrapp -> {
			// Wrapper::getInnerClass is a generic method
			InnerClass innerClass = wrapp.getInnerClass();
			String toString = wrapp.toString();
			sb.append(innerClass.getName() + toString);
		});
	}
	
	public void unusedVariablesInitializedWithGenericMethod() {
		StringBuilder sb = new StringBuilder();
		List<Wrapper> wrappers = new ArrayList<>();
		wrappers.stream().map(wrapp -> {
			/*
			 * The generic method is not used as initializer of the
			 * mapping variable.
			 */
			InnerClass innerClass = wrapp.getInnerClass();
			String strInnerClass = innerClass.toString();
			return wrapp.toString() + strInnerClass;
		}).forEach(toString -> sb.append(toString));
	}
	
	interface Inner {
		
	}
	
	class InnerClass implements Inner {
		public String getName() {
			return this.getClass().getName();
		}
	}
	
	public void useInnerClass(InnerClass innerClass) {
		
	}
	
	class Wrapper {
		public <I extends Inner> I getInnerClass() {
			return null;
		}
	}
	
	public <T> void mapToTypeVariable(List<Person> refs) {
		List<T> keys = new ArrayList<>();
		refs.stream().forEach(ref -> {
			final T testKey = refToKeyT(ref);
			keys.add(testKey);
		});
	}
	
	private <T> T refToKeyT(Person ref) {
		return null;
	}
	
	private <T> List<T> refToKey(Person ref) {
		return new ArrayList<>();
	}

	private List generateRawListOfStrings() {
		List rawList = Arrays.asList("2.3", "4.5");
		return rawList;
	}
}
