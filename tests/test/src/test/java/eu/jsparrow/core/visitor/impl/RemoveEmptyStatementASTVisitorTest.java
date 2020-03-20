package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
public class RemoveEmptyStatementASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setVisitor(new RemoveEmptyStatementASTVisitor());
	}

	@Test
	public void visit_removeEmptyStatementFromMethodBody_shouldRefactor() throws Exception {
		String original = "; int i = 0;";
		String expected = "int i = 0;";

		assertChange(original, expected);
	}

	@Test
	public void visit_removeEmptyStatementFromSwitchCaseStatement_shouldRefactor() throws Exception {
		String original = "switch (\"\") { case \"\": ; break; default: break; }";
		String expected = "switch (\"\") { case \"\": break; default: break; }";

		assertChange(original, expected);
	}

	@Test
	public void visit_removeEmptyStatementFromSwitchDefaultStatement_shouldRefactor() throws Exception {
		String original = "switch (\"\") { case \"\": break; default: ; break; }";
		String expected = "switch (\"\") { case \"\": break; default: break; }";

		assertChange(original, expected);
	}

	@Test
	public void visit_removeMultipleEmptyStatementFromMethodBody_shouldRefactor() throws Exception {
		String original = ";;;;;; ;;; ;;;; ;; int i = 0;;;;; ;;; ;;";
		String expected = "int i = 0;";

		assertChange(original, expected);
	}

	@Test
	public void visit_removeEmptyStatementFromIfBody_shouldRefactor() throws Exception {
		String original = "if(true) {;} int i = 0;";
		String expected = "if(true) { } int i = 0;";

		assertChange(original, expected);
	}

	@Test
	public void visit_singleBodyIfStatement_shouldNotRefactor() throws Exception {
		assertNoChange("if(true) ; int i = 0;");
	}

	@Test
	public void visit_singleBodyForStatement_shouldNotRefactor() throws Exception {
		fixture.addImport(java.util.Arrays.class.getName());
		assertNoChange("for(String string : Arrays.asList(\"\")) ;");
	}

	@Test
	public void visit_emptyForLoopHeader_shouldNotRefactor() throws Exception {
		fixture.addImport(java.util.Arrays.class.getName());
		assertNoChange("for(;;) {break;}");
	}
}
