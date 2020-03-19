package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("nls")
public class RemoveDoubleNegationVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new RemoveDoubleNegationASTVisitor());
	}
	
	@Test
	public void visit_zeroNegation() throws Exception {
		assertNoChange("boolean a = true;");
	}

	@Test
	public void visit_singleNegation() throws Exception {
		assertNoChange("boolean a = !true;");
	}
	
	@Test
	public void visit_doubleNegation() throws Exception {
		assertChange("boolean a = !!true;", "boolean a = true;");
	}
	
	@Test
	public void visit_tripleNegation() throws Exception {
		assertChange("boolean a = !!!true;", "boolean a = !true;");
	}
	
	@Test
	public void visit_4timesNegation() throws Exception {
		assertChange("boolean a = !!!!true;", "boolean a = true;");
	}
	
	@Test
	public void visit_5timesNegation() throws Exception {
		assertChange("boolean a = !!!!!true;", "boolean a = !true;");
	}
	
	@Test
	public void visit_numericExpressionPrefix() throws Exception {
		assertNoChange("int i = 0; boolean a = ++i == 0;");
	}

}
