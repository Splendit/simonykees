package org.eu.jsparrow.rules.java16.javarecords;

import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.rules.java16.javarecords.UseJavaRecordsASTVisitor;

@SuppressWarnings("nls")
public class UseJavaRecordsClassAnalysisASTVisitorTest extends AbstractUseJavaRecordsTest {

	private static final String BODY_DECLARATIONS = ""
			+ "			private final int x;\n"
			+ "			private final int y;\n"
			+ "\n"
			+ "			Point(int x, int y) {\n"
			+ "				this.x = x;\n"
			+ "				this.y = y;\n"
			+ "			}\n"
			+ "\n"
			+ "			public int x() {\n"
			+ "				return x;\n"
			+ "			}\n"
			+ "\n"
			+ "			public int y() {\n"
			+ "				return y;\n"
			+ "			}\n";

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
	public void visit_FinalLocalClassPoint_shouldTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		final class Point {\n"
				+ BODY_DECLARATIONS
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	public void methodWithLocalClassPoint() {\n" +
				"		record Point(int x, int y) {\n" +
				"		}\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_LocalClassPoint_shouldTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	public void methodWithLocalClassPoint() {\n" +
				"		record Point(int x, int y) {\n" +
				"		}\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_StaticClassInLocalClass_shouldTransform() throws Exception {
		String original = "" +
				"	void methodWithLocalClass() {\n"
				+ "		class LocalClassSurroundingStaticClass {\n"
				+ "			static class Point {\n"
				+ BODY_DECLARATIONS
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	void methodWithLocalClass() {\n"
				+ "		class LocalClassSurroundingStaticClass {\n"
				+ "			record Point (int x, int y) {\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_StaticClassInAnonymousClass_shouldTransform() throws Exception {
		String original = "" +
				"	Runnable runnable = new Runnable() {\n"
				+ "\n"
				+ "		static class Point {\n"
				+ BODY_DECLARATIONS
				+ "		}\n"
				+ "\n"
				+ "		@Override\n"
				+ "		public void run() {\n"
				+ "		}\n"
				+ "	};";

		String expected = "" +
				"	Runnable runnable = new Runnable() {\n"
				+ "\n"
				+ "		record Point  (int x, int y){\n"
				+ "		}\n"
				+ "\n"
				+ "		@Override\n"
				+ "		public void run() {\n"
				+ "		}\n"
				+ "	};";

		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"static abstract",
			"private final",
			"static",
			"public static final",
			"protected static final"
	})
	public void visit_NestedClassWithNotSupportedModifiers_shouldNotTransform(String modifiers)
			throws Exception {
		String original = "" +
				"	" + modifiers + " class Point {\n"
				+ BODY_DECLARATIONS
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_InterfacePoint_shouldNotransform() throws Exception {
		String original = "" +
				"	interface IPoint {\n"
				+ "		int x();\n"
				+ "		int y();\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_PointExtendsSuperClass_shouldNotTransform() throws Exception {
		String original = "" +
				"	private static final class Point extends SuperClass {\n"
				+ BODY_DECLARATIONS
				+ "	}\n"
				+ "	\n"
				+ "	private static class SuperClass {\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_PrivateStaticNestedClassNotEffectivelyFinal_shouldNotTransform() throws Exception {
		String original = "" +
				"	private static class Point {\n"
				+ BODY_DECLARATIONS
				+ "	}\n"
				+ "\n"
				+ "	private static class Point00 extends Point {\n"
				+ "		Point00(int x, int y) {\n"
				+ "			super(0, 0);\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_LocalClassNotEffectivelyFinal_shouldNotTransform() throws Exception {
		String original = "" +
				"	public void methodWithLocalClassPoint() {\n"
				+ "		class Point {\n"
				+ BODY_DECLARATIONS
				+ "		}\n"
				+ "		\n"
				+ "		class Point00 extends Point {\n"
				+ "			Point00(int x, int y) {\n"
				+ "				super(0, 0);\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	public static Stream<Arguments> modifierData() throws Exception {
		return Stream.of(
				Arguments.of("private static final", "private"),
				Arguments.of("private static", "private"),
				Arguments.of("static final", ""),
				Arguments.of("private static final strictfp", "private strictfp"));
	}

	@ParameterizedTest
	@MethodSource("modifierData")
	public void visit_NestedClassWithSupportedModifiers_shouldTransform(String classModifiers, String recordModifiers)
			throws Exception {
		String original = "" +
				"	" + classModifiers + " class Point {\n"
				+ BODY_DECLARATIONS
				+ "	}";

		String expected = "" +
				"	" + recordModifiers + " record Point(int x, int y) {\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_StaticFinalNestedClassPoint_shouldTransform()
			throws Exception {
		String original = "" +
				"	static final class Point {\n"
				+ BODY_DECLARATIONS
				+ "	}";

		String expected = "" +
				"	record Point(int x, int y) {\n" +
				"	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_PointImplementsInterface_shouldTransform() throws Exception {

		String original = "" +
				"	private static class Point implements IPoint {\n"
				+ BODY_DECLARATIONS
				+ "	}\n"
				+ "	\n"
				+ "	interface IPoint{\n"
				+ "		int x();\n"
				+ "		int y();\n"
				+ "	}";

		String expected = "" +
				"	private record Point(int x, int y) {\n"
				+ "	}\n"
				+ "	\n"
				+ "	interface IPoint{\n"
				+ "		int x();\n"
				+ "		int y();\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_SubclassNotExtendingPoint_shouldTransform() throws Exception {
		String original = "" +
				"	private static class Point {\n"
				+ "		private final int x;\n"
				+ "		private final int y;\n"
				+ "\n"
				+ "		Point(int x, int y) {\n"
				+ "			this.x = x;\n"
				+ "			this.y = y;\n"
				+ "		}\n"
				+ "\n"
				+ "		public int x() {\n"
				+ "			return x;\n"
				+ "		}\n"
				+ "\n"
				+ "		public int y() {\n"
				+ "			return y;\n"
				+ "		}\n"
				+ "	}\n"
				+ "\n"
				+ "	private static class SuperClass {\n"
				+ "\n"
				+ "	}\n"
				+ "\n"
				+ "	private static class SubClass extends SuperClass {\n"
				+ "\n"
				+ "	}";

		String expected = "" +
				"	private record Point(int x, int y) {\n"
				+ "	}\n"
				+ "\n"
				+ "	private static class SuperClass {\n"
				+ "\n"
				+ "	}\n"
				+ "\n"
				+ "	private static class SubClass extends SuperClass {\n"
				+ "\n"
				+ "	}";

		assertChange(original, expected);
	}

}
