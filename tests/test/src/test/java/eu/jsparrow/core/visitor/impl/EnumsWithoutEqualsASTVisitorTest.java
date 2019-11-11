package eu.jsparrow.core.visitor.impl;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.dom.Block;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.dummies.ASTRewriteVisitorListenerStub;

@SuppressWarnings({ "nls" })
public class EnumsWithoutEqualsASTVisitorTest extends UsesSimpleJDTUnitFixture {

	private EnumsWithoutEqualsASTVisitor visitor;
	
	@BeforeEach
	public void setUp() {
		visitor = new EnumsWithoutEqualsASTVisitor();
	}

	@Test
	public void visit_EqualsWithEnumeration_ShouldReplaceWithInfix() throws Exception {
		fixture.addImport("java.math.RoundingMode");
		fixture.addMethodBlock("RoundingMode roundingMode; if(roundingMode.equals(RoundingMode.UP)){}");
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock("RoundingMode roundingMode; if(roundingMode == RoundingMode.UP){}");
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_EqualsWithEnumerationSwitched_ShouldReplaceWithInfix() throws Exception {
		fixture.addImport("java.math.RoundingMode");
		fixture.addMethodBlock("RoundingMode roundingMode; if(RoundingMode.UP.equals(roundingMode)){}");
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock("RoundingMode roundingMode; if(RoundingMode.UP == roundingMode){}");
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_EqualsWithEnumerationAndNegation_ShouldReplaceWithNotEqualsInfix() throws Exception {
		fixture.addImport("java.math.RoundingMode");
		fixture.addMethodBlock("RoundingMode roundingMode; if(!RoundingMode.UP.equals(roundingMode)){}");
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		Block expected = createBlock("RoundingMode roundingMode; if(RoundingMode.UP != roundingMode){}");
		assertMatch(expected, fixture.getMethodBlock());
	}

	@Test
	public void visit_EqualsWithString_ShouldNotReplace() throws Exception {
		String statements = "String myString; if(myString.equals(\"\")){}";
		fixture.addMethodBlock(statements);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}

	@Test
	public void visit_CompareToWithEnum_ShouldNotReplace() throws Exception {
		String methodBlock = "RoundingMode roundingMode; if(RoundingMode.UP.compareTo(roundingMode) > 0){}";
		fixture.addMethodBlock(methodBlock);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}

	@Test
	public void visit_EqualsWithoutArgument_ShouldNotReplace() throws Exception {
		String methodBlock = "RoundingMode roundingMode; if(RoundingMode.UP.equals()){}";
		fixture.addMethodBlock(methodBlock);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}

	@Test
	public void visit_EqualsWithoutExpression_ShouldNotReplace() throws Exception {
		String methodBlock = "RoundingMode roundingMode; if(equals(RoundingMode.UP)){}";
		fixture.addMethodBlock(methodBlock);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertFalse(fixture.hasChanged());
	}

	@Test
	public void visit_EqualsWithEnumeration_ShouldUpdateListeners() throws Exception {
		ASTRewriteVisitorListenerStub listener = new ASTRewriteVisitorListenerStub();
		visitor.addRewriteListener(listener);
		fixture.addImport("java.math.RoundingMode");
		fixture.addMethodBlock("RoundingMode roundingMode; if(roundingMode.equals(RoundingMode.UP)){}");
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertTrue(listener.wasUpdated());
	}

}
