package eu.jsparrow.sample.postRule.allRules;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings({ "unused", "nls" })
public class MultiVariableDeclarationLineRule {
	private int a;
	private int b;
	int c = 10;
	int d;
	@TestAnnotation
	List<Integer> list = new LinkedList<>();
	@TestAnnotation
	List<Integer> list2;

	public String methodWithVariables() {
		final Integer foo = 0;
		final Integer foo2;
		final Integer foo3 = Integer.valueOf(0);
		final List<? extends String> strings;
		final List<? extends String> strings2 = new ArrayList<>();
		if (foo > 0) {
			final int e;
			final int f = 0;

			list.stream()
				.map(element -> {
					final int x;
					final int y = -20;

					return element;
				});
		}
		return "";
	}

	public void saveComments() {
		// unlinked comment
		final int a;
		final int b;

		// unlinked comment after c
		final int c; // trailing comment
		final int d // comment after d
		;
		final int e
		// comment after e
		;

		// I don't want to break anything...
		final int // I don't want to break anything...
		f;
		final int g;
	}

	enum Foo {
		ASD,
		DFG;

		int a;
		int b;
		int c;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface TestAnnotation {

		public boolean testEnabled() default true;

		public String test = "";
		public String test2 = String.valueOf(10);
	}
}
