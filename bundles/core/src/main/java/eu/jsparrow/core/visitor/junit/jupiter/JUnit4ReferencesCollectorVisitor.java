package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
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
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

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

	private static final String PACKAGE_ORG_JUNIT = "org.junit"; //$NON-NLS-1$

	private static final Predicate<String> PREDICATE_J_UNIT_4_PACKAGE = RegexPredicateFactory
		.createjUnit4PackagePredicate();

	private static final Predicate<String> PREDICATE_J_UNIT_4_SUPPORTED_ANNOTATIONS = RegexPredicateFactory
		.createSupportedAnnotationPredicate();

	private final List<Annotation> supportedAnnotations = new ArrayList<>();

	private final List<ImportDeclaration> supportedAnnotationInports = new ArrayList<>();

	private final List<Name> unexpectedReferencesToJUnit = new ArrayList<>();

	@Override
	public boolean preVisit2(ASTNode node) {

		return super.preVisit2(node) && referencesOK();
	}

	@Override
	public boolean visit(QualifiedName node) {
		if (isNameOfSupportedAnnotation(node)) {
			supportedAnnotations.add((Annotation) node.getParent());
		} else if (isNameOfSupportedAnnotationImport(node)) {
			supportedAnnotationInports.add((ImportDeclaration) node.getParent());
		} else if (isUnexpectedReferenceToJUnit(node)) {
			unexpectedReferencesToJUnit.add(node);
		}
		return false;
	}

	@Override
	public boolean visit(SimpleName node) {
		if (isNameOfSupportedAnnotation(node)) {
			supportedAnnotations.add((Annotation) node.getParent());
		} else if (isUnexpectedReferenceToJUnit(node)) {
			unexpectedReferencesToJUnit.add(node);
		}
		return false;
	}

	private boolean isNameOfSupportedAnnotation(Name name) {

		if (name.getLocationInParent() == MarkerAnnotation.TYPE_NAME_PROPERTY
				|| name.getLocationInParent() == NormalAnnotation.TYPE_NAME_PROPERTY
				|| name.getLocationInParent() == SingleMemberAnnotation.TYPE_NAME_PROPERTY) {

			ITypeBinding typeBinding = name.resolveTypeBinding();
			return PREDICATE_J_UNIT_4_SUPPORTED_ANNOTATIONS.test(typeBinding.getQualifiedName());

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

	private boolean isUnexpectedReferenceToJUnit(Name name) {

		if (name.getLocationInParent() == MemberValuePair.NAME_PROPERTY &&
				name.getParent()
					.getLocationInParent() == NormalAnnotation.VALUES_PROPERTY) {
			NormalAnnotation annotation = (NormalAnnotation) name.getParent()
				.getParent();
			// Special case for annotations like
			// @Ignore(value = "This test is ignored")
			// where reference to JUNit 4 is not unexpected
			if (annotation.resolveTypeBinding()
				.getQualifiedName()
				.equals("org.junit.Ignore")) { //$NON-NLS-1$
				return false;
			}
		}

		IBinding binding = name.resolveBinding();
		IPackageBinding packageBinding = null;

		if (binding.getKind() == IBinding.PACKAGE) {
			packageBinding = (IPackageBinding) binding;

		}

		if (binding.getKind() == IBinding.TYPE) {
			ITypeBinding typeBinding = (ITypeBinding) binding;
			packageBinding = typeBinding.getPackage();
		}

		if (binding.getKind() == IBinding.METHOD) {
			IMethodBinding methodBinding = (IMethodBinding) binding;
			ITypeBinding typeBinding = methodBinding.getDeclaringClass();
			packageBinding = typeBinding.getPackage();
		}

		if (binding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;
			ITypeBinding typeBinding = variableBinding.getDeclaringClass();
			if (typeBinding != null) {
				packageBinding = typeBinding.getPackage();
			}
		}

		if (packageBinding != null) {
			return PREDICATE_J_UNIT_4_PACKAGE.test(packageBinding.getName());
		}
		return false;
	}

	List<Annotation> getSupportedAnnotations() {
		return supportedAnnotations;
	}

	List<ImportDeclaration> getSupportedAnnotationImports() {
		return supportedAnnotationInports;
	}

	boolean referencesOK() {
		return unexpectedReferencesToJUnit.isEmpty();
	}

}
