package at.splendit.simonykees.sample.postRule.allRules;

public class TestWhileToFor2Rule {
	/*
	 * Pre public void testNextOnlyIterator(){ List<String> stringList = new
	 * ArrayList<>();
	 * 
	 * Iterator<String> stringIterator = stringList.iterator(); String s = null;
	 * while((s = stringIterator.next()) != null){ System.out.println(s); } }
	 */
	/*
	 * Post public void testNextOnlyIterator(){ List<String> stringList = new
	 * ArrayList<>();
	 * 
	 * for(String s : stringList){ System.out.println(s); } }
	 */
}
