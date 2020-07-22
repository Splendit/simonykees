package eu.jsparrow.core.visitor.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RemoveToStringOnStringASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new RemoveToStringOnStringASTVisitor());
	}

	@Test
	public void visit_parenthesisedMethodExpression_shouldTransformButNotUnwrap() throws Exception {
		String original = "System.out.println((\"abc\".toString() + System.getProperty(\"line.separator\", \"\\n\")).toString().hashCode());";
		String expected = "System.out.println((\"abc\" + System.getProperty(\"line.separator\", \"\\n\")).hashCode());";

		assertChange(original, expected);
	}
	
	@Test
	public void visit_toStringAsConsumerBody_shouldNotTransform() throws Exception {
		fixture.addImport(java.util.function.Consumer.class.getName());
		String original = "Consumer<String> consumer = (String value) -> value.toString();";
		assertNoChange(original);
	}

	@Test
	public void visit_toStringAsConsumerBodyWithMethodInvocationExpression_shouldTransform() throws Exception {
		fixture.addImport(java.util.function.Consumer.class.getName());
		String original = "Consumer<String> consumer = (String value) -> this.getClass().getName().toString();";
		String expected = "Consumer<String> consumer = (String value) -> this.getClass().getName();";
		assertChange(original, expected);
	}
	
	@Test
	public void visit_toStringAsConsumerBodyWithNewInstanceExpression_shouldTransform() throws Exception {
		fixture.addImport(java.util.function.Consumer.class.getName());
		String original = "Consumer<String> consumer = (String value) -> new String(value).toString();";
		String expected = "Consumer<String> consumer = (String value) -> new String(value);";
		assertChange(original, expected);
	}

	@Test
	public void visit_toStringAsSupplierBody_shouldTransform() throws Exception {
		fixture.addImport(java.util.function.Supplier.class.getName());
		String original = "Supplier<String> supplier = () -> \"\".toString();";
		String expected = "Supplier<String> supplier = () -> \"\";";
		assertChange(original, expected);
	}
}
