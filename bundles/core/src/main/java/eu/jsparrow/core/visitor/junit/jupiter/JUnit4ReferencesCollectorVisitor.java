package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

/**
 * Collects the following annotations: <br>
 * <ul>
 * <li>Annotations having the simple type names<br>
 * {@code Test}, {@code Ignore}, {@code Before}, {@code BeforeClass},
 * {@code After} and {@code AfterClass}</li>
 * <li>All other annotations which are JUnit-4-annotations.</li>
 * </ul>
 *
 */
class JUnit4ReferencesCollectorVisitor extends ASTVisitor {

	private static final String ORG_JUNIT_IGNORE = "org.junit.Ignore"; //$NON-NLS-1$

	private static final String PACKAGE_ORG_JUNIT = "org.junit"; //$NON-NLS-1$

	private static final Predicate<String> PREDICATE_J_UNIT_4_PACKAGE = RegexPredicateFactory
		.createjUnit4PackagePredicate();

	private static final Predicate<String> PREDICATE_J_UNIT_4_SUPPORTED_ANNOTATIONS = RegexPredicateFactory
		.createSupportedAnnotationPredicate();

	private boolean transformationPossible = true;

	@Override
	public boolean preVisit2(ASTNode node) {

		return transformationPossible && super.preVisit2(node);
	}

	@Override
	public boolean visit(QualifiedName node) {
		transformationPossible = analyzeQualifiedName(node);
		return false;
	}

	@Override
	public boolean visit(SimpleName node) {
		transformationPossible = analyzeSimpleName(node);
		return false;
	}

	private boolean analyzeSimpleName(SimpleName node) {
		if (isNameOfSupportedAnnotation(node)) {
			return true;
		}
		if (isIgnoreAnnotationValueName(node)) {
			return true;
		}
		return !isUnexpectedReferenceToJUnit(node.resolveBinding());
	}

	private boolean analyzeQualifiedName(QualifiedName node) {

		if (isNameOfSupportedAnnotation(node)) {
			return true;
		}
		if (isNameOfSupportedAnnotationImport(node)) {
			return true;
		}
		return !isUnexpectedReferenceToJUnit(node.resolveBinding());
	}

	private boolean isNameOfSupportedAnnotation(Name name) {
		
		IBinding binding = name.resolveBinding();
		if (binding.getKind() != IBinding.TYPE) {
			return false;
		}

		ITypeBinding typeBinding = (ITypeBinding) binding;
		if (PREDICATE_J_UNIT_4_SUPPORTED_ANNOTATIONS.test(typeBinding.getQualifiedName())) {

			if (name.getLocationInParent() == MarkerAnnotation.TYPE_NAME_PROPERTY) {
				return true;
			}

			if (name.getLocationInParent() == NormalAnnotation.TYPE_NAME_PROPERTY) {
				NormalAnnotation normalAnnotation = (NormalAnnotation) name.getParent();
				if (normalAnnotation.values()
					.isEmpty()) {
					return true;
				}
			}

			if (typeBinding.getQualifiedName()
				.equals(ORG_JUNIT_IGNORE)) {
				return true;
			}
		}
		return false;
	}

	private boolean isNameOfSupportedAnnotationImport(Name name) {
		if (name.getLocationInParent() == ImportDeclaration.NAME_PROPERTY) {
			IBinding binding = name.resolveBinding();
			if (binding.getKind() == IBinding.PACKAGE) {
				IPackageBinding packageBinding = (IPackageBinding) binding;
				return packageBinding.getName()
					.equals(PACKAGE_ORG_JUNIT);
			}
			if (binding.getKind() == IBinding.TYPE) {
				ITypeBinding typeBinding = (ITypeBinding) binding;
				return PREDICATE_J_UNIT_4_SUPPORTED_ANNOTATIONS.test(typeBinding.getQualifiedName());
			}

		}
		return false;
	}

	private boolean isIgnoreAnnotationValueName(SimpleName simpleName) {
		if (simpleName.getLocationInParent() != MemberValuePair.NAME_PROPERTY) {
			return false;
		}
		MemberValuePair memberValuePair = (MemberValuePair) simpleName.getParent();

		if (memberValuePair.getLocationInParent() != NormalAnnotation.VALUES_PROPERTY) {
			return false;
		}
		NormalAnnotation annotation = (NormalAnnotation) memberValuePair.getParent();
		return annotation.resolveTypeBinding()
			.getQualifiedName()
			.equals(ORG_JUNIT_IGNORE);
	}

	private boolean isUnexpectedReferenceToJUnit(IBinding binding) {

		if (binding.getKind() == IBinding.PACKAGE) {
			return isJUnit4Package((IPackageBinding) binding);

		}
		if (binding.getKind() == IBinding.TYPE) {
			return isJUnit4Type((ITypeBinding) binding);

		}
		if (binding.getKind() == IBinding.METHOD) {
			IMethodBinding methodBinding = (IMethodBinding) binding;
			return isJUnit4Type(methodBinding.getDeclaringClass());

		}

		if (binding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;
			if (isJUnit4Type(variableBinding.getType())) {
				return true;
			}
			return isJUnit4Type(variableBinding.getDeclaringClass());
		}

		if (binding.getKind() == IBinding.ANNOTATION) {
			IAnnotationBinding annotationBinding = (IAnnotationBinding) binding;
			return isJUnit4Type(annotationBinding.getAnnotationType());

		}

		return false;
	}

	private boolean isJUnit4Package(IPackageBinding packageBinding) {
		if (packageBinding != null) {
			return PREDICATE_J_UNIT_4_PACKAGE.test(packageBinding.getName());
		}
		return false;
	}

	private boolean isJUnit4Type(ITypeBinding typeBinding) {
		if (typeBinding != null) {
			return isJUnit4Package(typeBinding.getPackage());
		}
		return false;
	}

	boolean isTransformationPossible() {
		return transformationPossible;
	}

}
