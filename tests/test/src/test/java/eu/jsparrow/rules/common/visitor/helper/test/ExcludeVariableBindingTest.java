package eu.jsparrow.rules.common.visitor.helper.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jface.text.BadLocationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.jdtunit.JdtUnitException;
import eu.jsparrow.rules.common.visitor.helper.ExcludeVariableBinding;

class ExcludeVariableBindingTest extends UsesJDTUnitFixture {

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	private static SimpleName findUniqueSimpleName(ASTNode astNode, String idendifier,
			Predicate<SimpleName> predicate) {

		List<SimpleName> simpleNames = new ArrayList<>();
		ASTVisitor simpleNamesCollectorVisitor = new ASTVisitor() {

			@Override
			public boolean visit(SimpleName node) {
				if (node.getIdentifier()
					.equals(idendifier) && predicate.test(node)) {
					simpleNames.add(node);
				}
				return false;
			}

		};

		astNode.accept(simpleNamesCollectorVisitor);
		assertEquals(1, simpleNames.size());
		return simpleNames.get(0);
	}

	private SimpleName findUniqueSimpleName(String code, String identifier, Predicate<SimpleName> predicate)
			throws JdtUnitException, JavaModelException, BadLocationException {

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		return findUniqueSimpleName(defaultFixture.getTypeDeclaration(), identifier, predicate);
	}

	@Test
	void visit_methodInvocationName_shouldNotHaveVariableBinding()
			throws Exception {

		String code = "" +
				"	void callMethodF() {\n" +
				"		f();\n" +
				"	}\n" +
				"\n" +
				"	void f() {\n" +
				"	}";

		String identifier = "f";
		Predicate<SimpleName> predicate = (
				SimpleName simpleName) -> simpleName.getLocationInParent() == MethodInvocation.NAME_PROPERTY;

		SimpleName xMethodName = findUniqueSimpleName(code, identifier, predicate);

		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(xMethodName));
		final IBinding binding = xMethodName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);
	}


	@Test
	void visit_methodDeclarationName_shouldNotHaveVariableBinding()
			throws Exception {

		String code = "" +
				"	void callMethodF() {\n" +
				"		f();\n" +
				"	}\n" +
				"\n" +
				"	void f() {\n" +
				"	}";

		String identifier = "f";
		Predicate<SimpleName> predicate = (
				SimpleName simpleName) -> simpleName.getLocationInParent() == MethodDeclaration.NAME_PROPERTY;

		SimpleName xMethodName = findUniqueSimpleName(code, identifier, predicate);

		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(xMethodName));
		final IBinding binding = xMethodName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);

	}
	
	@Test
	void visit_ExpressionMethodReference_shouldNotHaveVariableBinding()
			throws Exception {

		String code = "" +
				"	Runnable r = this::f;\n" +
				"\n" +
				"	void f() {\n" +
				"	}";

		String identifier = "f";
		Predicate<SimpleName> predicate = (
				SimpleName simpleName) -> simpleName.getLocationInParent() == ExpressionMethodReference.NAME_PROPERTY;

		SimpleName xMethodName = findUniqueSimpleName(code, identifier, predicate);

		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(xMethodName));
		final IBinding binding = xMethodName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);

	}
	
	
	@Test
	void visit_SuperMethodReference_shouldNotHaveVariableBinding()
			throws Exception {

		String code = "" +
				"		static class SuperClass {\n"
				+ "			void superMethod() {\n"
				+ "			}\n"
				+ "		}\n"
				+ "\n"
				+ "		static class SubClass extends SuperClass {\n"
				+ "			Runnable r = super::superMethod;\n"
				+ "		}";

		String identifier = "superMethod";
		Predicate<SimpleName> predicate = (
				SimpleName simpleName) -> simpleName.getLocationInParent() == SuperMethodReference.NAME_PROPERTY;

		SimpleName xMethodName = findUniqueSimpleName(code, identifier, predicate);

		assertTrue(ExcludeVariableBinding.isVariableBindingExcludedFor(xMethodName));
		final IBinding binding = xMethodName.resolveBinding();
		assertNotNull(binding);
		assertFalse(binding instanceof IVariableBinding);

	}


}
