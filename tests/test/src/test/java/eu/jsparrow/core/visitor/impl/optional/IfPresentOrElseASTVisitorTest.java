package eu.jsparrow.core.visitor.impl.optional;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;
import eu.jsparrow.core.visitor.optional.OptionalIfPresentOrElseASTVisitor;

@SuppressWarnings("nls")
public class IfPresentOrElseASTVisitorTest extends UsesJDTUnitFixture {
	
	private OptionalIfPresentOrElseASTVisitor visitor;
	
	@BeforeEach
	public void setUp() throws Exception {
		visitor = new OptionalIfPresentOrElseASTVisitor();
		fixture.addImport("java.util.Optional");
	}
	
	@Test
	public void visit_baseCase_shouldTransform() throws Exception {
		String original = "" +
				" 		Optional<String> optional = Optional.empty();\n" + 
				"		if(optional.isPresent()) {\n" + 
				"			String value = optional.get();\n" + 
				"			System.out.print(value);\n" + 
				"		} else {\n" + 
				"			System.out.println(\"No value\");\n" + 
				"		}";
		String expected = "" +
				"		Optional<String> optional = Optional.empty();\n" +
				"		optional.ifPresentOrElse(\n" +
				"			value -> System.out.print(value),\n" + 
				"			() -> System.out.println(\"No value\"));";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_throwsStatement_shouldNotTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.empty();" +
				"		if(optional.isPresent()) {\n" + 
				"			String value = optional.get();\n" + 
				"			System.out.print(value);\n" + 
				"		} else {\n" + 
				"			System.out.println(\"No value\");\n" + 
				"			throw new RuntimeException();\n" + 
				"		}";

		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_returnStatement_shouldNotTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.empty();" +
				"		if(optional.isPresent()) {\n" + 
				"			String value = optional.get();\n" + 
				"			System.out.print(value);\n" + 
				"			return;\n" + 
				"		} else {\n" + 
				"			System.out.println(\"No value\");\n" + 
				"		}";

		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_nonFinalVariables_shouldNotTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.empty();\n" + 
				"		int i = 0;\n" + 
				"		i++;\n" + 
				"		if(optional.isPresent()) {\n" + 
				"			String value = optional.get();\n" + 
				"			System.out.print(value);\n" + 
				"			i++;\n" + 
				"		} else {\n" + 
				"			System.out.println(\"No value\");\n" + 
				"		}";

		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_nonFinalVariablesInElseBlock_shouldNotTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.empty();\n" + 
				"		int j = 0;\n" + 
				"		if(optional.isPresent()) {\n" + 
				"			String value = optional.get();\n" + 
				"			System.out.print(value);\n" + 
				"		} else {\n" + 
				"			System.out.println(\"No value\");\n" + 
				"			j++;\n" + 
				"		}";

		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_multipleIfThenElseStatements_shouldNotTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.empty();\n" + 
				"		if(optional.isPresent()) {\n" + 
				"			String value = optional.get();\n" + 
				"			System.out.print(value);\n" + 
				"		} else if (t) {\n" + 
				"			System.out.println(\"t\");\n" + 
				"		} else {\n" + 
				"			System.out.println(\"No value\");\n" + 
				"		}";

		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_ifThenElseIf_shouldNotTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.empty();\n" + 
				"		\n" + 
				"		if(optional.isPresent()) {\n" + 
				"			String value = optional.get();\n" + 
				"			System.out.print(value);\n" + 
				"		} else if(t) {\n" + 
				"			System.out.println(\"No value\");\n" + 
				"		}";

		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_discardedOptionalGet_shouldNotTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.empty();\n" + 
				"		if(optional.isPresent()) {\n" + 
				"			String value = \"\";\n" + 
				"			optional.get();\n" + 
				"			optional.get();\n" + 
				"			System.out.print(value);\n" + 
				"		} else {\n" + 
				"			System.out.println(\"No value\");\n" + 
				"		}";

		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_compoundIfCondition_shouldNotTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.empty();\n" + 
				"		final int i = 0;\n" + 
				"		if(optional.isPresent() ||  i >= 0) {\n" + 
				"			String value = optional.get();\n" + 
				"			System.out.print(value);\n" + 
				"		} else {\n" + 
				"			System.out.println(\"No value\");\n" + 
				"		}";

		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_missingOptionalGet_shouldNotTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.empty();\n" + 
				"		if(optional.isPresent()) {\n" + 
				"			String value = optional.orElse(\"\");\n" + 
				"			System.out.print(value);\n" + 
				"		} else {\n" + 
				"			System.out.println(\"No value\");\n" + 
				"		}";

		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());
		fixture.accept(visitor);
		assertMatch(createBlock(original), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_emptyElseBlock_shouldTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.empty();\n" + 
				"		if(optional.isPresent()) {\n" + 
				"			String value = optional.get();\n" + 
				"			System.out.print(value);\n" + 
				"		} else {\n" + 
				"		}";
		String expected = "" +
				"		Optional<String> optional = Optional.empty();\n" +
				"		optional.ifPresentOrElse(" +
				"			value -> System.out.print(value), \n" + 
				"			() -> { });";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_singleStatementElse_shouldTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.empty();\n" + 
				"		if(optional.isPresent()) {\n" + 
				"			String value = optional.get();\n" + 
				"			System.out.print(value);\n" + 
				"		} else \n" + 
				"			System.out.println(\"No value\");";
		String expected = "" +
				"		Optional<String> optional = Optional.empty();\n" +
				"		optional.ifPresentOrElse(\n" + 
				"			value -> System.out.print(value),\n" + 
				"			() -> System.out.println(\"No value\"));";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(expected), fixture.getMethodBlock());
	}
	
	@Test
	public void visit_illegalElseExpression_shouldNotTransform() throws Exception {
		String original = "" +
				"		Optional<String> optional = Optional.empty();\n" + 
				"		if(optional.isPresent()) {\n" + 
				"			String value = optional.get();\n" + 
				"			System.out.print(value);\n" + 
				"		} else \n" + 
				"			for(int i =0; i <10; i++) {\n" + 
				"				System.out.println(i);\n" + 
				"			}";
		fixture.addMethodBlock(original);
		visitor.setASTRewrite(fixture.getAstRewrite());

		fixture.accept(visitor);

		assertMatch(createBlock(original), fixture.getMethodBlock());
	}

}
