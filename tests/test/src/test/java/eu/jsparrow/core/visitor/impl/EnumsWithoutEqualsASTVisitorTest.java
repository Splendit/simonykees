package eu.jsparrow.core.visitor.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.dummies.ASTRewriteVisitorListenerStub;

@SuppressWarnings({ "nls" })
public class EnumsWithoutEqualsASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() {
		setVisitor(new EnumsWithoutEqualsASTVisitor());
	}

	@Test
	public void visit_EqualsWithEnumeration_ShouldReplaceWithInfix() throws Exception {
		fixture.addImport("java.math.RoundingMode");
		String original = "RoundingMode roundingMode = RoundingMode.UP; if(roundingMode.equals(RoundingMode.UP)){}";
		String expected = "RoundingMode roundingMode = RoundingMode.UP; if(roundingMode == RoundingMode.UP){}";
		assertChange(original, expected);		
	}

	@Test
	public void visit_EqualsWithEnumerationSwitched_ShouldReplaceWithInfix() throws Exception {
		fixture.addImport("java.math.RoundingMode");
		String original = "RoundingMode roundingMode = RoundingMode.UP; if(RoundingMode.UP.equals(roundingMode)){}";
		String expected = "RoundingMode roundingMode = RoundingMode.UP; if(RoundingMode.UP == roundingMode){}";
		assertChange(original, expected);
	}

	@Test
	public void visit_EqualsWithEnumerationAndNegation_ShouldReplaceWithNotEqualsInfix() throws Exception {
		fixture.addImport("java.math.RoundingMode");
		String original = "RoundingMode roundingMode = RoundingMode.UP; if(!RoundingMode.UP.equals(roundingMode)){}";
		String expected = "RoundingMode roundingMode = RoundingMode.UP; if(RoundingMode.UP != roundingMode){}";
		assertChange(original, expected);
	}

	@Test
	public void visit_EqualsWithString_ShouldNotReplace() throws Exception {
		assertNoChange("String myString = \"\"; if(myString.equals(\"\")){}");
	}

	@Test
	public void visit_CompareToWithEnum_ShouldNotReplace() throws Exception {
		assertNoChange("RoundingMode roundingMode = RoundingMode.UP; if(RoundingMode.UP.compareTo(roundingMode) > 0){}");
	}

	@Test
	public void visit_EqualsWithoutArgument_ShouldNotReplace() throws Exception {
		assertNoChange("RoundingMode roundingMode = RoundingMode.UP; if(RoundingMode.UP.equals()){}");
	}

	@Test
	public void visit_EqualsWithoutExpression_ShouldNotReplace() throws Exception {
		assertNoChange("RoundingMode roundingMode = RoundingMode.UP; if(equals(RoundingMode.UP)){}");
	}

	@Test
	public void visit_EqualsWithEnumeration_ShouldUpdateListeners() throws Exception {
		EnumsWithoutEqualsASTVisitor enumsWithoutEqualsASTVisitor = new EnumsWithoutEqualsASTVisitor();
		ASTRewriteVisitorListenerStub listener = new ASTRewriteVisitorListenerStub();
		enumsWithoutEqualsASTVisitor.addRewriteListener(listener);
		fixture.addImport("java.math.RoundingMode");
		fixture.addMethodBlock("RoundingMode roundingMode = RoundingMode.UP; if(roundingMode.equals(RoundingMode.UP)){}");
		enumsWithoutEqualsASTVisitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(enumsWithoutEqualsASTVisitor);

		assertTrue(listener.wasUpdated());
	}

}
