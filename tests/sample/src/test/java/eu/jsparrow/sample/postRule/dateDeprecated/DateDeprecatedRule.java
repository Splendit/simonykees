package eu.jsparrow.sample.postRule.dateDeprecated;

import java.util.Date;
import java.util.Calendar;

public class DateDeprecatedRule {
	
	private String calendar1 = "don't hide me!";
	private Date unInitializedDate;
	
	private final Date instantiateMeInInitializerBlock;
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(1999, 1, 1);
		instantiateMeInInitializerBlock = calendar.getTime();
	}
	
	public void replaceDeprecatedCtor() {
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(1999, 1, 1);
		Date date = calendar.getTime();
	}
	
	public void avoidConflictingMethodArgument(String calendar) {
		
		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(1999, 1, 1);
		Date date = calendar2.getTime();
	}
	
	
	public void introduceMultipleCalendarInstances(String calendar) {
		
		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(1999, 1, 1);
		Date date = calendar2.getTime();
		
		Calendar calendar3 = Calendar.getInstance();
		calendar3.set(1990, 31, 1);
		Date date2 = calendar3.getTime();
	}
	
	public void noLocalDeclarations() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(1999, 1, 1);
		unInitializedDate = calendar.getTime();
	}
	
	
	class InnerClass {
		
		public void replaceInInnerClass() {
			Calendar calendar = Calendar.getInstance();
			calendar.set(1999, 1, 1);
			Date date = calendar.getTime();
		}
		
		public void avoidConflictingOuterField(String calendar) {
			Calendar calendar2 = Calendar.getInstance();
			calendar2.set(1999, 1, 1);
			Date date = calendar2.getTime();
		}
	}

}
