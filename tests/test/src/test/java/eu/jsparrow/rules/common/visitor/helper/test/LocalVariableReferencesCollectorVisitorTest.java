package eu.jsparrow.rules.common.visitor.helper.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ChildPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jface.text.BadLocationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.jdtunit.JdtUnitException;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableReferencesCollectorVisitor;

class LocalVariableReferencesCollectorVisitorTest extends UsesJDTUnitFixture {
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

	private SimpleName findUniqueSimpleName(String code, String identifier, Predicate<SimpleName> predicate)
			throws JdtUnitException, JavaModelException, BadLocationException {

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> simpleNames = new ArrayList<>();
		ASTVisitor simpleNamesCollectorVisitor = new ASTVisitor() {

			@Override
			public boolean visit(SimpleName node) {
				if (node.getIdentifier()
					.equals(identifier) && predicate.test(node)) {
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
	void visit_declareAndReturnLocalVariableX_shouldFindReference()
			throws Exception {

		String code = "" +
				"	int returnLocalVariable() {\n" +
				"		int x = 1;\n" +
				"		return x;\n" +
				"	}";

		SimpleName declarationFragmentName = findUniqueSimpleName(code, "x", VariableDeclarationFragment.NAME_PROPERTY);
		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) declarationFragmentName
			.getParent();
		Block block = (Block) declarationFragment.getParent()
			.getParent();
		LocalVariableReferencesCollectorVisitor visitor = new LocalVariableReferencesCollectorVisitor(
				getCompilationUnit(), declarationFragment);

		block.accept(visitor);
		assertEquals(1, visitor.getReferences()
			.size());
	}

	@Test
	void visit_assignFieldXBeforeLocalX_shouldNotFindReference()
			throws Exception {

		String code = "" +
				"	int x;\n" +
				"	void assignFieldXBeforeLocalX() {\n" +
				"		x = 1;\n" +
				"		int x = 1;\n" +
				"	}";

		Predicate<SimpleName> predicate = simpleName -> simpleName.getParent()
			.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY;

		SimpleName declarationFragmentName = findUniqueSimpleName(code, "x", predicate);
		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) declarationFragmentName
			.getParent();
		Block block = (Block) declarationFragment.getParent()
			.getParent();
		LocalVariableReferencesCollectorVisitor visitor = new LocalVariableReferencesCollectorVisitor(
				getCompilationUnit(), declarationFragment);

		block.accept(visitor);
		assertTrue(visitor.getReferences()
			.isEmpty());
	}

	@Test
	void visit_LocalClassFieldXBeforeLocalVariableX_shouldNotFindReference()
			throws Exception {

		String code = "" +
				"	void variableDeclarationBeforeX() {\n" +
				"		class LocalClass {\n" +
				"			int x;\n" +
				"		}\n" +
				"		int x = 1;\n" +
				"	}";

		Predicate<SimpleName> predicate = simpleName -> simpleName.getParent()
			.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY;

		SimpleName declarationFragmentName = findUniqueSimpleName(code, "x", predicate);
		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) declarationFragmentName
			.getParent();
		Block block = (Block) declarationFragment.getParent()
			.getParent();
		LocalVariableReferencesCollectorVisitor visitor = new LocalVariableReferencesCollectorVisitor(
				getCompilationUnit(), declarationFragment);

		block.accept(visitor);
		assertTrue(visitor.getReferences()
			.isEmpty());
	}

	@Test
	void visit_undefinedVariable_shouldHaveInvalidBinding()
			throws Exception {

		String code = "" +
				"	int returnUndefinedVariable() {\n" +
				"		Undef x = 1;\n" +
				"		int y = x;\n" +
				"	}";

		SimpleName declarationFragmentName = findUniqueSimpleName(code, "x", VariableDeclarationFragment.NAME_PROPERTY);
		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) declarationFragmentName
			.getParent();
		Block block = (Block) declarationFragment.getParent()
			.getParent();
		LocalVariableReferencesCollectorVisitor visitor = new LocalVariableReferencesCollectorVisitor(
				getCompilationUnit(), declarationFragment);

		block.accept(visitor);
		assertTrue(visitor.getReferences()
			.isEmpty());
		assertTrue(visitor.isInvalidBinding());

	}

	@Test
	void visit_QualifiedDeprecatedAnnotation_shouldExcludeVariableReference()
			throws Exception {

		String code = "" +
				"			void useDeprecatedAnnotation() {			\n"
				+ "				int Deprecated = 1;\n"
				+ "				@java.lang.Deprecated int xDeprecated;\n"
				+ "			}";

		SimpleName declarationFragmentName = findUniqueSimpleName(code, "Deprecated",
				VariableDeclarationFragment.NAME_PROPERTY);
		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) declarationFragmentName
			.getParent();
		Block block = (Block) declarationFragment.getParent()
			.getParent();
		LocalVariableReferencesCollectorVisitor visitor = new LocalVariableReferencesCollectorVisitor(
				getCompilationUnit(), declarationFragment);

		block.accept(visitor);
		assertTrue(visitor.getReferences()
			.isEmpty());
	}
	
	
	@Test
	void visit_returnIntegerMaxValue_shouldNotFindReference()
			throws Exception {

		String code = "" +
				"		int maxValueNotReference () {			\n"
				+ "			int MAX_VALUE = 10000;\n"
				+ "			return java.lang.Integer.MAX_VALUE;\n"
				+ "		}";

		SimpleName declarationFragmentName = findUniqueSimpleName(code, "MAX_VALUE",
				VariableDeclarationFragment.NAME_PROPERTY);
		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) declarationFragmentName
			.getParent();
		Block block = (Block) declarationFragment.getParent()
			.getParent();
		LocalVariableReferencesCollectorVisitor visitor = new LocalVariableReferencesCollectorVisitor(
				getCompilationUnit(), declarationFragment);

		block.accept(visitor);
		assertTrue(visitor.getReferences()
			.isEmpty());
	}
}
