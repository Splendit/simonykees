package org.eu.jsparrow.rules.java16.javarecords;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import eu.jsparrow.rules.java16.javarecords.UseJavaRecordsASTVisitor;

@SuppressWarnings("nls")
public class UseJavaRecordsASTVisitorTest extends AbstractUseJavaRecordsTest {

	@BeforeEach
	public void setUp() {
		setDefaultVisitor(new UseJavaRecordsASTVisitor());
		fixtureProject.setJavaVersion(JavaCore.VERSION_16);
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_NestedClassWithPrivateFinalIntX_shouldTransform() throws Exception {
		String original = "" +
				"	private static final class NestedClassWithPrivateFinalIntX {\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		NestedClassWithPrivateFinalIntX (int x) {\n"
				+ "			this.x = x;\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	record NestedClassWithPrivateFinalIntX(int x) {\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_NestedClassWithStaticField_shouldTransform() throws Exception {
		String original = "" +
				"	private static final class NestedClassWithPrivateFinalIntX {\n"
				+ "		\n"
				+ "		static final int X_MAX = 1000;\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		NestedClassWithPrivateFinalIntX(int x) {\n"
				+ "			this.x = x;\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	record NestedClassWithPrivateFinalIntX (int x) {\n"
				+ "		;\n"
				+ "		static final int X_MAX = 1000;\n"
				+ "	}";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "			System.out.println(x + \",\" + y);\n"
					+ "			this.x = x;\n"
					+ "			this.y = y;",
			""
					+ "			if (x < 100) {\n"
					+ "				this.x = x;\n"
					+ "			} else {\n"
					+ "				this.x = 100;\n"
					+ "			}\n"
					+ "			if (y < 100) {\n"
					+ "				this.y = y;\n"
					+ "			} else {\n"
					+ "				this.y = 100;\n"
					+ "			}",
			""
					+ "			System.out.println(x + \",\" + y);\n"
					+ "			if (x > y) {\n"
					+ "				this.x = x;\n"
					+ "				this.y = y;\n"
					+ "			} else {\n"
					+ "				this.x = y;\n"
					+ "				this.y = x;\n"
					+ "			}",
			""
					+ "			this.x = Integer.MIN_VALUE;\n"
					+ "			this.y = Integer.MAX_VALUE;",
			""
					+ "			this.x = y;\n"
					+ "			this.y = x;"

	})
	public void visit_CanonicalConstructorNotRemoved_shouldTransform(String constructorStatements) throws Exception {
		String original = "" +
				"	private static final class Point {\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "		private final int y;\n"
				+ "\n"
				+ "		Point(int x, int y) {\n"
				+ constructorStatements + "\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	record Point(int x, int y) {\n"
				+ "		;\n"
				+ "		Point(int x, int y) {\n"
				+ constructorStatements + "\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

}
