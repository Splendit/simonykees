package eu.jsparrow.core.visitor.junit.junit3;

import static eu.jsparrow.jdtunit.Matchers.assertMatch;

import java.util.Collections;

import org.eclipse.jdt.core.dom.Modifier;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class ReplaceJUnit3TestCasesToJupiterASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUpVisitor() throws Exception {
		addDependency("junit", "junit", "3.8.2");
		addDependency("org.junit.jupiter", "junit-jupiter-api", "5.4.0");
		Junit3MigrationConfiguration configuration = new Junit3MigrationConfigurationFactory()
			.createJUnit4ConfigurationValues();
		setDefaultVisitor(new ReplaceJUnit3TestCasesASTVisitor(configuration));
	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_fullyQualifiedAssertMethod_shouldTransform() throws Exception {
		defaultFixture.addImport("junit.framework.TestCase");
		defaultFixture.setSuperClassType("TestCase");

		String expected = "" +
				"@Test\n" +
				"public void test() {\n" +
				"	assertTrue(true);\n" +
				"}";

		defaultFixture.addMethod("test", "junit.framework.Assert.assertTrue(true);",
				Collections.singletonList(Modifier.ModifierKeyword.PUBLIC_KEYWORD));

		AbstractASTRewriteASTVisitor defaultVisitor = getDefaultVisitor();
		defaultVisitor.setASTRewrite(defaultFixture.getAstRewrite());
		defaultFixture.accept(getDefaultVisitor());

		assertMatch(
				ASTNodeBuilder.createTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, expected,
						Collections.singletonList("public")),
				defaultFixture.getTypeDeclaration());
	}
}
