package eu.jsparrow.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import eu.jsparrow.sample.utilities.Person;

@SuppressWarnings({ "nls", "unused", "rawtypes", "unchecked" })
public class LambdaForEachMapRule {

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";"));
	}

	public String unwrapFromCollection(String input) {
		final List<String> list = Arrays.asList(input + "non", "non-empty");
		final StringBuilder sb = new StringBuilder();

		// save me 1
		// comment after s.substring(1)
		// save me 2
		// trailing comment in rs 2
		// trailing comment in rs 1
		list.stream()
			.map(s -> StringUtils.substring(s, 1) // comment after
													// s.substring(1)
			)
			.forEach(sb::append);

		// I could get lost in the map
		list.stream()
			.map(s -> StringUtils.substring(s, 1))
			.forEach(subString -> {
				if (!StringUtils.isEmpty(subString)) {
					// I am safer here
					sb.append(subString);
				}
			});

		// save me 1
		// save me 2
		// save me 3
		// save me 4
		list.stream()
			.map(s -> StringUtils.substring(s, 1))
			.forEach(subString -> list.stream()
				.map(s2 -> StringUtils.substring(s2, 1))
				.forEach(sb::append));

		return sb.toString();
	}

	public String unwrapOneExpression(String input) {
		final List<String> list = generateList(input);
		final StringBuilder sb = new StringBuilder();

		// save me 1
		// save me 2
		list.stream()
			.filter(s -> !StringUtils.isEmpty(s))
			.map(s -> StringUtils.substring(s, 1))
			.forEach(sb::append);

		return sb.toString();
	}

	public String longTypedStream(String input) {
		final List<Long> list = Arrays.asList(5L, 3L, 2L);
		final StringBuilder sb = new StringBuilder();

		list.stream()
			.filter(l -> l > 0)
			.mapToLong(l -> 100L / l)
			.forEach(sb::append);

		return sb.toString();
	}

	public String doubleTypedStream(String input) {
		final List<Double> list = Arrays.asList(5D, 3D, 2D);
		final StringBuilder sb = new StringBuilder();

		list.stream()
			.filter(d -> d > 0)
			.mapToDouble(d -> 100D / d)
			.forEach(sb::append);

		return sb.toString();
	}

	public String intTypedStream(String input) {
		final List<Integer> list = Arrays.asList(5, 3, 2);
		final StringBuilder sb = new StringBuilder();

		list.stream()
			.filter(i -> i > 0)
			.mapToInt(i -> 100 - i)
			.forEach(sb::append);

		return sb.toString();
	}

	public String unwrapMultipleExpressions(String input) {
		final List<String> list = generateList(input);
		final StringBuilder sb = new StringBuilder();

		// save me 2
		list.stream()
			.filter(s -> !StringUtils.isEmpty(s))
			.map(s -> {
				// save me 1
				final int i = 10;
				return StringUtils.substring(s, 1) + i;
			})
			.forEach(subString -> {
				// save me 3
				final String lower = StringUtils.lowerCase(subString);
				// save me 4
				sb.append(lower);
			});

		return sb.toString();
	}

	public String unsplittableBody(String input) {
		final List<String> list = generateList(input);
		final StringBuilder sb = new StringBuilder();

		list.stream()
			.filter(s -> !StringUtils.isEmpty(s))
			.forEach(s -> {
				final int i = 10;
				final String subString = StringUtils.substring(s, 1) + i;
				final String lower = StringUtils.lowerCase(subString);
				sb.append(lower + i);
			});
		return sb.toString();
	}

	public String unsplittableBody2(String input) {
		final List<String> list = generateList(input);
		final StringBuilder sb = new StringBuilder();

		list.stream()
			.filter(s -> !StringUtils.isEmpty(s))
			.forEach(s -> {
				final int i = 10;
				final int c = 0;
				final String subString = new StringBuilder().append(StringUtils.substring(s, 1))
					.append(i)
					.append(c)
					.toString();
				final String lower = StringUtils.lowerCase(subString);
				sb.append(lower + c);
			});
		return sb.toString();
	}

	public String unsplittableBody3(String input) {
		final List<String> list = generateList(input);
		final StringBuilder sb = new StringBuilder();

		list.stream()
			.filter(s -> !StringUtils.isEmpty(s))
			.forEach(s -> {
				final int i = 10;
				final int c = 0;
				final String subString = new StringBuilder().append(StringUtils.substring(s, 1))
					.append(i)
					.append(c)
					.toString();
				final String lower = StringUtils.lowerCase(subString);
				sb.append(lower);
				if (StringUtils.isEmpty(lower)) {
					sb.append(s);
				}
			});
		return sb.toString();
	}

	public String mapToDifferentType(String input) {
		final List<String> list = generateList(input);
		final StringBuilder sb = new StringBuilder();

		list.stream()
			.filter(s -> !StringUtils.isEmpty(s))
			.mapToInt(s -> {
				final int offset = 10;
				return StringUtils.indexOf(s, "i") + offset;
			})
			.forEach(pos -> sb.append(Integer.toString(pos)));
		return sb.toString();
	}

	public String mapToDifferentTypeSingleBodyExpression(String input) {
		final List<String> list = generateList(input);
		final StringBuilder sb = new StringBuilder();

		list.stream()
			.filter(s -> !StringUtils.isEmpty(s))
			.mapToInt(s -> StringUtils.indexOf(s, "i") + 10)
			.forEach(pos -> sb.append(Integer.toString(pos)));
		return sb.toString();
	}

	public String ifStatementAfterMappingVar(String input) {
		final List<String> list = generateList(input);
		final StringBuilder sb = new StringBuilder();

		final int offset = 10;
		list.stream()
			.filter(s -> !StringUtils.isEmpty(s))
			.mapToInt(s -> {
				sb.append(s);
				sb.append("c");
				return StringUtils.indexOf(s, "i") + 10;
			})
			.forEach(pos -> {
				sb.append("d");
				if (offset > 0) {
					sb.append(Integer.toString(pos));
				}
			});
		return sb.toString();
	}

	public String ifStatementBeforeMappingVar(String input) {
		final List<String> list = generateList(input);
		final StringBuilder sb = new StringBuilder();

		final int offset = 10;
		list.stream()
			.filter(s -> !StringUtils.isEmpty(s))
			.mapToInt(s -> {
				if (offset > 0) {
					sb.append(s);
				}
				return StringUtils.indexOf(s, "i");
			})
			.forEach(pos -> {
				sb.append("c");
				sb.append(pos + "d");
			});
		return sb.toString();
	}

	public String saveComments(String input) {
		final List<String> list = generateList(input);
		final StringBuilder sb = new StringBuilder();

		final int offset = 10;
		list.stream()
			.filter(s -> !StringUtils.isEmpty(s))
			.mapToInt(s -> {
				// I may be necessary here
				if (offset > 0) {
					sb.append(s);
				}
				return StringUtils.indexOf(s, "i");
			})
			.forEach(pos -> {
				// and here...
				sb.append("c");
				sb.append(pos + "d");
			});
		return sb.toString();
	}

	public String saveComments2(String input) {
		final List<String> list = generateList(input);
		final StringBuilder sb = new StringBuilder();

		final int offset = 10;
		list.stream()
			.filter(s -> !StringUtils.isEmpty(s))
			.mapToInt(s -> {
				final int i;// not important
				// not used
				final int j;
				return StringUtils.indexOf(s, "i");
			})
			.forEach(pos -> {
				sb.append("c");
				sb.append(pos + "d");
			});
		return sb.toString();
	}

	public String multipleDeclarationFragments(String input) {
		final List<String> list = generateList(input);
		final StringBuilder sb = new StringBuilder();

		list.stream()
			.filter(s -> !StringUtils.isEmpty(s))
			.mapToInt(s -> {
				sb.append(s);
				return StringUtils.indexOf(s, "i");
			})
			.forEach(pos -> {
				final int c = 0;
				sb.append("c");
				sb.append(pos + "d");
			});
		return sb.toString();
	}

	public String explicitParameterType() {
		final List<Number> numbers = new ArrayList<>();
		numbers.add(2.3);
		numbers.add(4.5);

		final StringBuilder sb = new StringBuilder();
		numbers.stream()
			.filter(n -> n.doubleValue() > 0)
			.map((Number n) -> (Double) n)
			.forEach((final Double d) -> {
				final String s = d.toString();
				sb.append(s);
			});

		return sb.toString();
	}

	public String explicitParameterizedArgumentType() {
		final List<Number> numbers = new ArrayList<>();
		numbers.add(2.3);
		numbers.add(4.5);

		final StringBuilder sb = new StringBuilder();
		numbers.stream()
			.filter(n -> n.doubleValue() > 0)
			.forEach((Number n) -> {
				final List<Number> nums = Collections.singletonList(n);
				final Double d = (Double) nums.get(0);
				final String s = d.toString();
				sb.append(nums.toString());
			});

		return sb.toString();
	}

	public String explicitArrayArgumentType() {
		final List<Number> numbers = new ArrayList<>();
		numbers.add(2.3);
		numbers.add(4.5);

		final StringBuilder sb = new StringBuilder();
		numbers.stream()
			.filter(n -> n.doubleValue() > 0)
			.forEach((Number n) -> {
				final Number[] nums = { n };
				final Double d = (Double) nums[0];
				final String s = d.toString();
				sb.append(nums.toString());
			});

		return sb.toString();
	}

	public String explicitPrimitiveType() {
		final List<Number> numbers = new ArrayList<>();
		numbers.add(2.3);
		numbers.add(4.5);

		final StringBuilder sb = new StringBuilder();
		numbers.stream()
			.filter(n -> n.doubleValue() > 0)
			.mapToInt((Number n) -> (int) n / 2)
			.forEach(sb::append);

		return sb.toString();
	}

	public String rawType() {

		final List rawList = generateRawListOfStrings();
		final StringBuilder sb = new StringBuilder();
		rawList.stream()
			.filter(o -> o != null)
			.forEach((Object n) -> {
				final String s = (String) n;
				final Number d = (int) Integer.valueOf(s) / 2;
				sb.append(d);
			});

		return sb.toString();
	}

	public String finalLocalVariable() {

		final List<Object> rawList = generateRawListOfStrings();
		final StringBuilder sb = new StringBuilder();
		rawList.stream()
			.filter(o -> o != null)
			.map((Object n) -> (String) n)
			.forEach((final String s) -> {
				final Number d = (int) Integer.valueOf(s) / 2;
				sb.append(d);
			});

		return sb.toString();
	}

	public String annotatedLocalVariable() {

		final List<Object> rawList = generateRawListOfStrings();
		final StringBuilder sb = new StringBuilder();
		rawList.stream()
			.filter(o -> o != null)
			.forEach((Object n) -> {
				@Deprecated
				final String s = (String) n;
				final Number d = (int) Integer.valueOf(s) / 2;

				sb.append(d);
			});

		return sb.toString();
	}

	public String rawTypeFromMethodInvocation() {

		final StringBuilder sb = new StringBuilder();
		this.generateRawListOfStrings()
			.stream()
			.filter(o -> o != null)
			.forEach((Object n) -> {
				final String s = (String) n;
				final Number d = (int) Integer.valueOf(s) / 2;
				sb.append(d);
			});

		return sb.toString();
	}

	public <T> void mapToGenericType(List<Person> refs) {
		final List<List<T>> keys = new ArrayList<>();
		refs.stream()
			.forEach(ref -> {
				final List<T> testKey = refToKey(ref);
				keys.add(testKey);
			});
	}

	public <T> void mapToNestedType(List<Person> refs) {
		final List<List<String>> keys = new ArrayList<>();
		refs.stream()
			.forEach(ref -> {
				final List<String> testKey = Collections.singletonList(ref.getName());
				keys.add(testKey);
			});
	}

	public void parameterizedMapMethod() {
		final StringBuilder sb = new StringBuilder();
		final List<Wrapper> wrappers = new ArrayList<>();
		wrappers.stream()
			.forEach(wrapp -> {
				final InnerClass innerClass = wrapp.getInnerClass();
				useInnerClass(innerClass);
				sb.append(innerClass.getName());
			});
	}

	public void multipleVariablesInitializedWithGenericMethod() {
		/*
		 * Similar to corner case in SIM-728
		 */
		final StringBuilder sb = new StringBuilder();
		final List<Wrapper> wrappers = new ArrayList<>();
		wrappers.forEach(wrapp -> {
			// Wrapper::getInnerClass is a generic method
			final InnerClass innerClass = wrapp.getInnerClass();
			final String toString = wrapp.toString();
			sb.append(innerClass.getName() + toString);
		});
	}

	public void unusedVariablesInitializedWithGenericMethod() {
		final StringBuilder sb = new StringBuilder();
		final List<Wrapper> wrappers = new ArrayList<>();
		wrappers.stream()
			.map(wrapp -> {
				/*
				 * The generic method is not used as initializer of the mapping
				 * variable.
				 */
				final InnerClass innerClass = wrapp.getInnerClass();
				final String strInnerClass = innerClass.toString();
				return wrapp.toString() + strInnerClass;
			})
			.forEach(sb::append);
	}

	public void useInnerClass(InnerClass innerClass) {

	}

	public <T> void mapToTypeVariable(List<Person> refs) {
		final List<T> keys = new ArrayList<>();
		refs.stream()
			.forEach(ref -> {
				final T testKey = refToKeyT();
				keys.add(testKey);
			});
	}

	private <T> T refToKeyT() {
		return null;
	}

	private <T> List<T> refToKey(Person ref) {
		ref.getBirthday();
		return new ArrayList<>();
	}

	private List generateRawListOfStrings() {
		final List rawList = Arrays.asList("2.3", "4.5");
		return rawList;
	}

	interface Inner {

	}

	class InnerClass implements Inner {
		public String getName() {
			return this.getClass()
				.getName();
		}
	}

	class Wrapper {
		public <I extends Inner> I getInnerClass() {
			return null;
		}
	}
}
