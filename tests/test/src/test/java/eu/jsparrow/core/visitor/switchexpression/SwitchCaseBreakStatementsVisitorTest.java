package eu.jsparrow.core.visitor.switchexpression;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.java16.switchexpression.SwitchCaseBreakStatementsVisitor;

class SwitchCaseBreakStatementsVisitorTest extends UsesSimpleJDTUnitFixture {

	private SwitchCaseBreakStatementsVisitor visitor;

	@BeforeEach
	void setUp() {
		visitor = new SwitchCaseBreakStatementsVisitor();
		setJavaVersion(JavaCore.VERSION_14);
	}
	
	private static Stream<String> codeExamples() {
		return Stream.of(
				/* for-loop in switch-case */
				""
				+ "int i = 0;\n"
				+ "switch(i) {\n"
				+ "case 0: for(int ii = 0; ii < 10; ii++) {\n"
				+ "	if(i == ii) break;\n"
				+ "} break;\n"
				+ "default:\n"
				+ "	System.out.println(\"none\");\n"
				+ "}",
				
				/* break statement in for-loop */
				""
				+ "int i = 0;\n"
				+ "for(int ii = 0; ii<10; ii++)\n"
				+ "if(Integer.valueOf(\"0\")==ii){\n"
				+ "	 break;\n"
				+ "}  else if (i == ii) {\n"
				+ "	break;\n"
				+ "}",
				
				/* break statement in enhanced for-loop */
				""
				+ "int i = 0;\n"
				+ "int[]ii = {};\n"
				+ "for(int j : ii) {\n"
				+ "	if(j == i) {\n"
				+ "		break;\n"
				+ "	} else if (j == 0) {\n"
				+ "		break;\n"
				+ "	}\n"
				+ "}",
						
				/* break statement in while loop */
				""
				+ "int i = 0;\n"
				+ "while(true) {\n"
				+ "	if(Integer.valueOf(\"0\")==i){\n"
				+ "		 break;\n"
				+ "	}  else if (i == 10) {\n"
				+ "		break;\n"
				+ "	}\n"
				+ "}",
				
				/* switch-statement in while-loop */
				""
				+ "int i = 0;\n"
				+ "while(true) {\n"
				+ "	i++;\n"
				+ "	switch(i) {\n"
				+ "	case 1: \n"
				+ "		System.out.println(\"1\");\n"
				+ "		break;\n"
				+ "	default: \n"
				+ "		System.out.println(\"1\");\n"
				+ "		break;\n"
				+ "	}\n"
				+ "	if(i==10) {\n"
				+ "		break;\n"
				+ "	} else if (i>15) {\n"
				+ "		break;\n"
				+ "	}\n"
				+ "	\n"
				+ "}", 
								
				/* Switch-statement in do-while loop*/
				""
				+ "do {\n"
				+ "	i++;\n"
				+ "	switch(i) {\n"
				+ "	case 1: \n"
				+ "		System.out.println(\"1\");\n"
				+ "		break;\n"
				+ "	default: \n"
				+ "		System.out.println(\"1\");\n"
				+ "		break;\n"
				+ "	}\n"
				+ "	if(i==10) {\n"
				+ "		break;\n"
				+ "	} else if (i>15) {\n"
				+ "		break;\n"
				+ "	}\n"
				+ "	\n"
				+ "} while(true);",
				/* Nested switch expression */
				""
				+ "String value = \"\";\n"
				+ "if(!value.isEmpty()) {\n"
				+ "	int i = switch(value) {\n"
				+ "	case \"2\" -> 2;\n"
				+ "	default -> 0;\n"
				+ "	};\n"
				+ "}", 
				
				/* Lambda Sample*/
				""
				+ "Runnable r = () -> {\n"
				+ "	switch (i) {\n"
				+ "	case 1: break;\n"
				+ "	case 2:\n"
				+ "	default: break;\n"
				+ "	}\n"
				+ "};",
				
				/* Anonymous Class Sample */
				""
				+ "Runnable r = new Runnable() {\n"
				+ "	@Override\n"
				+ "	public void run() {\n"
				+ "		switch (i) {\n"
				+ "		case 1: break;\n"
				+ "		case 2:\n"
				+ "		default: break;\n"
				+ "		}\n"
				+ "	}\n"
				+ "};",
				
				/* Local Class Sample */
				""
				+ "class Foo {\n"
				+ "	void foo(int i) {\n"
				+ "		switch (i) {\n"
				+ "		case 1: break;\n"
				+ "		case 2:\n"
				+ "		default: break;\n"
				+ "		}\n"
				+ "	}\n"
				+ "}\n"
				);
	}
	
	@ParameterizedTest
	@MethodSource("codeExamples")
	void visit_breakStatementsCodeExamples_shouldReturnNoBreaks(String methodBlock) throws Exception {
		fixture.addMethodBlock(methodBlock);
		Block block = fixture.getMethodBlock();
		block.accept(visitor);
		List<BreakStatement> breakStatements = visitor.getBreakStatements();
		assertTrue(breakStatements.isEmpty());
	}
}
