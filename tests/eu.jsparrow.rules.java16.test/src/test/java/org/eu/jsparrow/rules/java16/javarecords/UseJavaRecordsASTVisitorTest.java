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
					+ "			this.x = 0;\n"
					+ "			this.y = 0;",
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

	@ParameterizedTest
	@ValueSource(strings = { "x", "this.x" })
	public void visit_ComponentGetterRemoved_shouldTransform(String returnedExpression) throws Exception {
		String original = "" +
				"	private static final class Point {\n"
				+ "		\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		Point(int x) {\n"
				+ "			this.x = x;\n"
				+ "		}\n"
				+ "		\n"
				+ "		int x() {\n"
				+ "			return " + returnedExpression + ";\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	record Point(int x) {\n"
				+ "	}";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "			System.out.println(x);\n"
					+ "			return this.x;",
			""
					+ "return 0;",
			""
					+ "return new NestedClassWithPrivateFinalIntX(0).x;"
	})
	public void visit_ComponentGettersNotRemoved_shouldTransform(String componentGetterStatements) throws Exception {
		String original = "" +
				"	private static final class NestedClassWithPrivateFinalIntX {\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		NestedClassWithPrivateFinalIntX(int x) {\n"
				+ "			this.x = x;\n"
				+ "		}\n"
				+ "\n"
				+ "		public int x() {\n"
				+ componentGetterStatements + "\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	record NestedClassWithPrivateFinalIntX(int x) {\n"
				+ "		;\n"
				+ "		public int x() {\n"
				+ componentGetterStatements + "\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_EqualsMethodToRemove_shouldTransform() throws Exception {
		String original = "" +
				"	private static final class XWrapper {\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		XWrapper(int x) {\n"
				+ "			this.x = x;\n"
				+ "		}\n"
				+ "		\n"
				+ "		public boolean equals(Object other) {\n"
				+ "			return this == other;\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	record XWrapper(int x) {\n"
				+ "	}";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "		public boolean equals() {\n"
					+ "			return false;\n"
					+ "		}",
			""
					+ "		public boolean equals(XWrapper other) {\n"
					+ "			return this.x == other.x;\n"
					+ "		}"
	})
	public void visit_EqualsMethodNotToRemove_shouldTransform(String equalsMethod) throws Exception {
		String original = "" +
				"	private static final class XWrapper {\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		XWrapper(int x) {\n"
				+ "			this.x = x;\n"
				+ "		}\n"
				+ "		\n"
				+ equalsMethod + "\n"
				+ "	}";

		String expected = "" +
				"	record XWrapper(int x) {\n"
				+ "		;\n"
				+ equalsMethod + "\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_HashCodeMethodToRemove_shouldTransform() throws Exception {
		String original = "" +
				"	private static final class XWrapper {\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		XWrapper(int x) {\n"
				+ "			this.x = x;\n"
				+ "		}\n"
				+ "		\n"
				+ "		public int hashCode() {\n"
				+ "			return 0;\n"
				+ "		}	\n"
				+ "	}";

		String expected = "" +
				"	record XWrapper(int x) {\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_HashCodeMethodNotToRemove_shouldTransform() throws Exception {
		String original = "" +
				"	private static final class XWrapper {\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		XWrapper(int x) {\n"
				+ "			this.x = x;\n"
				+ "		}\n"
				+ "		\n"
				+ "		public int hashCode(int i) {\n"
				+ "			return i + i;\n"
				+ "		}	\n"
				+ "	}";

		String expected = "" +
				"	record XWrapper(int x) {\n"
				+ "		;\n"
				+ "		public int hashCode(int i) {\n"
				+ "			return i + i;\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "		int x() {\n"
					+ "			return 0;\n"
					+ "		}",
			""
					+ "		public static int x() {\n"
					+ "			return 0;\n"
					+ "		}",
			""
					+ "		public String x() {\n"
					+ "			return String.valueOf(x);\n"
					+ "		}",
	})
	public void visit_InvalidComponentGetters_shouldNotTransform(String invalidComponentGetter) throws Exception {
		String original = "" +
				"	private static final class XWrapper {\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		XWrapper(int x) {\n"
				+ "			this.x = x;\n"
				+ "		}\n"
				+ "		\n"
				+ invalidComponentGetter + "\n"
				+ "	}";
		assertNoChange(original);
	}
	
	@Test
	public void visit_AmbiguousCanonicConstructor_shouldNotTransform() throws Exception {
		String original = "" +
				"	private static final class StringWrapper {\n"
				+ "		private final String s;\n"
				+ "\n"
				+ "		public StringWrapper(String s) {\n"
				+ "			this.s = s;\n"
				+ "		}\n"
				+ "\n"
				+ "		public StringWrapper() {\n"
				+ "			this.s = \"\";\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);
	}
	
	@Test
	public void visit_InstanceInitializer_shouldNotTransform() throws Exception {
		String original = "" +
				"	private static final class StringWrapper {\n"
				+ "		private final String s;\n"
				+ "\n"
				+ "		{\n"
				+ "		}\n"
				+ "\n"
				+ "		public StringWrapper(String s) {\n"
				+ "			this.s = s;\n"
				+ "		}\n"
				+ "\n"
				+ "	}";
		assertNoChange(original);
	}
	
	@Test
	public void visit_FinalNonPrivateInstanceField_shouldNotTransform() throws Exception {
		String original = "" +
				"	private static final class StringWrapper {\n"
				+ "		final String s;\n"
				+ "\n"
				+ "		public StringWrapper(String s) {\n"
				+ "			this.s = s;\n"
				+ "		}\n"
				+ "\n"
				+ "	}";
		assertNoChange(original);
	}
	
	@Test
	public void visit_PrivateNonFinalInstanceField_shouldNotTransform() throws Exception {
		String original = "" +
				"	private static final class StringWrapper {\n"
				+ "		private String s;\n"
				+ "\n"
				+ "		public StringWrapper(String s) {\n"
				+ "			this.s = s;\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);
	}
	
	@Test
	public void visit_FieldWithInitializer_shouldNotTransform() throws Exception {
		String original = "" +
				"	private static final class StringWrapper {\n"
				+ "		private final String s;\n"
				+ "		private final int i = 0;\n"
				+ "\n"
				+ "		public StringWrapper(String s) {\n"
				+ "			this.s = s;\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);
	}
	
	@Test
	public void visit_FormalParametersCountNotMatching_shouldNotTransform() throws Exception {
		String original = "" +
				"	private static final class StringWrapper {\n"
				+ "		private final String s;\n"
				+ "		private final int length;\n"
				+ "\n"
				+ "		public StringWrapper(String s) {\n"
				+ "			this.s = s;\n"
				+ "			this.length = s.length();\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);
	}

	
	@Test
	public void visit_FormalParameterNameNotMatching_shouldNotTransform() throws Exception {
		String original = "" +
				"	private static final class StringWrapper {\n"
				+ "		private final String s;\n"
				+ "\n"
				+ "		public StringWrapper(String str) {\n"
				+ "			this.s = str;\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);
	}
	
	@Test
	public void visit_FormalParameterTypeNotMatching_shouldNotTransform() throws Exception {
		String original = "" +
				"	private static final class StringWrapper {\n"
				+ "		private final String s;\n"
				+ "\n"
				+ "		public StringWrapper(int s) {\n"
				+ "			this.s = String.valueOf(s);\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);
	}
}
