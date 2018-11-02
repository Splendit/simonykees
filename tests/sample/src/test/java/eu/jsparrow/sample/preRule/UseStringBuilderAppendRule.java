package eu.jsparrow.sample.preRule;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class UseStringBuilderAppendRule {

	@SuppressWarnings("nl" + "s")
	private String annotatedField = "";

	@MyCustomAnnotation(name = "Chaitanya", address = "Angra, " + "India")
	private void annotatedMethod() {

	}

	private void savingComments() {
		String value = /* 1 */ "left" /* 2 */ + /* 3 */ "right" /* 4 */
				+ /* 5 */ ( /* 6 */ "expanded1" /* 7 */ + /* 8 */ "expanded2" /* 9 */ ) /*10*/
				+ /* 11 */ ( /* 12 */ 3 /* 13 */ + /* 14 */ 4/* 15 */ )/* 16 */ ; /* 17 */

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
