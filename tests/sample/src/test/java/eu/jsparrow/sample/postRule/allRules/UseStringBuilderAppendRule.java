package eu.jsparrow.sample.postRule.allRules;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class UseStringBuilderAppendRule {

	@SuppressWarnings("nl" + "s")
	private final String annotatedField = "";

	@MyCustomAnnotation(name = "Chaitanya", address = "Angra, " + "India")
	private void annotatedMethod() {

	}

	private void savingComments() {
		/* 1 */
		/* 5 */
		/* 6 */
		/* 10 */
		/* 11 */
		/* 16 */
		final String value = new StringBuilder().append("left" /* 2 */)
			.append(/* 3 */ "right" /* 4 */)
			.append("expanded1" /* 7 */)
			.append(/* 8 */ "expanded2" /* 9 */)
			.append(/* 12 */ 3 /* 13 */ + /* 14 */ 4/* 15 */)
			.toString(); /* 17 */

	}

	@Documented
	@Target(ElementType.METHOD)
	@Inherited
	@Retention(RetentionPolicy.RUNTIME)
	public @interface MyCustomAnnotation {
		int id() default 0;

		String name();

		String address();
	}

}
