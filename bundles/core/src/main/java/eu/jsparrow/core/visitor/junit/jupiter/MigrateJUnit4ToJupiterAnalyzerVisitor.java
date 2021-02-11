package eu.jsparrow.core.visitor.junit.jupiter;

import static eu.jsparrow.core.visitor.junit.jupiter.MigrateJUnit4ToJupiterASTVisitor.ANNOTATION_QUALIFIED_NAMES_REPLACEMENT_MAP;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LabeledStatement;
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
class MigrateJUnit4ToJupiterAnalyzerVisitor extends ASTVisitor {

	private static final String TYPE_ORG_JUNIT_ASSERT = "org.junit.Assert"; //$NON-NLS-1$
	private static final String TYPE_ORG_JUNIT_IGNORE = "org.junit.Ignore"; //$NON-NLS-1$
	private static final String PACKAGE_ORG_JUNIT = "org.junit"; //$NON-NLS-1$

	private static final Predicate<String> PREDICATE_J_UNIT_4_PACKAGE = createjUnit4PackagePredicate();
	private boolean transformationPossible = true;

	private static Predicate<String> createjUnit4PackagePredicate() {
		final String regexOrgJunitChildPackages = "experimental|function|internal|matchers|rules|runner|runners|validator"; //$NON-NLS-1$
		final String regexOrgJUnit = "org\\.junit(\\.(" + regexOrgJunitChildPackages + ")(\\..+)?)?$"; //$NON-NLS-1$ //$NON-NLS-2$
		final String regexJUnit = "junit\\.(extensions|framework|runner|textui)$"; //$NON-NLS-1$
		Pattern pattern = Pattern.compile("^(" + regexJUnit + ")|(" + regexOrgJUnit + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		return pattern.asPredicate();
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return transformationPossible;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		transformationPossible = analyzeImport(node);
		return false;
	}

	@Override
	public boolean visit(QualifiedName node) {
		transformationPossible = analyzeName(node);
		return false;
	}

	@Override
	public boolean visit(SimpleName node) {
		transformationPossible = analyzeName(node);
		return false;
	}

	private boolean analyzeImport(ImportDeclaration node) {
		IBinding binding = node.resolveBinding();
		if (binding.getKind() == IBinding.PACKAGE) {
			String packageName = ((IPackageBinding) binding).getName();
			return packageName.equals(PACKAGE_ORG_JUNIT);
		}
		if (binding.getKind() == IBinding.TYPE) {
			ITypeBinding typeBinding = (ITypeBinding) binding;
			if (isSupportedJUnit4AnnotationType(typeBinding)) {
				return true;
			}
		}
		return checkOtherReference(binding);
	}

	private boolean analyzeName(Name node) {
		if (node.getLocationInParent() == LabeledStatement.LABEL_PROPERTY
				|| node.getLocationInParent() == ContinueStatement.LABEL_PROPERTY
				|| node.getLocationInParent() == BreakStatement.LABEL_PROPERTY

		) {
			return true;
		}
		IBinding binding = node.resolveBinding();
		if (binding == null) {
			return false;
		}
		if (isNameOfSupportedAnnotation(node, binding)) {
			return true;
		}
		if (isIgnoreAnnotationValueName(node)) {
			return true;
		}
		return checkOtherReference(binding);
	}

	private boolean isNameReferencingJUnit4Assert(IBinding binding) {
		if (binding.getKind() == IBinding.METHOD) {
			if (isJUnit4AssertClass(((IMethodBinding) binding).getDeclaringClass())) {
				return true;
			}
		}
		if (binding.getKind() == IBinding.TYPE) {
			if (isJUnit4AssertClass((ITypeBinding) binding)) {
				return true;
			}
		}
		if (binding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;
			if (isJUnit4AssertClass(variableBinding.getType())) {
				return true;
			}
			if (isJUnit4AssertClass(variableBinding.getDeclaringClass())) {
				return true;
			}
		}
		return false;
	}

	private boolean isNameOfSupportedAnnotation(Name name, IBinding binding) {
		if (binding.getKind() != IBinding.TYPE) {
			return false;
		}

		ITypeBinding typeBinding = (ITypeBinding) binding;
		if (isSupportedJUnit4AnnotationType(typeBinding)) {
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
				.equals(TYPE_ORG_JUNIT_IGNORE)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSupportedJUnit4AnnotationType(ITypeBinding typeBinding) {
		String qualifiedTypeName = typeBinding.getQualifiedName();
		return ANNOTATION_QUALIFIED_NAMES_REPLACEMENT_MAP.containsKey(qualifiedTypeName);
	}

	private boolean isIgnoreAnnotationValueName(Name name) {
		if (name.getLocationInParent() != MemberValuePair.NAME_PROPERTY) {
			return false;
		}
		MemberValuePair memberValuePair = (MemberValuePair) name.getParent();

		if (memberValuePair.getLocationInParent() != NormalAnnotation.VALUES_PROPERTY) {
			return false;
		}
		NormalAnnotation annotation = (NormalAnnotation) memberValuePair.getParent();
		return annotation.resolveTypeBinding()
			.getQualifiedName()
			.equals(TYPE_ORG_JUNIT_IGNORE);
	}

	private boolean checkOtherReference(IBinding binding) {
		if (isNameReferencingJUnit4Assert(binding)) {
			return true;
		}
		if (binding.getKind() == IBinding.PACKAGE) {
			return !isJUnit4Package((IPackageBinding) binding);

		}
		if (binding.getKind() == IBinding.TYPE) {
			return !isJUnit4Type((ITypeBinding) binding);

		}
		if (binding.getKind() == IBinding.METHOD) {
			IMethodBinding methodBinding = (IMethodBinding) binding;
			return !isJUnit4Type(methodBinding.getDeclaringClass());

		}
		if (binding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;
			if (isJUnit4Type(variableBinding.getType())) {
				return false;
			}
			return !isJUnit4Type(variableBinding.getDeclaringClass());
		}
		if (binding.getKind() == IBinding.ANNOTATION) {
			IAnnotationBinding annotationBinding = (IAnnotationBinding) binding;
			return !isJUnit4Type(annotationBinding.getAnnotationType());
		}
		if (binding.getKind() == IBinding.MEMBER_VALUE_PAIR) {
			return true;
		}
		if (binding.getKind() == IBinding.MODULE) {
			return true;
		}
		return false;
	}

	private boolean isJUnit4AssertClass(ITypeBinding typeBinding) {
		if (typeBinding == null) {
			return false;
		}
		String qualifiedTypeName = typeBinding.getQualifiedName();
		return qualifiedTypeName.equals(TYPE_ORG_JUNIT_ASSERT);
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
