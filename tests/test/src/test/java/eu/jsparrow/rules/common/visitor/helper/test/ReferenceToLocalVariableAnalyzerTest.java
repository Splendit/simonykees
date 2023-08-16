package eu.jsparrow.rules.common.visitor.helper.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesJDTUnitFixture;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.ReferenceToLocalVariableAnalyzer;

class ReferenceToLocalVariableAnalyzerTest extends UsesJDTUnitFixture {

	@AfterEach
	void tearDown() throws Exception {
		fixtureProject.clear();
	}

	private static List<SimpleName> collectSimpleNames(ASTNode astNode, String idendifier) {
		List<SimpleName> simpleNames = new ArrayList<>();
		ASTVisitor simpleNamesCollectorVisitor = new ASTVisitor() {

			@Override
			public boolean visit(SimpleName node) {
				if (node.getIdentifier()
					.equals(idendifier)) {
					simpleNames.add(node);
				}
				return false;
			}

		};
		astNode.accept(simpleNamesCollectorVisitor);
		return simpleNames;
	}

	@Test
	void visit_declareVariableAndReturnIt_shouldBeReference()
			throws Exception {

		String code = "" +
				"	int declareVariableAndReturnIt() {\n" +
				"		int x = 1;\n" +
				"		return x;\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> variableNamesX = collectSimpleNames(defaultFixture.getTypeDeclaration(), "x");

		SimpleName firstX = variableNamesX.get(0);
		assertEquals(VariableDeclarationFragment.NAME_PROPERTY, firstX.getLocationInParent());

		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) firstX
			.getParent();

		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(declarationFragment, CompilationUnit.class);

		ReferenceToLocalVariableAnalyzer referenceAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit,
				declarationFragment);

		SimpleName secondX = variableNamesX.get(1);

		assertTrue(referenceAnalyzer.isReference(secondX));
	}

	@Test
	void visit_VariableDeclarationFragmentNameWithEqualIdentifier_shouldNotBeReference()
			throws Exception {

		String code = "" +
				"		void declaringVarialeWithSameNameInRunnable() {\n" +
				"\n" +
				"			int x = 1;\n" +
				"			Runnable r = new Runnable() {\n" +
				"				@Override\n" +
				"				public void run() {\n" +
				"					int x = 1;\n" +
				"				}\n" +
				"			};\n" +
				"		}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> variableNamesX = collectSimpleNames(defaultFixture.getTypeDeclaration(), "x");

		SimpleName declarationFragmentName = variableNamesX.get(0);
		assertEquals(VariableDeclarationFragment.NAME_PROPERTY, declarationFragmentName.getLocationInParent());

		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) declarationFragmentName
			.getParent();

		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(declarationFragment, CompilationUnit.class);

		ReferenceToLocalVariableAnalyzer referenceAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit,
				declarationFragment);

		SimpleName declarationFragmentNameWithSameIdentifier = variableNamesX.get(1);
		assertEquals(VariableDeclarationFragment.NAME_PROPERTY, declarationFragmentNameWithSameIdentifier.getLocationInParent());

		assertFalse(referenceAnalyzer.isReference(declarationFragmentNameWithSameIdentifier));
	}

	@Test
	void visit_SimpleNameSameAsDeclarationFragmentName_shouldNotBeReference()
			throws Exception {

		String code = "" +
				"	void simpleNameSameAsDeclarationFragmentName() {\n" +
				"		int x = 1;\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> variableNamesX = collectSimpleNames(defaultFixture.getTypeDeclaration(), "x");

		SimpleName declarationFragmentName = variableNamesX.get(0);
		assertEquals(VariableDeclarationFragment.NAME_PROPERTY, declarationFragmentName.getLocationInParent());

		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) declarationFragmentName
			.getParent();

		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(declarationFragment, CompilationUnit.class);

		ReferenceToLocalVariableAnalyzer referenceAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit,
				declarationFragment);

		assertFalse(referenceAnalyzer.isReference(declarationFragmentName));
	}

	@Test
	void visit_usingVariableWithSameNameInRunnable_shouldNotBeReference()
			throws Exception {

		String code = "" +
				"	void usingVariableWithSameNameInRunnable() {\n" +
				"\n" +
				"		int x = 1;\n" +
				"		Runnable r = new Runnable() {\n" +
				"			@Override\n" +
				"			public void run() {\n" +
				"				int x = 1;\n" +
				"				int y = x;\n" +
				"			}\n" +
				"		};\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> variableNamesX = collectSimpleNames(defaultFixture.getTypeDeclaration(), "x");

		SimpleName firstX = variableNamesX.get(0);
		assertEquals(VariableDeclarationFragment.NAME_PROPERTY, firstX.getLocationInParent());

		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) firstX
			.getParent();

		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(declarationFragment, CompilationUnit.class);

		ReferenceToLocalVariableAnalyzer referenceAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit,
				declarationFragment);

		SimpleName thirdX = variableNamesX.get(2);

		assertFalse(referenceAnalyzer.isReference(thirdX));
	}

	@Test
	void visit_returnThisX_shouldNotBeReference() throws Exception {
		String code = "" +
				"	int x;\n" +
				"	int delareLocalXReturnThisX() {\n" +
				"		int x = 1;\n" +
				"		return this.x;\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> variableNamesX = collectSimpleNames(defaultFixture.getTypeDeclaration(), "x");

		SimpleName secondX = variableNamesX.get(1);
		assertEquals(VariableDeclarationFragment.NAME_PROPERTY, secondX.getLocationInParent());

		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) secondX
			.getParent();

		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(declarationFragment, CompilationUnit.class);

		ReferenceToLocalVariableAnalyzer referenceAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit,
				declarationFragment);

		SimpleName thirdX = variableNamesX.get(2);

		assertFalse(referenceAnalyzer.isReference(thirdX));
	}

	@Test
	void visit_parameterXOfLocalClass_shouldNotBeReference() throws Exception {
		String code = "" +
				"	void parameterXOfLocalClass() {\n" +
				"		int x = 1;\n" +
				"		class LocalClass {\n" +
				"			void methodWithParameterX(int x) {\n" +
				"			}\n" +
				"		}\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> variableNamesX = collectSimpleNames(defaultFixture.getTypeDeclaration(), "x");

		SimpleName xDeclaredAsLocalVariable = variableNamesX.get(0);
		assertEquals(VariableDeclarationFragment.NAME_PROPERTY, xDeclaredAsLocalVariable.getLocationInParent());

		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) xDeclaredAsLocalVariable
			.getParent();

		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(declarationFragment, CompilationUnit.class);

		ReferenceToLocalVariableAnalyzer referenceAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit,
				declarationFragment);

		SimpleName xDeclaredAsParameter = variableNamesX.get(1);

		assertFalse(referenceAnalyzer.isReference(xDeclaredAsParameter));
	}

	@Test
	void visit_enumConstantXInLocalClass_shouldNotBeReference() throws Exception {
		String code = "" +
				"	void parameterXOfLocalClass() {\n" +
				"		int x = 1;\n" +
				"		class LocalClass {\n" +
				"			enum X {\n" +
				"				x;\n" +
				"			}\n" +
				"		}\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> variableNamesX = collectSimpleNames(defaultFixture.getTypeDeclaration(), "x");

		SimpleName xDeclaredAsLocalVariable = variableNamesX.get(0);
		assertEquals(VariableDeclarationFragment.NAME_PROPERTY, xDeclaredAsLocalVariable.getLocationInParent());

		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) xDeclaredAsLocalVariable
			.getParent();

		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(declarationFragment, CompilationUnit.class);

		ReferenceToLocalVariableAnalyzer referenceAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit,
				declarationFragment);

		SimpleName xDeclaredAsEnumConstant = variableNamesX.get(1);

		assertFalse(referenceAnalyzer.isReference(xDeclaredAsEnumConstant));
	}

	@Test
	void visit_returnSuperX_shouldNotBeReference() throws Exception {
		String code = "" +
				"	class SuperClass {\n" +
				"		int x;\n" +
				"	}\n" +
				"\n" +
				"	class SubClass extends SuperClass {\n" +
				"		int returnSuperX() {\n" +
				"			int x = 1;\n" +
				"			return super.x;\n" +
				"		}\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> variableNamesX = collectSimpleNames(defaultFixture.getTypeDeclaration(), "x");

		SimpleName xDeclaredAsLocalVariable = variableNamesX.get(1);
		assertEquals(VariableDeclarationFragment.NAME_PROPERTY, xDeclaredAsLocalVariable.getLocationInParent());

		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) xDeclaredAsLocalVariable
			.getParent();

		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(declarationFragment, CompilationUnit.class);

		ReferenceToLocalVariableAnalyzer referenceAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit,
				declarationFragment);

		SimpleName superX = variableNamesX.get(2);

		assertFalse(referenceAnalyzer.isReference(superX));
	}

	@Test
	void visit_IntegerMaxValue_shouldNotBeReferenceToLocalMaxValue() throws Exception {
		String code = "" +
				"	int returnIntegerMaxValue() {\n" +
				"		int MAX_VALUE = 1000;\n" +
				"		return Integer.MAX_VALUE;\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> variableNamesX = collectSimpleNames(defaultFixture.getTypeDeclaration(), "MAX_VALUE");

		SimpleName maxValueDeclaredAsLocalVariable = variableNamesX.get(0);
		assertEquals(VariableDeclarationFragment.NAME_PROPERTY, maxValueDeclaredAsLocalVariable.getLocationInParent());

		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) maxValueDeclaredAsLocalVariable
			.getParent();

		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(declarationFragment, CompilationUnit.class);

		ReferenceToLocalVariableAnalyzer referenceAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit,
				declarationFragment);

		SimpleName integerMaxValue = variableNamesX.get(1);

		assertFalse(referenceAnalyzer.isReference(integerMaxValue));
	}

	@Test
	void visit_returnXAfterDeclaringY_shouldNotBeReference() throws Exception {
		String code = "" +
				"	int returnXAfterDeclaringY() {\n" +
				"		int x = 1;\n" +
				"		int y = 2;\n" +
				"		return x;\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> variableNamesX = collectSimpleNames(defaultFixture.getTypeDeclaration(), "x");
		List<SimpleName> variableNamesY = collectSimpleNames(defaultFixture.getTypeDeclaration(), "y");

		SimpleName yDeclaredAsLocalVariable = variableNamesY.get(0);
		assertEquals(VariableDeclarationFragment.NAME_PROPERTY, yDeclaredAsLocalVariable.getLocationInParent());

		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) yDeclaredAsLocalVariable
			.getParent();

		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(declarationFragment, CompilationUnit.class);

		ReferenceToLocalVariableAnalyzer referenceAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit,
				declarationFragment);

		SimpleName xUsedInReturnStatement = variableNamesX.get(1);

		assertFalse(referenceAnalyzer.isReference(xUsedInReturnStatement));
	}

	@Test
	void visit_lableWithSameNameAsVariable_shouldNotBeReference() throws Exception {
		String code = "" +
				"		void lableWithSameNameAsVariable() {\n" +
				"			int x = 0;\n" +
				"			x: if (true) {\n" +
				"\n" +
				"			}\n" +
				"		}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> variableNamesX = collectSimpleNames(defaultFixture.getTypeDeclaration(), "x");

		SimpleName xDeclaredAsLocalVariable = variableNamesX.get(0);
		assertEquals(VariableDeclarationFragment.NAME_PROPERTY, xDeclaredAsLocalVariable.getLocationInParent());

		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) xDeclaredAsLocalVariable
			.getParent();

		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(declarationFragment, CompilationUnit.class);

		ReferenceToLocalVariableAnalyzer referenceAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit,
				declarationFragment);

		SimpleName xAsLabelName = variableNamesX.get(1);

		assertFalse(referenceAnalyzer.isReference(xAsLabelName));
	}

	@Test
	void visit_methodInvocationWithSameNameAsVariable_shouldNotBeReference() throws Exception {
		String code = "" +
				"	int methodInvocationWithSameNameAsVariable() {\n" +
				"		int x = 0;\n" +
				"		return x();\n" +
				"	}\n" +
				"\n" +
				"	int x() {\n" +
				"		return 1;\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> variableNamesX = collectSimpleNames(defaultFixture.getTypeDeclaration(), "x");

		SimpleName xDeclaredAsLocalVariable = variableNamesX.get(0);
		assertEquals(VariableDeclarationFragment.NAME_PROPERTY, xDeclaredAsLocalVariable.getLocationInParent());

		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) xDeclaredAsLocalVariable
			.getParent();

		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(declarationFragment, CompilationUnit.class);

		ReferenceToLocalVariableAnalyzer referenceAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit,
				declarationFragment);

		SimpleName xAsMethodInvocation = variableNamesX.get(1);

		assertFalse(referenceAnalyzer.isReference(xAsMethodInvocation));
	}

	@Test
	void visit_referencingParameterX_shouldNotBeReferenceOfLocalVariableX() throws Exception {
		String code = "" +
				"	void referencingParameterX() {\n" +
				"		int x = 1;\n" +
				"		class LocalClass {\n" +
				"			int methodWithParameterX(int x) {\n" +
				"				return x;\n" +
				"			}\n" +
				"		}\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> variableNamesX = collectSimpleNames(defaultFixture.getTypeDeclaration(), "x");

		SimpleName xDeclaredAsLocalVariable = variableNamesX.get(0);
		assertEquals(VariableDeclarationFragment.NAME_PROPERTY, xDeclaredAsLocalVariable.getLocationInParent());

		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) xDeclaredAsLocalVariable
			.getParent();

		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(declarationFragment, CompilationUnit.class);

		ReferenceToLocalVariableAnalyzer referenceAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit,
				declarationFragment);

		SimpleName xAsParameterReference = variableNamesX.get(2);

		assertFalse(referenceAnalyzer.isReference(xAsParameterReference));
	}

	@Test
	void visit_referencingFieldX_shouldNotBeReferenceOfLocalVariableX() throws Exception {
		String code = "" +
				"	void referencingParameterX() {\n" +
				"		int x = 1;\n" +
				"		class LocalClass {\n" +
				"			int x = 1;\n" +
				"			int methodReturningFieldX() {\n" +
				"				return x;\n" +
				"			}\n" +
				"		}\n" +
				"	}";

		defaultFixture.addTypeDeclarationFromString(DEFAULT_TYPE_DECLARATION_NAME, code);
		List<SimpleName> variableNamesX = collectSimpleNames(defaultFixture.getTypeDeclaration(), "x");

		SimpleName xDeclaredAsLocalVariable = variableNamesX.get(0);
		assertEquals(VariableDeclarationFragment.NAME_PROPERTY, xDeclaredAsLocalVariable.getLocationInParent());

		VariableDeclarationFragment declarationFragment = (VariableDeclarationFragment) xDeclaredAsLocalVariable
			.getParent();

		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(declarationFragment, CompilationUnit.class);

		ReferenceToLocalVariableAnalyzer referenceAnalyzer = new ReferenceToLocalVariableAnalyzer(compilationUnit,
				declarationFragment);

		SimpleName xAsFieldReference = variableNamesX.get(2);

		assertFalse(referenceAnalyzer.isReference(xAsFieldReference));
	}
}
