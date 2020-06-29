package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StringBufferToBuilderASTVisitorTest  extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new StringBufferToBuilderASTVisitor());
	}
	
	@Test
	public void visit_returningParenthesizedExpression_shouldNotTransform() throws Exception {
		
		String original = "" +
				"public class StringBufferToBuilder {\n" + 
				"	public StringBuffer getRequestURL() {\n" + 
				"		StringBuffer url = new StringBuffer();\n" + 
				"		url.append(\"value\");\n" + 
				"		return (url);\n" + 
				"	}\n" + 
				"}";
		
		assertNoChange(original);
	}
}
