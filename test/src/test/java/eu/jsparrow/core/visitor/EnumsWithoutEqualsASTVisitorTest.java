package eu.jsparrow.core.visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEdit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.jsparrow.jdtunit.JdtUnitFixture;

@SuppressWarnings({ "nls" }) 
public class EnumsWithoutEqualsASTVisitorTest {

	private EnumsWithoutEqualsASTVisitor visitor;

	private static JdtUnitFixture fixture;
	
	@BeforeClass
	public static void setUpClass() throws Exception {
		fixture = new JdtUnitFixture();
		fixture.setUp();
	}
	
	@AfterClass
	public static void tearDownClass() throws CoreException {
		fixture.tearDown();
	}
	
	@Before
	public void setUp(){
		visitor = new EnumsWithoutEqualsASTVisitor();
	}
	
	@After
	public void tearDown() throws Exception{
		fixture.clear();
	}
	
	@Test
	public void visit_EqualsWithEnumeration_ShouldReplaceWithInfix() throws Exception {
		fixture.addImport("java.math.RoundingMode");
		String methodBlock = "RoundingMode roundingMode; if(roundingMode.equals(RoundingMode.UP)){}";
		fixture.addMethodBlock(methodBlock);
		CompilationUnit astRoot = fixture.saveChanges();

		final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
		visitor.setAstRewrite(astRewrite);

		astRoot.accept(visitor);

		TextEdit edit = astRewrite.rewriteAST();
		astRoot = fixture.saveChanges(edit);

		InfixExpression infix = findFirstInfixWithOperator(fixture.getMethodBlock(), InfixExpression.Operator.EQUALS);
		assertNotNull(infix);
		assertEquals("roundingMode", infix.getLeftOperand().toString());
		assertEquals("RoundingMode.UP", infix.getRightOperand().toString());
	}
	
	@Test
	public void visit_EqualsWithEnumerationSwitched_ShouldReplaceWithInfix() throws Exception {
		fixture.addImport("java.math.RoundingMode");
		String methodBlock = "RoundingMode roundingMode; if(RoundingMode.UP.equals(roundingMode)){}";
		fixture.addMethodBlock(methodBlock);
		CompilationUnit astRoot = fixture.saveChanges();

		final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
		visitor.setAstRewrite(astRewrite);
		astRoot.accept(visitor);

		TextEdit edit = astRewrite.rewriteAST();
		astRoot = fixture.saveChanges(edit);

		InfixExpression infix = findFirstInfixWithOperator(fixture.getMethodBlock(), InfixExpression.Operator.EQUALS);
		assertNotNull(infix);
		assertEquals("RoundingMode.UP", infix.getLeftOperand().toString());
		assertEquals("roundingMode", infix.getRightOperand().toString());
	}
	
	@Test
	public void visit_EqualsWithEnumerationAndNegation_ShouldReplaceWithNotEqualsInfix() throws Exception {
		fixture.addImport("java.math.RoundingMode");
		String methodBlock = "RoundingMode roundingMode; if(!RoundingMode.UP.equals(roundingMode)){}";
		fixture.addMethodBlock(methodBlock);
		CompilationUnit astRoot = fixture.saveChanges();

		final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
		visitor.setAstRewrite(astRewrite);
		astRoot.accept(visitor);

		TextEdit edit = astRewrite.rewriteAST();
		astRoot = fixture.saveChanges(edit);

		InfixExpression infix = findFirstInfixWithOperator(fixture.getMethodBlock(), InfixExpression.Operator.NOT_EQUALS);
		assertNotNull(infix);
		assertEquals("RoundingMode.UP", infix.getLeftOperand().toString());
		assertEquals("roundingMode", infix.getRightOperand().toString());
	}
	
	@Test
	public void visit_EqualsWithString_ShouldNotReplace() throws Exception {
		String methodBlock = "String myString; if(myString.equals(\"\")){}";
		fixture.addMethodBlock(methodBlock);
		CompilationUnit astRoot = fixture.saveChanges();

		final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
		visitor.setAstRewrite(astRewrite);
		astRoot.accept(visitor);

		TextEdit edit = astRewrite.rewriteAST();
		boolean hasEdits = !Arrays.asList(edit.getChildren()).isEmpty();
		assertFalse(hasEdits);
	}
	
	@Test
	public void visit_CompareToWithEnum_ShouldNotReplace() throws Exception {
		String methodBlock = "RoundingMode roundingMode; if(RoundingMode.UP.compareTo(roundingMode) > 0){}";
		fixture.addMethodBlock(methodBlock);
		CompilationUnit astRoot = fixture.saveChanges();

		final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
		visitor.setAstRewrite(astRewrite);
		astRoot.accept(visitor);

		TextEdit edit = astRewrite.rewriteAST();
		boolean hasEdits = !Arrays.asList(edit.getChildren()).isEmpty();
		assertFalse(hasEdits);
	}
	
	@Test
	public void visit_EqualsWithoutArgument_ShouldNotReplace() throws Exception {
		String methodBlock = "RoundingMode roundingMode; if(RoundingMode.UP.equals()){}";
		fixture.addMethodBlock(methodBlock);
		CompilationUnit astRoot = fixture.saveChanges();

		final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
		visitor.setAstRewrite(astRewrite);
		astRoot.accept(visitor);

		TextEdit edit = astRewrite.rewriteAST();
		boolean hasEdits = !Arrays.asList(edit.getChildren()).isEmpty();
		assertFalse(hasEdits);
	}
	
	@Test
	public void visit_EqualsWithoutExpression_ShouldNotReplace() throws Exception {
		String methodBlock = "RoundingMode roundingMode; if(equals(RoundingMode.UP)){}";
		fixture.addMethodBlock(methodBlock);
		CompilationUnit astRoot = fixture.saveChanges();

		final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());
		visitor.setAstRewrite(astRewrite);
		astRoot.accept(visitor);

		TextEdit edit = astRewrite.rewriteAST();
		boolean hasEdits = !Arrays.asList(edit.getChildren()).isEmpty();
		assertFalse(hasEdits);
	}
	
	private InfixExpression findFirstInfixWithOperator(Block block, InfixExpression.Operator operator) {
		SearchInfixVisitor visitor = new SearchInfixVisitor();
		block.accept(visitor);
		InfixExpression infix = visitor.getFound();
		return infix;
	}

	private class SearchInfixVisitor extends ASTVisitor {

		private InfixExpression found;

		@Override
		public boolean visit(InfixExpression mi) {
			found = mi;
			return true;
		}

		public InfixExpression getFound() {
			return found;
		}
	}
	
	private class SearchPrefixVisitor extends ASTVisitor {

		private PrefixExpression found;

		@Override
		public boolean visit(PrefixExpression mi) {
			found = mi;
			return true;
		}

		public PrefixExpression getFound() {
			return found;
		}
	}
}
