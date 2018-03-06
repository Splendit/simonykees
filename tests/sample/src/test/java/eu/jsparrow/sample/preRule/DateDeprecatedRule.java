package eu.jsparrow.sample.preRule;

import java.util.Date;

public class DateDeprecatedRule {
	
	private String calendar1 = "don't hide me!";
	private Date unInitializedDate;
	
	private final Date instantiateMeInInitializerBlock;
	{
		instantiateMeInInitializerBlock = new Date(99, 1, 1);
	}
	
	public void replaceDeprecatedCtor() {
		
		Date date = new Date(99, 1, 1);
	}
	
	public void avoidConflictingMethodArgument(String calendar) {
		
		Date date = new Date(99, 1, 1);
	}
	
	
	public void introduceMultipleCalendarInstances(String calendar) {
		
		Date date = new Date(99, 1, 1);
		
		Date date2 = new Date(90, 31, 1);
	}
	
	public void noLocalDeclarations() {
		unInitializedDate = new Date(99, 1, 1);
	}
	
	
	class InnerClass {
		
		public void replaceInInnerClass() {
			Date date = new Date(99, 1, 1);
		}
		
		public void avoidConflictingOuterField(String calendar) {
			Date date = new Date(99, 1, 1);
		}
	}

}
