package eu.jsparrow.core.visitor.junit.jupiter;

import static eu.jsparrow.core.visitor.junit.jupiter.MigrateJUnit4ToJupiterASTVisitor.ANNOTATION_QUALIFIED_NAMES_REPLACEMENT_MAP;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
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
import org.eclipse.jdt.core.dom.PackageDeclaration;
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

	private static final String TYPE_ORG_JUNIT_IGNORE = "org.junit.Ignore"; //$NON-NLS-1$

	private boolean transformationPossible = true;

	@Override
	public boolean preVisit2(ASTNode node) {
		return transformationPossible;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		transformationPossible = analyzePackageBinding(node.resolveBinding());
		return false;
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
			return analyzePackageBinding((IPackageBinding) binding);
		}

		if (binding.getKind() == IBinding.TYPE) {
			ITypeBinding typeBinding = (ITypeBinding) binding;
			if (isSupportedJUnit4AnnotationType(typeBinding)) {
				return true;
			}
		}
		return checkOtherReference(binding);
	}

	private boolean analyzePackageBinding(IPackageBinding packageBinding) {
		String packageName = packageBinding.getName();
		// Not supported
		if (packageName.equals("junit")) { //$NON-NLS-1$
			return false;
		}
		// Not supported
		if (packageName.startsWith("junit.")) { //$NON-NLS-1$
			return false;
		}
		// supported
		if (packageName.equals("org.junit")) { //$NON-NLS-1$
			return true;
		}
		// inside "org.junit."
		// Not supported except for "org.junit.jupiter.api" or any package
		// inside org.junit.jupiter.api
		if (packageName.startsWith("org.junit.")) { //$NON-NLS-1$
			if (packageName.equals("org.junit.jupiter.api")) { //$NON-NLS-1$
				return true;
			}
			if (packageName.startsWith("org.junit.jupiter.api.")) { //$NON-NLS-1$
				return true;
			}
			return false;
		}
		// outside "org.junit." and therefore supported
		return true;
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
		if (binding.getKind() == IBinding.PACKAGE) {
			// assumed that this part of code will never be covered because all
			// cases with a package binding are handled on the level of the
			// package declaration and the imports.
			return false;
		}
		if (binding.getKind() == IBinding.TYPE) {
			return checkTypeBinding((ITypeBinding) binding);

		}
		if (binding.getKind() == IBinding.METHOD) {
			IMethodBinding methodBinding = (IMethodBinding) binding;
			return checkTypeBinding(methodBinding.getDeclaringClass());

		}
		if (binding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;
			if (!checkTypeBinding(variableBinding.getType())) {
				return false;
			}
			return checkTypeBinding(variableBinding.getDeclaringClass());
		}
		if (binding.getKind() == IBinding.ANNOTATION) {
			/**
			 * assumed that this part of code will never be covered because only
			 * an annotation can can have an annotation binding.
			 */
			return false;
		}
		if (binding.getKind() == IBinding.MEMBER_VALUE_PAIR) {
			/**
			 * assumed that this part of code will never be covered because only
			 * a member-value-pair can have a member-value-pair binding.
			 */
			return false;
		}
		if (binding.getKind() == IBinding.MODULE) {
			/**
			 * Not clear what value to return... seams to be used only in Java 9
			 */
			return true;
		}
		return false;
	}

	private boolean checkTypeBinding(ITypeBinding typeBinding) {
		if (typeBinding != null) {
			String qualifiedTypeName = typeBinding.getQualifiedName();
			// Not supported as soon as inside "junit."
			if (qualifiedTypeName.startsWith("junit.")) { //$NON-NLS-1$
				return false;
			}
			if (qualifiedTypeName.startsWith("org.junit.")) { //$NON-NLS-1$
				// supported because "org.junit.Assert" although inside
				// "org.junit."
				if (qualifiedTypeName.equals("org.junit.Assert")) { //$NON-NLS-1$
					return true;
				}
				// supported because JUnit Jupiter
				if (qualifiedTypeName.startsWith("org.junit.jupiter.api.")) { //$NON-NLS-1$
					return true;
				}
				// Not supported because other type inside "org.junit."
				return false;
			}
		}
		// OK:
		// type not within "junit."
		// type not within "org.junit."
		return true;

	}

	boolean isTransformationPossible() {
		return transformationPossible;
	}
}
