package at.splendit.simonykees.sample.postRule.multiVariableDeclarationLine;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
		Integer foo = 0;
		Integer foo2;
		Integer foo3 = Integer.valueOf(0);
		final List<? extends String> strings;
		final List<? extends String> strings2 = new ArrayList<>();
		if (foo > 0) {
			int e;
			int f = 0;

			list.stream().map(element -> {
				int x;
				int y = -20;

				return element;
			});
		}
		return "";
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
