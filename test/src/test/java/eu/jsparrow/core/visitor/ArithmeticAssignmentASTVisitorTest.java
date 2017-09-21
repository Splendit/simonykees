package eu.jsparrow.core.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEdit;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.jsparrow.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;
import eu.jsparrow.jdtunit.JdtUnitFixture;

public class ArithmeticAssignmentASTVisitorTest {

	private ArithmethicAssignmentASTVisitor visitor;

	private static JdtUnitFixture fixture;

	@BeforeClass
	public static void setUpClass() throws Exception {
		fixture = new JdtUnitFixture();
		fixture.setUp();
	}

	@Before
	public void setUp() {
		visitor = new ArithmethicAssignmentASTVisitor();
	}

	@After
	public void tearDown() throws Exception {
		fixture.clear();
	}

	@Test
	public void visit_AssignmentWithAdd_ShouldReplaceAddAssignment() throws Exception {
		String methodBlock = "int a;  a = a + 3;";
		fixture.addMethodBlock(methodBlock);
		CompilationUnit astRoot = fixture.saveChanges();

		final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
		visitor.setAstRewrite(astRewrite);

		astRoot.accept(visitor);

		TextEdit edit = astRewrite.rewriteAST();
		astRoot = fixture.saveChanges(edit);

		String actual = fixture.getMethodBlock().toString();
		Assignment assignment = findFirstAssignmentWithOperator(fixture.getMethodBlock(),
				Assignment.Operator.PLUS_ASSIGN);
		assertNotNull(assignment);
		assertEquals("a+=3", assignment.toString());
	}

	private Assignment findFirstAssignmentWithOperator(ASTNode block, Assignment.Operator operator) {
		SearchAssignmentVisitor visitor = new SearchAssignmentVisitor();
		block.accept(visitor);
		return visitor.getFound();
	}

	private class SearchAssignmentVisitor extends ASTVisitor {

		private Assignment found;

		@Override
		public boolean visit(Assignment assignment) {
			found = assignment;
			return true;
		}

		public Assignment getFound() {
			return found;
		}
	}

}
