package at.splendit.simonykees.sample.preRule;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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
	
	public String testWhileLoopsDiscardIterator(String input) {
		List<String> l = generateList(input);
		StringBuilder sb = new StringBuilder();
		
		Iterator<String> iterator = l.iterator();
		while(iterator.hasNext()) {
			iterator.next();
			sb.append("nothing");
		}
		
		return sb.toString();
	}
}
