package eu.jsparrow.rules.common.visitor.helper.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.BadLocationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.jdtunit.JdtUnitException;
import eu.jsparrow.rules.common.exception.UnresolvedBindingException;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.FindVariableBinding;

class FindVariableBindingTest extends UsesJDTUnitFixture {

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	private SimpleName findUniqueSimpleName(String code, String identifier, ChildPropertyDescriptor propertyDescriptor)
			throws JdtUnitException, JavaModelException, BadLocationException {

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> simpleNames = new ArrayList<>();
		ASTVisitor simpleNamesCollectorVisitor = new ASTVisitor() {

			@Override
			public boolean visit(SimpleName node) {
				if (node.getIdentifier()
					.equals(identifier) && node.getLocationInParent() == propertyDescriptor) {
					simpleNames.add(node);
				}
				return false;
			}

		};

		getCompilationUnit()
			.accept(simpleNamesCollectorVisitor);
		assertEquals(1, simpleNames.size());
		return simpleNames.get(0);
	}

	private CompilationUnit getCompilationUnit() {
		final TypeDeclaration typeDeclaration = defaultFixture.getTypeDeclaration();
		return ASTNodeUtil.getSpecificAncestor(typeDeclaration, CompilationUnit.class);
	}

	@Test
	void visit_declareVariableAndReturnIt_shouldFindVariableBinding()
			throws Exception {

		String code = "" +
				"	int returnLocalVariable() {\n" +
				"		int x = 1;\n" +
				"		return x;\n" +
				"	}";

		SimpleName uniqueSimpleName = findUniqueSimpleName(code, "x", ReturnStatement.EXPRESSION_PROPERTY);
		final IVariableBinding variableBinding = FindVariableBinding.findVariableBinding(uniqueSimpleName)
			.orElse(null);
		assertNotNull(variableBinding);

	}

	@Test
	void visit_importStaticMethod_shouldNotFindVariableBinding()
			throws Exception {

		defaultFixture.addImport("java.lang.Integer.valueOf", true, false);

		String code = "int x = valueOf(1);";

		SimpleName uniqueSimpleName = findUniqueSimpleName(code, "valueOf", QualifiedName.NAME_PROPERTY);
		final IVariableBinding variableBinding = FindVariableBinding.findVariableBinding(uniqueSimpleName)
			.orElse(null);
		assertNull(variableBinding);

	}

	public static Stream<Arguments> arguments_LabelX_notVariable() throws Exception {
		return Stream.of(
				Arguments.of(
						"" +
								"		x: while (true)\n" +
								"			break;",
						LabeledStatement.LABEL_PROPERTY),
				Arguments.of(
						"" +
								"		x: while (true)\n" +
								"			break x;",
						BreakStatement.LABEL_PROPERTY),
				Arguments.of(
						"" +
								"		while (true) {\n" +
								"			x: while (true)\n" +
								"				continue x;\n" +
								"		}",
						ContinueStatement.LABEL_PROPERTY));
	}

	@ParameterizedTest
	@MethodSource("arguments_LabelX_notVariable")
	void visit_LabelX_shouldNotFindVariableBinding(String code, ChildPropertyDescriptor labelProperty)
			throws Exception {

		String identifier = "x";
		String method = "" +
				"	void simpleNameAsLabelProperty() {\n" +
				code + "\n" +
				"	}";

		SimpleName uniqueSimpleName = findUniqueSimpleName(method, identifier, labelProperty);
		final IVariableBinding variableBinding = FindVariableBinding.findVariableBinding(uniqueSimpleName)
			.orElse(null);
		assertNull(variableBinding);
	}

	@Test
	void visit_undefinedVariable_shouldThrowUnresolvedBindingException()
			throws Exception {

		String code = "" +
				"	int returnUndefinedVariable() {\n" +
				"		return x;\n" +
				"	}";

		SimpleName uniqueSimpleName = findUniqueSimpleName(code, "x", ReturnStatement.EXPRESSION_PROPERTY);
		assertThrows(UnresolvedBindingException.class, () -> FindVariableBinding.findVariableBinding(uniqueSimpleName));
	}

}
