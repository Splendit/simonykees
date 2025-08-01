package eu.jsparrow.sample.postRule.whileToForEach;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings({ "nls", "unused", "rawtypes", "unchecked" })
public class TestWhileToForEachRule {

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";")); //$NON-NLS-1$
	}
	
	public String unsafeIteratorName(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();
		String aL = "I am here to confuse you!";
		for (String aL1 : l //
) {
			sb.append(aL1);
		}
		return sb.toString();
	}
	
	public String loopingOverArrays(String input) {
		StringBuilder sb = new StringBuilder();
		String []array = {"-", input, "."};
		for (String t : array) {
			System.out.println(t);
			sb.append(t);
		}
		return sb.toString();
	}
	
	public String loopingOverLists(String input) {
		StringBuilder sb = new StringBuilder();
		List<String>list = generateList(input);
		for (String t : list) {
			System.out.println(t);
			sb.append(t);
		}
		return sb.toString();
	}

	public String testWhileToFor(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (String s : l) {
			sb.append(s);
		}
		return sb.toString();
	}

	public String testWhileToFor2(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> iterator = l.iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
			s = iterator.next();
			sb.append(s);
		}
		return sb.toString();
	}

	public String testWhileToFor3(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> iterator = l.iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
			s = iterator.next();
			s = iterator.next();
			sb.append(s);
		}
		return sb.toString();
	}

	public String testWhileToFor4(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> iterator = l.iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
			sb.append(s);
			iterator.remove();
			iterator.forEachRemaining(null);
		}
		return sb.toString();
	}

	public String testWhileToFor5(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		String s;
		for (String aL : l) {
			Object k;
			s = aL;
			sb.append(s);
		}
		return sb.toString();
	}

	public String testWhileToFor6(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		String s;
		s = "lalelu";
		for (String aL : l) {
			Object k;
			s = aL;
			sb.append(s);
		}
		return sb.toString();
	}

	public String testWhileToFor7(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		String s;
		for (String aL : l) {
			Object k;
			s = aL;
			sb.append(s);
		}
		s = "lalelu";
		return sb.toString();
	}

	public String testWhileToFor8(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		String s = "";
		for (String aL : l) {
			Object k;
			s = aL;
			sb.append(s);
		}
		sb.append(s);
		return sb.toString();
	}

	public String testNextOnlyIterator(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> stringIterator = l.iterator();
		String s = null;
		while ((s = stringIterator.next()) != null) {
			sb.append(s);
		}
		return sb.toString();
	}
	
	public String testNestedWhileLoops(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		for (String outerVal : l) {
			sb.append(outerVal);
			
			for (String innerStr : l) {
				sb.append(innerStr);
			}
		}
		
		return sb.toString();
	}
	
	public String testNestedWhileLoopsSingleBodyStatement(String input) {
		List<String> l = generateList(input);
		List<String> innerList = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		Iterator<String> innerIt = innerList.iterator();
		Iterator<String> iterator = l.iterator();
		
		while(iterator.hasNext()) 
			while(innerIt.hasNext())
				sb.append(innerIt.next() + iterator.next());
		
		return sb.toString();
	}
	
	public String testNestedWhileLoopsSingleBodyStatement2(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		Iterator<String> iterator = l.iterator();
		
		for(String outerKey : l)
			for (String aL : l)
				sb.append(aL + outerKey);
		
		return sb.toString();
	}
	
	public String testCascadedWhilesToFor(String input) {
		List<String> l = generateList(input);
		List<String> k = generateList(input);
		StringBuilder sb = new StringBuilder();

		for (String s : l) {
			sb.append(s);
		}
		
		for (String s : k) {
			sb.append(s);
		}
		
		return sb.toString();
	}
	
	public String testTripleNestedWhilesToFor(String input) {
		List<String> l = generateList(input);
		List<String> k = generateList(input);
		List<String> m = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		for (String outerVal : l) {
			sb.append(outerVal);
			
			for (String kVal : k) {
				sb.append(kVal);
				
//				FIXME SIM-173: RuleException is thrown
//				Iterator<String> mIterator = m.iterator(); 
//				while (mIterator.hasNext()) {
//					String mVal = mIterator.next();
//					sb.append(mVal);
//				}
			}
		}

		return sb.toString();
	}
	
	public String testNestedIfWhilesToFor(String input) {
		List<String> l = generateList(input);
		List<String> k = generateList(input);
		List<String> m = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		for (String outerVal : l) {
			sb.append(outerVal);
			
			Iterator<String> kIterator = k.iterator();
			String kVal;
			if ((kVal = kIterator.next()) != null) {
				sb.append(kVal);
				
				for (String mVal : m) {
					sb.append(mVal);
				}
			}
		}

		return sb.toString();
	}
	
	public String testWhileLoopsMultipleDeclaration(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		for (String m : l) {
			String n = "nothing";
			Integer i = 1;
			String o = "-";
			String p = "something";
			sb.append(n);
			sb.append(m);
			sb.append(o);
			sb.append(p);
			sb.append(i.toString());
		}
		
		return sb.toString();
	}
	
	public String testWhileLoopsIgnoreIterator(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		for (String aL : l) {
			String p = "foo";
			sb.append(p);
		}
		
		return sb.toString();
	}
	
	public String testWhileLoopsNoIteratingVariable(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		String s;
		String foo = "foo";
		for (String aL : l) {
			sb.append(aL);
		}
		
		return sb.toString();
	}
	
	public String testWhileLoopsRawList(String input) {
		List l = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		Iterator<String> iterator = l.iterator();
		String s;
		String foo = "foo";
		while(iterator.hasNext()) {
			sb.append(iterator.next());
		}
		
		return sb.toString();
	}
	
	public String testQualifiedNameIterator(String input) {
		Foo foo = new Foo();
		foo.l = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		for (String aL : foo.l) {
			sb.append(aL);
		}
		
		return sb.toString();
	}
	
	public String testWhileLoopsCompoundCondition(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		Iterator<String> iterator = l.iterator();
		String s;
		String foo = "foo";
		while(iterator.hasNext() && !foo.isEmpty()) {
			if(l.size() > 0) {
				s = iterator.next();
				sb.append(s + "|" + foo);
			}
		}
		
		return sb.toString();
	}
	
	public String testWhileLoopsWithSwitchCase(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();
		String fooCase = "foo";
		
		for (String s : l) {
			switch (fooCase) {
			case "foo":
				sb.append(s);
				break;
			case "b":
				sb.append("b");
				break;
			case "c":
				sb.append("c");
				break;
			default:
				sb.append("nothing");
				break;
			}
		}
		
		return sb.toString();
	}
	
	public String testWhileLoopsWithNestedTryCatch(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		String s;
		String foo = "foo";
		String suffix = "";
		String prefix = "";
		for (String aL : l) {
			try {
				if(l.size() > 0) {
					s = aL;
					prefix = s;
				}
			} catch (Exception e) {
				s = e.getLocalizedMessage();
			} finally {
				suffix = "|" + foo;
			}
			
			sb.append(prefix + suffix);
		}
		
		return sb.toString();
	}
	
	public String testWhileLoopsWithNestedLambda(String input) {
		List<String> l = generateList(input);
		List<String> k = generateList(input);
		List<String> result = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		
		String foo = "foo";
		String suffix = "";
		String prefix = "";
		for (String s : l) {
			result = k
					.stream()
					.map(key -> {
						return s + "|" + key + ";" ;
					}).collect(Collectors.toList());
			
			result.forEach(sb::append);
		}
		return sb.toString();
	}
	
	public String testWhileLoopsNumericIterator(String input) {
		List<String> l = generateList(input);
		List<Number> numbers = l.stream()
				.map(val -> val.hashCode())
				.collect(Collectors.toList());
		
		StringBuilder sb = new StringBuilder();
		
		for (Number s : numbers) {
			String foo = "foo";
			sb.append(s);
		}
		return sb.toString();
	}

	//SIM-211
	public String testIteratorReuse(String input) {
		List<String> l1 = generateList(input);
		List<String> l2 = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		Iterator iterator = l1.iterator();
		while (iterator.hasNext()) {
			String s = (String) iterator.next();
			int i = StringUtils.length(s);
			sb.append(s).append(i);
		}
		
		iterator = l2.iterator();
		
		while (iterator.hasNext()) {
			String s = (String) iterator.next();
			int i = StringUtils.length(s);
			sb.append(s).append(i);
		}
		
		return sb.toString();
	}
	
	public String testNonIterableCollection(String input) {
		StringBuilder sb = new StringBuilder();
		FooCollection<Number> numbers = new FooCollection<>();
		Iterator<Number> iterator = numbers.iterator();
		
		while(iterator.hasNext()) {
			String foo = "foo";
			Number s = iterator.next();
			sb.append(s);
		}
		return sb.toString();
	}
	
	/**
	 * This collection is not subtype of {@code Iterable}.
	 */
	private class FooCollection<T> {
		private final int size = 5;
		private int index = 0;

		public boolean hasNext() {
			return index < size;
		}

		public Iterator<T> iterator() {
			return new Iterator<T>() {

				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public T next() {
					return null;
				}
			};
		}
	}
	
	class Foo {
		public List<String> l;
	}
}
