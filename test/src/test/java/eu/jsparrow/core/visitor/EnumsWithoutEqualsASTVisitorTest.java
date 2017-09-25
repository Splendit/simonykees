package eu.jsparrow.core.visitor;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
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
public class EnumsWithoutEqualsASTVisitorTest extends AbstractASTVisitorTest {

	@Before
	public void setUp() {
		visitor = new EnumsWithoutEqualsASTVisitor();
	}

	@Test
	public void visit_EqualsWithEnumeration_ShouldReplaceWithInfix() throws Exception {
		fixture.addImport("java.math.RoundingMode");
		fixture.addMethodBlock("RoundingMode roundingMode; if(roundingMode.equals(RoundingMode.UP)){}");
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock("RoundingMode roundingMode; if(roundingMode == RoundingMode.UP){}");
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_EqualsWithEnumerationSwitched_ShouldReplaceWithInfix() throws Exception {
		fixture.addImport("java.math.RoundingMode");
		fixture.addMethodBlock("RoundingMode roundingMode; if(RoundingMode.UP.equals(roundingMode)){}");
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock("RoundingMode roundingMode; if(RoundingMode.UP == roundingMode){}");
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_EqualsWithEnumerationAndNegation_ShouldReplaceWithNotEqualsInfix() throws Exception {
		fixture.addImport("java.math.RoundingMode");
		fixture.addMethodBlock("RoundingMode roundingMode; if(!RoundingMode.UP.equals(roundingMode)){}");
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock("RoundingMode roundingMode; if(RoundingMode.UP != roundingMode){}");
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_EqualsWithString_ShouldNotReplace() throws Exception {
		String statements = "String myString; if(myString.equals(\"\")){}";
		fixture.addMethodBlock(statements);
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}

	@Test
	public void visit_CompareToWithEnum_ShouldNotReplace() throws Exception {
		String methodBlock = "RoundingMode roundingMode; if(RoundingMode.UP.compareTo(roundingMode) > 0){}";
		fixture.addMethodBlock(methodBlock);
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}

	@Test
	public void visit_EqualsWithoutArgument_ShouldNotReplace() throws Exception {
		String methodBlock = "RoundingMode roundingMode; if(RoundingMode.UP.equals()){}";
		fixture.addMethodBlock(methodBlock);
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}

	@Test
	public void visit_EqualsWithoutExpression_ShouldNotReplace() throws Exception {
		String methodBlock = "RoundingMode roundingMode; if(equals(RoundingMode.UP)){}";
		fixture.addMethodBlock(methodBlock);
		visitor.setAstRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}

}
