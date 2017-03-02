package at.splendit.simonykees.sample.postRule.diamondOperator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
	
	private String concatList(List objects) {
		objects.add(new Object());
		Object val = objects.stream().map(o -> o.toString()).collect(Collectors.joining(", "));
		return val.toString();
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
		if(ten > 0) {			
			list = new ArrayList<>();
			map = new HashMap<>();
			list.add(ten.toString());
			map.put(input, new GenericSample<String>(ten.toString()));
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
		if(list.isEmpty()) {
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
		concatList(new ArrayList<String>());
	}
}
