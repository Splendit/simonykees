package eu.jsparrow.core.visitor.junit.junit3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class JUnit3TestCaseAnalyzer {

	private static final String JUNIT_FRAMEWORK_TEST = "junit.framework.Test"; //$NON-NLS-1$
	private static final String JUNIT_FRAMEWORK_ASSERT = "junit.framework.Assert"; //$NON-NLS-1$
	private static final String JUNIT_FRAMEWORK_TEST_CASE = "junit.framework.TestCase"; //$NON-NLS-1$
	private final JUnit3DataCollectorVisitor junit3DataCollectorVisitor;
	private final List<TypeDeclaration> jUnit3TestCases = new ArrayList<>();
	private final List<SimpleType> jUnit3TestCaseSuperTypesToRemove = new ArrayList<>();

	JUnit3TestCaseAnalyzer(JUnit3DataCollectorVisitor junit3DataCollectorVisitor) {
		this.junit3DataCollectorVisitor = junit3DataCollectorVisitor;
	}

	boolean collectTestCaseDeclarationData() {
		List<TypeDeclaration> typeDeclarationsToAnalyze = junit3DataCollectorVisitor.getTypeDeclarationsToAnalyze();
		for (TypeDeclaration typeDeclaration : typeDeclarationsToAnalyze) {
			SimpleType jUnitFrameworkTestCaseAsSuperType = findJUnitFrameworkTestCaseAsSuperType(typeDeclaration)
				.orElse(null);
			if (jUnitFrameworkTestCaseAsSuperType != null) {
				if (analyzeTypeDeclarationExtendingTestCase(typeDeclaration)) {
					jUnit3TestCases.add(typeDeclaration);
					jUnit3TestCaseSuperTypesToRemove.add(jUnitFrameworkTestCaseAsSuperType);
				} else {
					return false;
				}
			} else {
				ITypeBinding typeBinding = typeDeclaration.resolveBinding();
				List<ITypeBinding> ancestorsToAnalyze = ClassRelationUtil.findAncestors(typeBinding);
				boolean unexpectedJUnitReference = ancestorsToAnalyze
					.stream()
					.anyMatch(UnexpectedJunit3References::hasUnexpectedJUnitReference);
				if (unexpectedJUnitReference) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean analyzeTypeDeclarationExtendingTestCase(TypeDeclaration typeDeclaration) {
		if (typeDeclaration.isLocalTypeDeclaration()) {
			return false;
		}
		ITypeBinding typeBinding = typeDeclaration.resolveBinding();

		List<ITypeBinding> ancestorsToAnalyze = ClassRelationUtil.findAncestors(typeBinding);
		for (ITypeBinding ancestor : ancestorsToAnalyze) {
			String qualifiedName = ancestor.getQualifiedName();
			if (!qualifiedName.equals(JUNIT_FRAMEWORK_TEST_CASE) &&
					!qualifiedName.equals(JUNIT_FRAMEWORK_ASSERT) &&
					!qualifiedName.equals(JUNIT_FRAMEWORK_TEST) &&
					UnexpectedJunit3References.hasUnexpectedJUnitReference(ancestor)) {
				return false;
			}
		}
		return true;
	}

	private Optional<SimpleType> findJUnitFrameworkTestCaseAsSuperType(TypeDeclaration node) {
		Type superclassType = node.getSuperclassType();
		if (superclassType != null && superclassType.getNodeType() == ASTNode.SIMPLE_TYPE) {
			SimpleType simpleSuperClassType = (SimpleType) superclassType;
			if (isJUnitFrameworkTestCase(simpleSuperClassType.resolveBinding())) {
				return Optional.of(simpleSuperClassType);
			}
		}
		return Optional.empty();
	}

	private boolean isJUnitFrameworkTestCase(ITypeBinding typeBinding) {
		String qualifiedName = typeBinding.getQualifiedName();
		return qualifiedName.equals(JUNIT_FRAMEWORK_TEST_CASE);
	}

	public List<TypeDeclaration> getJUnit3TestCases() {
		return jUnit3TestCases;
	}

	public List<SimpleType> getJUnit3TestCaseSuperTypesToRemove() {
		return jUnit3TestCaseSuperTypesToRemove;
	}
}