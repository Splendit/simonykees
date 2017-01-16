package at.splendit.simonykees.sample.preRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

@SuppressWarnings({ "nls", "unused" })
public class TestWhileToForRule {

	private List<String> generateList(String input) {
		return Arrays.asList(input.split(";")); //$NON-NLS-1$
	}

	public String testWhileToFor(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> iterator = l.iterator();
		while (iterator.hasNext()) {
			String s = iterator.next();
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

		Iterator<String> iterator = l.iterator();
		String s;
		while (iterator.hasNext()) {
			Object k;
			s = iterator.next();
			sb.append(s);
		}
		return sb.toString();
	}

	public String testWhileToFor6(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> iterator = l.iterator();
		String s;
		s = "lalelu";
		while (iterator.hasNext()) {
			Object k;
			s = iterator.next();
			sb.append(s);
		}
		return sb.toString();
	}

	public String testWhileToFor7(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> iterator = l.iterator();
		String s;
		while (iterator.hasNext()) {
			Object k;
			s = iterator.next();
			sb.append(s);
		}
		s = "lalelu";
		return sb.toString();
	}

	public String testWhileToFor8(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> iterator = l.iterator();
		String s = "";
		while (iterator.hasNext()) {
			Object k;
			s = iterator.next();
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
		
		Iterator<String> iterator = l.iterator();
		while(iterator.hasNext()) {
			String s = iterator.next();
			sb.append(s);
			
			Iterator<String> innerIt = l.iterator();
			while(innerIt.hasNext()) {
				String innerStr = innerIt.next();
				sb.append(innerStr);
			}
		}
		
		return sb.toString();
	}
	
	public String testCascadedWhilesToFor(String input) {
		List<String> l = generateList(input);
		List<String> k = generateList(input);
		StringBuilder sb = new StringBuilder();

		Iterator<String> sIterator = l.iterator();
		while (sIterator.hasNext()) {
			String s = sIterator.next();
			sb.append(s);
		}
		
		Iterator<String> rIterator = k.iterator();
		while (rIterator.hasNext()) {
			String s = rIterator.next();
			sb.append(s);
		}
		
		return sb.toString();
	}
	
	public String testTripleNestedWhilesToFor(String input) {
		List<String> l = generateList(input);
		List<String> k = generateList(input);
		List<String> m = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		Iterator<String> outerIt = l.iterator();
		while (outerIt.hasNext()) {
			String outerVal = outerIt.next();
			sb.append(outerVal);
			
			Iterator<String> kIterator = k.iterator();
			while (kIterator.hasNext()) {
				String kVal = kIterator.next();
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
		
		Iterator<String> outerIt = l.iterator();
		while (outerIt.hasNext()) {
			String outerVal = outerIt.next();
			sb.append(outerVal);
			
			Iterator<String> kIterator = k.iterator();
			String kVal;
			if ((kVal = kIterator.next()) != null) {
				sb.append(kVal);
				
				Iterator<String> mIterator = m.iterator();
				while (mIterator.hasNext()) {
					String mVal = mIterator.next();
					sb.append(mVal);
				}
			}
		}

		return sb.toString();
	}
	
	public String testWhileLoopsMultipleDeclaration(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		Iterator<String> iterator = l.iterator();
		while(iterator.hasNext()) {
			String n = "nothing";
			Integer i = 1;
			String m = iterator.next();
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
		
		Iterator<String> iterator = l.iterator();
		while(iterator.hasNext()) {
			iterator.next();
			String p = "foo";
			sb.append(p);
		}
		
		return sb.toString();
	}
	
	public String testWhileLoopsNoIteratingVariable(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		Iterator<String> iterator = l.iterator();
		String s;
		String foo = "foo";
		while(iterator.hasNext()) {
			sb.append(iterator.next());
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
		
		Iterator<String> iterator = l.iterator();
		while(iterator.hasNext()) {
			switch (fooCase) {
			case "foo":
				String s = iterator.next();
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
		
		Iterator<String> iterator = l.iterator();
		String s;
		String foo = "foo";
		String suffix = "";
		String prefix = "";
		while(iterator.hasNext()) {
			try {
				if(l.size() > 0) {
					s = iterator.next();
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
		
		Iterator<String> iterator = l.iterator();
		
		String foo = "foo";
		String suffix = "";
		String prefix = "";
		while(iterator.hasNext()) {
		
			result = k
					.stream()
					.map(key -> {
						String s = iterator.next();
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
		
		Iterator<Number> iterator = numbers.iterator();
		while(iterator.hasNext()) {
			String foo = "foo";
			Number s = iterator.next();
			sb.append(s);
		}
		return sb.toString();
	}

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
}
