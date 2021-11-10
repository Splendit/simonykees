package org.eu.jsparrow.rules.java16.javarecords;

import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
				"	private record NestedClassWithPrivateFinalIntX(int x) {\n"
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
				"	private record NestedClassWithPrivateFinalIntX (int x) {\n"
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
				"	private record Point(int x, int y) {\n"
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
				"	private record Point(int x) {\n"
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
				"	private record NestedClassWithPrivateFinalIntX(int x) {\n"
				+ "		;\n"
				+ "		public int x() {\n"
				+ componentGetterStatements + "\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_MethodIntXWithParameter_shouldTransform() throws Exception {
		String original = "" +
				"	private static final class XWrapper {\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		XWrapper(int x) {\n"
				+ "			this.x = x;\n"
				+ "		}\n"
				+ "		\n"
				+ "		public int x(int value) {\n"
				+ "			return x + value;\n"
				+ "		}		\n"
				+ "	}";

		String expected = "" +
				"	private record XWrapper(int x) {\n"
				+ "		;\n"
				+ "		public int x(int value) {\n"
				+ "			return x + value;\n"
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
				"	private record XWrapper(int x) {\n"
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
				"	private record XWrapper(int x) {\n"
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
				"	private record XWrapper(int x) {\n"
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
				"	private record XWrapper(int x) {\n"
				+ "		;\n"
				+ "		public int hashCode(int i) {\n"
				+ "			return i + i;\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	public static Stream<Arguments> recordAndConstructorVisibility() throws Exception {
		return Stream.of(
				Arguments.of("private ", "private "),
				Arguments.of("private ", ""),
				Arguments.of("", ""),
				Arguments.of("", "protected "),
				Arguments.of("", "public "));
	}

	@ParameterizedTest
	@MethodSource("recordAndConstructorVisibility")
	public void visit_CanonicalConstructorVisibilitySufficient_shouldTransform(String recordVisibility,
			String constructorVisibility) throws Exception {
		String original = "" +
				"	" + recordVisibility + "static final class XWrapper {\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		" + constructorVisibility + "XWrapper(int x) {\n"
				+ "			if (x < 100) {\n"
				+ "				this.x = x;\n"
				+ "			} else {\n"
				+ "				this.x = 100;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		String expected = "" +
				"	" + recordVisibility + "record XWrapper(int x) {\n"
				+ "		;\n"
				+ "		" + constructorVisibility + "XWrapper(int x) {\n"
				+ "			if (x < 100) {\n"
				+ "				this.x = x;\n"
				+ "			} else {\n"
				+ "				this.x = 100;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_StaticInitializer_shouldTransform() throws Exception {
		String original = "" +
				"	private static final class StringWrapper {\n"
				+ "		private final String s;\n"
				+ "\n"
				+ "		static {\n"
				+ "		}\n"
				+ "\n"
				+ "		StringWrapper(String s) {\n"
				+ "			this.s = s;\n"
				+ "		}\n"
				+ "\n"
				+ "	}";

		String expected = "" +
				"	private record StringWrapper(String s) {\n"
				+ "		;\n"
				+ "		static {\n"
				+ "		}\n"
				+ "	}";

		assertChange(original, expected);
	}

	@Test
	public void visit_WrapperWithTypeParameter_shouldTransform() throws Exception {
		String original = "" +
				"	private static final class ValueWrapper<V> {\n"
				+ "\n"
				+ "		private final V value;\n"
				+ "\n"
				+ "		public ValueWrapper(V value) {\n"
				+ "			this.value = value;\n"
				+ "		}\n"
				+ "	} ";

		String expected = "" +
				"	private record ValueWrapper<V> (V value) {\n"
				+ "	} ";
		assertChange(original, expected);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "		public void x() {\n"
					+ "			return;\n"
					+ "		}",
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
				+ "		StringWrapper(String s) {\n"
				+ "			this.s = s;\n"
				+ "		}\n"
				+ "\n"
				+ "		StringWrapper() {\n"
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
				+ "		StringWrapper(String s) {\n"
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
				+ "		StringWrapper(String s) {\n"
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
				+ "		StringWrapper(String s) {\n"
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
				+ "		StringWrapper(String s) {\n"
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
				+ "		StringWrapper(String s) {\n"
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
				+ "		StringWrapper(String str) {\n"
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
				+ "		StringWrapper(int s) {\n"
				+ "			this.s = String.valueOf(s);\n"
				+ "		}\n"
				+ "	}";
		assertNoChange(original);
	}

	@Test
	public void visit_PrivateCanonicalConstructorForPackageScopeRecord_shouldNotTransform() throws Exception {
		String original = "" +
				"	static final class XWrapper {\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		private XWrapper(int x) {\n"
				+ "			if (x < 100) {\n"
				+ "				this.x = x;\n"
				+ "			} else {\n"
				+ "				this.x = 100;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_NestedClassWithinNestedClass_shouldNotTransform() throws Exception {
		String original = "" +
				"	private static final class StringWrapper {\n"
				+ "		private final String s;\n"
				+ "\n"
				+ "		public StringWrapper(String s) {\n"
				+ "			this.s = s;\n"
				+ "		}\n"
				+ "		\n"
				+ "		class NestedClassWithinStringWrapper {\n"
				+ "			\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AnnotationOnConstructorParameter_shouldNotTransform() throws Exception {
		String original = "" +
				"	@interface ExampleAnnotation {\n"
				+ "\n"
				+ "	}\n"
				+ "\n"
				+ "	private static final class XWrapper {\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		XWrapper(@ExampleAnnotation int x) {\n"
				+ "			this.x = x;\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AnnotationOnInstanceField_shouldNotTransform() throws Exception {
		String original = "" +
				"	@interface ExampleAnnotation {\n"
				+ "\n"
				+ "	}\n"
				+ "\n"
				+ "	private static final class XWrapper {\n"
				+ "\n"
				+ "		@ExampleAnnotation\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		XWrapper(int x) {\n"
				+ "			this.x = x;\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@Test
	public void visit_AnnotationOnGetter_shouldNotTransform() throws Exception {
		String original = "" +
				"	@interface ExampleAnnotation {\n"
				+ "\n"
				+ "	}\n"
				+ "\n"
				+ "	private static final class XWrapper {\n"
				+ "\n"
				+ "		private final int x;\n"
				+ "\n"
				+ "		XWrapper(int x) {\n"
				+ "			this.x = x;\n"
				+ "		}\n"
				+ "\n"
				+ "		@ExampleAnnotation\n"
				+ "		public int x() {\n"
				+ "			return this.x;\n"
				+ "		}\n"
				+ "	}";

		assertNoChange(original);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			""
					+ "	private static final class SetWrapper<E> {\n"
					+ "\n"
					+ "		private final Set elements;\n"
					+ "\n"
					+ "		public SetWrapper(Set<E> elements) {\n"
					+ "			this.elements = elements;\n"
					+ "		}\n"
					+ "	} ",
			""
					+ "	private static final class SetWrapper<E extends AbstractCharSequence> {\n"
					+ "\n"
					+ "		private final Set<? extends CharSequence> elements;\n"
					+ "\n"
					+ "		public SetWrapper(Set<E> elements) {\n"
					+ "			this.elements = elements;\n"
					+ "		}\n"
					+ "	}\n"
					+ "	\n"
					+ "	abstract static  class AbstractCharSequence implements CharSequence {\n"
					+ "		\n"
					+ "	}",
			""
					+ "	private static final class SetWrapper<E extends CharSequence> {\n"
					+ "\n"
					+ "		private final Set<E> elements;\n"
					+ "\n"
					+ "		public SetWrapper(Set<E> elements) {\n"
					+ "			this.elements = elements;\n"
					+ "		}\n"
					+ "\n"
					+ "		public Set<? extends CharSequence> elements() {\n"
					+ "			return this.elements;\n"
					+ "		}\n"
					+ "	}"

	})
	public void visit_PrivateFinalSetOfJokerExtendsCharSequence_shouldNotTransform(String original)
			throws Exception {

		defaultFixture.addImport(java.util.Set.class.getName());

		assertNoChange(original);
	}
}
