package eu.jsparrow.sample.postRule.allRules;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@SuppressWarnings({ "unused", "unchecked", "rawtypes", "nls" })
public class DiamondOperatorRule {

	/**
	 * SIM-820 - if a diamond operator is used, eclipse will not indicate a
	 * compile error but the code will not compile.
	 */
	CtorExpectingLambdas<String> collection = new CtorExpectingLambdas<String>(ArrayList::new, IdentityHashMap::new);

	private String concatRawTypeList(List objects) {
		objects.add(new Object());
		Object val = objects.stream()
			.map(Object::toString)
			.collect(Collectors.joining(", "));
		return val.toString();
	}

	public List<String> sampleGenericReturnTypeMethod(String input) {
		return new ArrayList<>();
	}

	private String concatTypedList(List<String> foo, int i, Map<String, List<String>> map) {
		return foo.stream()
			.collect(Collectors.joining(","));
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
		List<String> list1 = new ArrayList<>();
		List<String> list2 = new ArrayList<>();
		List<String> list3 = new ArrayList<>();
		list1.add(input);
		list2.add(input);
		list3.add(input);
	}

	public void lasyInstatiation(String input) {
		List<String> list;
		Map<String, GenericSample<String>> map;
		Integer ten = Integer.valueOf(10);
		if (ten <= 0) {
			return;
		}
		list = new ArrayList<>();
		map = new HashMap<>();
		list.add(ten.toString());
		map.put(input, new GenericSample<>(ten.toString()));
	}

	public void multipleMapDeclarationStatement(String input) {
		Map<String, Number> map1 = new HashMap<>();
		Map<String, Number> map2 = new HashMap<>();
		Map<String, Number> map3 = new HashMap<>();
		map1.put(input, 10);
		map2.put(input, 11);
		map3.put(input, 12);
	}

	public void blockWithMapAndLists(String input) {
		List<String> list = new ArrayList<>();
		List<Integer> numList = new ArrayList<>();
		Map<String, Integer> map = new HashMap<>();
		Set<String> set = new HashSet<>();
		if (!list.isEmpty()) {
			return;
		}
		Set<Integer> numSet = new HashSet<>();
		list.add(input);
		numSet.add(input.hashCode());
		numList.add(input.length());
		map.put(input, input.hashCode());
		set.add(input);
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

	public void collectionArgumentInConstructor(String input) {
		ArrayList<String> ndCollection = new ArrayList<>(new ArrayList<String>(1));
	}

	public String anonymousGenericInstatiation(String input) {
		Foo<String> foo = new Foo<String>(input) {
		};
		return foo.getValue();
	}

	public void testOverloadWithTypeVariables_shouldNotChange(String input) {
		Foo<String> foo = new Foo<String>(input) {
		};
		List<GenericSample> result = foo.genericOverloaded(new ArrayList<GenericSample>());
		List<GenericSample> result2 = foo.genericOverloaded(new ArrayList<GenericSample>(), 0);
	}

	public void testNormalOverloading_shouldChangeInJava8(String input) {
		Foo<String> foo = new Foo<String>(input) {
		};
		List<GenericSample> result = foo.genericOverloaded(new ArrayList<>(), input);
		List<GenericSample> result2 = foo.genericOverloaded(new ArrayList<>(), input, 0);
	}

	public void savingComments() {
		/* Some comment here */
		List<String> myList = new ArrayList<>();
	}

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
		private List<String>[] arrayList;

		public Foo(T t) {
			setValue(t);
			arrayList = new ArrayList[2];
		}

		private void setValue(T t) {
			this.t = t;
		}

		public T getValue() {
			return t;
		}

		public <TypeVar> List<TypeVar> genericOverloaded(List<TypeVar> typeVariable) {
			/* save me */
			return new ArrayList<>();
		}

		public ArrayList<String> genericOverloaded(ArrayList<String> typeVariable) {
			return new ArrayList<// line comment doesn't change
			>();
		}

		public <TypeVar> List<TypeVar> genericOverloaded(List<TypeVar> typeVariable, int i) {
			return new ArrayList<>(i);
		}

		public ArrayList<String> genericOverloaded(ArrayList<String> typeVariable, int i) {
			return new ArrayList<>();
		}

		public <TypeVar> List<TypeVar> genericOverloaded(List<TypeVar> typeVariable, List<String> strings) {
			return new ArrayList<>();
		}

		public <TypeVar> List<TypeVar> genericOverloaded(List<TypeVar> typeVariable, String string, int i) {
			return new ArrayList<>(i);
		}

		public <TypeVar> List<TypeVar> genericOverloaded(List<TypeVar> typeVariable, String s) {
			return new ArrayList<>();
		}

		public <TypeVar> List<TypeVar> genericOverloaded() {
			return new ArrayList<>();
		}

		public void resetValue() {
			this.field = new ArrayList<>();
			arrayList[0] = new ArrayList<>();
			for (List<String> list = new ArrayList<>(); !list.isEmpty();) {
				list.add("");
			}
		}

		/**
		 * SIM-820
		 */
		public void useOverloadedMethodOnParameterizedTypes(String input) {
			/*
			 * Should not be changed
			 */
			List<GenericSample> result = genericOverloaded(new ArrayList<GenericSample>());
			List<GenericSample> result2 = genericOverloaded(new ArrayList<GenericSample>(), 0);

			/*
			 * Should be changed in java 8
			 */
			List<GenericSample> result3 = genericOverloaded(new ArrayList<>(), input);
			List<GenericSample> result4 = genericOverloaded(new ArrayList<>(), input, 0);
		}
	}

	class CtorExpectingLambdas<E> {
		Collection<E> collection;
		IdentityHashMap<E, Integer> map;

		public CtorExpectingLambdas(Supplier<List<E>> collectionFactory, Supplier<IdentityHashMap<E, Integer>> map) {
			this.collection = collectionFactory.get();
			this.map = map.get();
		}
	}
}
