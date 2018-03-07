package eu.jsparrow.sample.preRule;

import java.util.Date;

public class DateDeprecatedRule {
	
	private String calendar1 = "don't hide me!";
	private Date unInitializedDate;
	private Date field = new Date(99, 1, 1);
	
	private Date /* name leading comment */ fieldWithComments /* name trailing comment */ = /* init leading comment */ new  /* init inner comment */ Date(99, 1, 1) /* init trailing comment */; // trailing comment
	
	private Date // name leading comment
	fieldWithLineComments 
	// name trailing comment
	= // init leading comment 
	new  
	// init inner comment  
	Date(99, 1, 1) // init trailing comment 
	; // trailing comment
	
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
	
	public void notDeprecated_dontReplace() {
		Date date = new Date();
	}
	
	public void savingComments() {
		// leading statement comment
		Date /* leading name comment */ date /* trailing name comment */ = /* leading init comment*/new /* inner init comment */ Date(99, 1, 1)/* trailing init comment */ ; // trailing statement comment
		
		Date 
		/* leading name comment */ 
		dateSurroundedByLineComments 
		// trailing name comment 
		= // leading init comment
		new // inner init comment /
		Date(99, 1, 1)// trailing init comment
		; // trailing statement comment
		
		if(date != null) 
			// leading comment
			date /* name comment */ = /* leading initializer comment */ new /* inner initializer comment */ Date(90, 1, 31) /* trailing initializer comment */ ; // trailing comment
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
