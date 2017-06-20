package at.splendit.simonykees.sample.preRule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({ "unused", "nls" })
public class MultiVariableDeclarationLineRule {
	private int a, b;
	int c = 10, d;
	@TestAnnotation List<Integer> list = new LinkedList<>(), list2;

	public String methodWithVariables() {
		Integer foo = 0, foo2, foo3 = Integer.valueOf(0);
		final List<? extends String> strings, strings2 = new ArrayList<>();
		if (foo > 0) {
			int e, f = 0;

			list.stream().map(element -> {
				int x, y = -20;

				return element;
			});
		}
		return "";
	}

	enum Foo {
		ASD, DFG;

		int a, b, c;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface TestAnnotation {

		public boolean testEnabled() default true;

		public String test = "", test2 = String.valueOf(10);
	}
}
