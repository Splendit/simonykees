package eu.jsparrow.core.visitor.junit.junit3;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class JUnit3TestCaseDeclarationsAnalyzer {

	private static final String JUNIT_FRAMEWORK_TEST_CASE = "junit.framework.TestCase"; //$NON-NLS-1$

	private final List<TypeDeclaration> jUnit3TestCaseDeclarations = new ArrayList<>();
	private final List<SimpleType> jUnit3TestCaseSuperTypesToRemove = new ArrayList<>();

	boolean collectTestCaseDeclarationAnalysisData(JUnit3DataCollectorVisitor junit3DataCollectorVisitor) {
		List<TypeDeclaration> typeDeclarationsToAnalyze = junit3DataCollectorVisitor.getTypeDeclarationsToAnalyze();
		for (TypeDeclaration typeDeclaration : typeDeclarationsToAnalyze) {
			if (isWellFormedTestCaseTypeDeclaration(typeDeclaration)) {
				jUnit3TestCaseDeclarations.add(typeDeclaration);
			} else if (UnexpectedJunit3References.hasUnexpectedJUnitReference(typeDeclaration.resolveBinding())) {
				return false;
			}
		}

		List<SimpleType> simpleTypesToAnalyze = junit3DataCollectorVisitor.getSimpleTypesToAnalyze();
		for (SimpleType simpleType : simpleTypesToAnalyze) {
			if (jUnit3TestCaseDeclarations.contains(simpleType.getParent())) {
				jUnit3TestCaseSuperTypesToRemove.add(simpleType);
			} else if (UnexpectedJunit3References.hasUnexpectedJUnitReference(simpleType.resolveBinding())) {
				return false;
			}
		}
		return true;
	}

	private static boolean isWellFormedTestCaseTypeDeclaration(TypeDeclaration typeDeclaration) {
		if (typeDeclaration.isLocalTypeDeclaration()) {
			return false;
		}
		Type superclassType = typeDeclaration.getSuperclassType();
		if (superclassType == null) {
			return false;
		}
		if (superclassType.getNodeType() != ASTNode.SIMPLE_TYPE) {
			return false;
		}
		String superClassQualifiedName = superclassType.resolveBinding()
			.getQualifiedName();

		if (!superClassQualifiedName.equals(JUNIT_FRAMEWORK_TEST_CASE)) {
			return false;
		}

		ITypeBinding[] interfacesToAnalyze = typeDeclaration.resolveBinding()
			.getInterfaces();
		for (ITypeBinding implementedInterface : interfacesToAnalyze) {
			if (UnexpectedJunit3References.hasUnexpectedJUnitReference(implementedInterface)) {
				return false;
			}
		}

		return true;
	}

	public List<TypeDeclaration> getJUnit3TestCaseDeclarations() {
		return jUnit3TestCaseDeclarations;
	}

	public List<SimpleType> getJUnit3TestCaseSuperTypesToRemove() {
		return jUnit3TestCaseSuperTypesToRemove;
	}
}
