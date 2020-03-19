package eu.jsparrow.sample.preRule;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RemoveCollectionAddAllRule {
	
	public void savingComments() {
		
		/* 1 */
		Set<String> set = new HashSet<>(/* 2 */);
		/* 3 */
		/* 4 */set./* 5 */addAll(/* 6 */Arrays.asList("value1", "value2")/* 7 */)/* 8 */;/* 9 */
		/* 10 */
	}

}
