package eu.jsparrow.core.visitor.junit.junit3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class JUnit3TestCaseAnalyzerVisitor extends ASTVisitor {

	private static final String JUNIT_FRAMEWORK_TEST_CASE = "junit.framework.TestCase"; //$NON-NLS-1$

	private boolean transformationPossible = true;

	private final List<TypeDeclaration> jUnit3TestCases = new ArrayList<>();
	private final List<SimpleType> jUnit3TestCaseSuperTypesToRemove = new ArrayList<>();

	@Override
	public boolean preVisit2(ASTNode node) {
		return transformationPossible;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		SimpleType jUnitFrameworkTestCaseAsSuperType = findJUnitFrameworkTestCaseAsSuperType(node).orElse(null);
		ITypeBinding typeBinding = node.resolveBinding();

		List<ITypeBinding> ancestors = ClassRelationUtil.findAncestors(typeBinding);
		if (jUnitFrameworkTestCaseAsSuperType != null) {
			transformationPossible = !node.isLocalTypeDeclaration() &&
					ancestors
						.stream()
						.filter(ancestor -> !isJUnitFrameworkTestCase(ancestor))
						.noneMatch(UnexpectedJunit3References::hasUnexpectedJUnitReference);
			if (transformationPossible) {
				jUnit3TestCases.add(node);
				jUnit3TestCaseSuperTypesToRemove.add(jUnitFrameworkTestCaseAsSuperType);
				return true;
			}
			return false;
		}

		transformationPossible = ancestors
			.stream()
			.noneMatch(UnexpectedJunit3References::hasUnexpectedJUnitReference);

		return transformationPossible;
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
}
