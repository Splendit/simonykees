package eu.jsparrow.core.visitor.switchexpression;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.SwitchCaseReturnStatementsVisitor;

class SwitchCaseReturnStatementsVisitorTest extends UsesSimpleJDTUnitFixture {
	
	private SwitchCaseReturnStatementsVisitor visitor;

	@BeforeEach
	void setUp() {
		visitor = new SwitchCaseReturnStatementsVisitor();
		setJavaVersion(JavaCore.VERSION_14);
	}
	
	private static Stream<String> codeExamples() {
		return Stream.of( 
				
				/* Lambda Sample*/
				""
				+ "int i = 0;\n"
				+ "switch (i) {\n"
				+ "case 0:\n"
				+ "	Runnable r = () -> {\n"
				+ "		switch (i) {\n"
				+ "		case 1:\n"
				+ "			return;\n"
				+ "		case 2:\n"
				+ "		default:\n"
				+ "			return;\n"
				+ "		}\n"
				+ "	};\n"
				+ "	return;\n"
				+ "default:\n"
				+ "	System.out.println(\"none\");\n"
				+ "}",
				
				/* Anonymous Class Sample */
				""
				+ "int i = 0;\n"
				+ "switch (i) {\n"
				+ "case 0:\n"
				+ "	Runnable r = new Runnable() {\n"
				+ "		@Override\n"
				+ "		public void run() {\n"
				+ "			switch (i) {\n"
				+ "			case 1:\n"
				+ "				return;\n"
				+ "			case 2:\n"
				+ "			default:\n"
				+ "				return;\n"
				+ "			}\n"
				+ "		}\n"
				+ "	};\n"
				+ "	return;\n"
				+ "default:\n"
				+ "	System.out.println(\"none\");\n"
				+ "}",
				
				/* Local Class Sample */
				""
				+ "int i = 0;\n"
				+ "switch (i) {\n"
				+ "case 0:\n"
				+ "	class Foo {\n"
				+ "		void foo(int i) {\n"
				+ "			switch (i) {\n"
				+ "			case 1:\n"
				+ "				return;\n"
				+ "			case 2:\n"
				+ "			default:\n"
				+ "				return;\n"
				+ "			}\n"
				+ "		}\n"
				+ "\n"
				+ "	}\n"
				+ "	return;\n"
				+ "default:\n"
				+ "	System.out.println(\"none\");\n"
				+ "}"
				);
	}
	
	@ParameterizedTest
	@MethodSource("codeExamples")
	void visit_breakStatementsCodeExamples_shouldReturnOneBreak(String methodBlock) throws Exception {
		fixture.addMethodBlock(methodBlock);
		Block block = fixture.getMethodBlock();
		block.accept(visitor);
		boolean multipleReturn = visitor.hasMultipleReturnStatements();
		assertFalse(multipleReturn);
	}
}
