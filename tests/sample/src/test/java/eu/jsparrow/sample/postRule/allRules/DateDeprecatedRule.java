package eu.jsparrow.sample.postRule.allRules;

import java.util.Calendar;
import java.util.Date;

public class DateDeprecatedRule {

	private final String calendar1 = "don't hide me!";
	private Date unInitializedDate;
	private final Date field;
	{
		final Calendar calendar = Calendar.getInstance();
		calendar.set(1999, 1, 1);
		field = calendar.getTime();
	}

	private final Date /* name leading comment */ fieldWithComments /*
																	 * name
																	 * trailing
																	 * comment
																	 */; // trailing
																			// comment
	{
		/* init leading comment */
		/* init inner comment */
		/* init trailing comment */
		final Calendar calendar = Calendar.getInstance();
		calendar.set(1999, 1, 1);
		fieldWithComments = calendar.getTime();
	}

	private final Date // name leading comment
	fieldWithLineComments
	// name trailing comment
	; // trailing comment
	{
		// init inner comment
		// init trailing comment
		final Calendar calendar = Calendar.getInstance();
		calendar.set(1999, 1, 1);
		fieldWithLineComments = calendar.getTime();
	}

	private final Date instantiateMeInInitializerBlock;
	{
		final Calendar calendar = Calendar.getInstance();
		calendar.set(1999, 1, 1);
		instantiateMeInInitializerBlock = calendar.getTime();
	}

	public void replaceDeprecatedCtor() {

		final Calendar calendar = Calendar.getInstance();
		calendar.set(1999, 1, 1);
		final Date date = calendar.getTime();
	}

	public void avoidConflictingMethodArgument(String calendar) {

		final Calendar calendar2 = Calendar.getInstance();
		calendar2.set(1999, 1, 1);
		final Date date = calendar2.getTime();
	}

	public void introduceMultipleCalendarInstances(String calendar) {

		final Calendar calendar2 = Calendar.getInstance();
		calendar2.set(1999, 1, 1);
		final Date date = calendar2.getTime();

		final Calendar calendar3 = Calendar.getInstance();
		calendar3.set(1990, 31, 1);
		final Date date2 = calendar3.getTime();
	}

	public void noLocalDeclarations() {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(1999, 1, 1);
		unInitializedDate = calendar.getTime();
	}

	public void notDeprecated_dontReplace() {
		final Date date = new Date();
	}

	public void savingComments() {
		final Calendar calendar = Calendar.getInstance();
		calendar.set(1999, 1, 1);
		/* leading init comment */
		/* inner init comment */
		/* trailing init comment */
		// leading statement comment
		Date /* leading name comment */ date /* trailing name comment */ = calendar.getTime(); // trailing
																								// statement
																								// comment

		final Calendar calendar2 = Calendar.getInstance();
		calendar2.set(1999, 1, 1);
		// inner init comment /
		// trailing init comment
		final Date
		/* leading name comment */
		dateSurroundedByLineComments
		// trailing name comment
				= // leading init comment
				calendar2.getTime(); // trailing statement comment

		if (date == null) {
			return;
		}
		final Calendar calendar3 = Calendar.getInstance();
		calendar3.set(1990, 1, 31);
		/* leading initializer comment */
		/* inner initializer comment */
		/* trailing initializer comment */
		// leading comment
		date /* name comment */ = calendar3.getTime(); // trailing comment
	}

	class InnerClass {

		public void replaceInInnerClass() {
			final Calendar calendar = Calendar.getInstance();
			calendar.set(1999, 1, 1);
			final Date date = calendar.getTime();
		}

		public void avoidConflictingOuterField(String calendar) {
			final Calendar calendar2 = Calendar.getInstance();
			calendar2.set(1999, 1, 1);
			final Date date = calendar2.getTime();
		}
	}

}
