package at.splendit.simonykees.sample.postRule.allRules;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings({ "unused", "unchecked", "rawtypes" })
public class DiamondOperatorRule {

	private class GenericSample<T> {
		private T t;

		public GenericSample(T t) {
			this.t = t;
		}

		public T getValue() {
			return t;
		}
	}

	private abstract class Foo<T> {
		private T t;
		private List<T> field = new ArrayList<>();

		public Foo(T t) {
			setValue(t);
		}

		private void setValue(T t) {
			this.t = t;
		}

		public T getValue() {
			return t;
		}
	}

	private String concatRawTypeList(List objects) {
		objects.add(new Object());
		Object val = objects.stream().map(o -> o.toString()).collect(Collectors.joining(", "));
		return val.toString();
	}

	public List<String> sampleGenericReturnTypeMethod(String input) {
		return new ArrayList<>();
	}

	private String concatTypedList(List<String> foo, int i, Map<String, List<String>> map) {
		return foo.stream().collect(Collectors.joining(","));
	}

	public void inferListType() {
		List<String> list = new ArrayList<>();
		list.add(BigDecimal.TEN.toString());
	}

	public void inferMapTypes() {
		Map<String, Object> map = new HashMap<>();
		Integer ten = Integer.valueOf(10);
		map.put(BigDecimal.TEN.toString(), ten);
	}

	public void inferGenericTypes() {
		List<? extends Number> list = new ArrayList<Number>();
	}

	public void multipleDeclarationStatement(String input) {
		List<String> list1 = new ArrayList<>(), list2 = new ArrayList<>(), list3 = new ArrayList<>();
		list1.add(input);
		list2.add(input);
		list3.add(input);
	}

	public void lasyInstatiation(String input) {
		List<String> list;
		Map<String, GenericSample<String>> map;
		Integer ten = Integer.valueOf(10);
		if (ten > 0) {
			list = new ArrayList<>();
			map = new HashMap<>();
			list.add(ten.toString());
			map.put(input, new GenericSample<>(ten.toString()));
		}
	}

	public void multipleMapDeclarationStatement(String input) {
		Map<String, Number> map1 = new HashMap<>(), map2 = new HashMap<>(), map3 = new HashMap<>();
		map1.put(input, 10);
		map2.put(input, 11);
		map3.put(input, 12);
	}

	public void blockWithMapAndLists(String input) {
		List<String> list = new ArrayList<>();
		List<Integer> numList = new ArrayList<>();
		Map<String, Integer> map = new HashMap<>();
		Set<String> set = new HashSet<>();
		if (list.isEmpty()) {
			Set<Integer> numSet = new HashSet<>();
			list.add(input);
			numSet.add(input.hashCode());
			numList.add(input.length());
			map.put(input, input.hashCode());
			set.add(input);
		}
	}

	public String userDefinedGeneric(String input) {
		GenericSample<String> userDefinedGeneric = new GenericSample<>(input);

		return userDefinedGeneric.getValue();
	}

	public void nestedGenericTypes(String input) {
		GenericSample<String> userDefined = new GenericSample<>(input);
		List<GenericSample<String>> list = new ArrayList<>();
		list.add(userDefined);

		Map<String, GenericSample<String>> map = new HashMap<>();
		map.put(input, userDefined);
	}

	public void diamondRuleOnMethodInvocation(String input) {
		concatRawTypeList(new ArrayList<String>());
		concatTypedList(new ArrayList<>(), 1, new HashMap<>());
	}

	public String anonymousGenericInstatiation(String input) {
		Foo<String> foo = new Foo<String>(input) {
		};
		return foo.getValue();
	}
}
