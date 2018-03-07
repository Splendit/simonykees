package eu.jsparrow.sample.postRule.allRules;

import java.util.Calendar;
import java.util.Date;

public class DateDeprecatedRule {

	private String calendar1 = "don't hide me!";
	private Date unInitializedDate;
	private Date field;
	{
		Calendar calendar = Calendar.getInstance();
		calendar.set(1999, 1, 1);
		field = calendar.getTime();
	}

	private Date /* name leading comment */ fieldWithComments /*
																 * name trailing
																 * comment
																 */; // trailing
																		// comment
	{
		/* init leading comment */
		/* init inner comment */
		/* init trailing comment */
		Calendar calendar = Calendar.getInstance();
		calendar.set(1999, 1, 1);
		fieldWithComments /* name trailing comment */ = calendar.getTime();
	}

	private Date // name leading comment
	fieldWithLineComments
	// name trailing comment
	; // trailing comment
	{
		// init inner comment
		// init trailing comment
		Calendar calendar = Calendar.getInstance();
		calendar.set(1999, 1, 1);
		fieldWithLineComments
		// name trailing comment
				= calendar.getTime();
	}

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

	public void notDeprecated_dontReplace() {
		Date date = new Date();
	}

	public void savingComments() {
		Calendar calendar = Calendar.getInstance();
		calendar.set(1999, 1, 1);
		/* leading init comment */
		/* inner init comment */
		/* trailing init comment */
		// leading statement comment
		Date /* leading name comment */ date /* trailing name comment */ = calendar.getTime(); // trailing
																								// statement
																								// comment

		Calendar calendar2 = Calendar.getInstance();
		calendar2.set(1999, 1, 1);
		// inner init comment /
		// trailing init comment
		Date
		/* leading name comment */
		dateSurroundedByLineComments
		// trailing name comment
				= // leading init comment
				calendar2.getTime(); // trailing statement comment

		if (date != null) {
			Calendar calendar3 = Calendar.getInstance();
			calendar3.set(1990, 1, 31);
			/* leading initializer comment */
			/* inner initializer comment */
			/* trailing initializer comment */
			// leading comment
			date /* name comment */ = calendar3.getTime(); // trailing comment
		}
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
