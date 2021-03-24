package eu.jsparrow.core.visitor.junit.jupiter;

import static eu.jsparrow.core.visitor.junit.jupiter.RegexJUnitQualifiedName.isJUnitJupiterName;
import static eu.jsparrow.core.visitor.junit.jupiter.RegexJUnitQualifiedName.isJUnitName;
import static eu.jsparrow.core.visitor.junit.jupiter.common.CommonJUnit4Analysis.JUNIT4_TO_JUPITER_TEST_ANNOTATIONS_MAP;

import java.util.List;

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

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Helper visitor analyzing a compilation unit to decide if it is possible to
 * migrate JUnit-4-annotations to JUnit-Jupiter-annotations.
 *
 * @since 3.27.0
 */
class JUnit4AnnotationsAnalyzerVisitor extends ASTVisitor {

	private static final String PKG_ORG_JUNIT = "org.junit"; //$NON-NLS-1$
	private static final String TYPE_ORG_JUNIT_IGNORE = "org.junit.Ignore"; //$NON-NLS-1$
	private boolean transformationPossible = true;

	@Override
	public boolean preVisit2(ASTNode node) {
		return transformationPossible;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		String packageName = node.resolveBinding()
			.getName();
		transformationPossible = !isJUnitName(packageName);
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
			String packageName = ((IPackageBinding) binding).getName();
			if (node.isOnDemand() && packageName.equals(PKG_ORG_JUNIT)) {
				return true;
			}
			if (isJUnitName(packageName)) {
				return isJUnitJupiterName(packageName);
			}
			return true;
		}
		if (binding.getKind() == IBinding.TYPE) {
			if (isSupportedJUnit4AnnotationType((ITypeBinding) binding)) {
				return true;
			}
		}
		return isSupportedBinding(binding);
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
		return isSupportedBinding(binding);
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
		return JUNIT4_TO_JUPITER_TEST_ANNOTATIONS_MAP.containsKey(qualifiedTypeName);
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

	private boolean isSupportedBinding(IBinding binding) {// isSupportedBinding
		if (binding.getKind() == IBinding.PACKAGE) {
			/*
			 * assumed that this part of code will never be covered because all
			 * cases with a package binding are handled on the level of the
			 * package declaration and the imports and types
			 */
			return false;
		}
		if (binding.getKind() == IBinding.TYPE) {
			return isSupportedTypeBinding((ITypeBinding) binding);

		}
		if (binding.getKind() == IBinding.METHOD) {
			IMethodBinding methodBinding = (IMethodBinding) binding;
			if (!isSupportedTypeBinding(methodBinding.getDeclaringClass())) {
				return false;
			}
			ITypeBinding returnType = methodBinding.getReturnType();
			return !isUnsupportedType(returnType);
		}
		if (binding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;
			if (!isSupportedTypeBinding(variableBinding.getType())) {
				return false;
			}
			ITypeBinding declaringClass = variableBinding.getDeclaringClass();
			if (declaringClass == null) {
				return true;
			}
			return isSupportedTypeBinding(declaringClass);
		}
		if (binding.getKind() == IBinding.ANNOTATION) {
			/*
			 * assumed that this part of code will never be covered because only
			 * an annotation can can have an annotation binding.
			 */
			return false;
		}
		if (binding.getKind() == IBinding.MEMBER_VALUE_PAIR) {
			/*
			 * assumed that this part of code will never be covered because only
			 * a member-value-pair can have a member-value-pair binding.
			 */
			return false;
		}
		if (binding.getKind() == IBinding.MODULE) {
			return false;
		}
		return false;
	}

	private boolean isSupportedTypeBinding(ITypeBinding typeBinding) {
		boolean isUnsupportedType = this.isUnsupportedType(typeBinding);
		if (isUnsupportedType) {
			return false;
		}
		boolean hasUnsupportedSuperType = this.hasUnsupportedSuperType(typeBinding);
		if (hasUnsupportedSuperType) {
			return false;
		}

		boolean hasUnsupportedTypeArg = this.hasUnsupportedTypeArgument(typeBinding);
		if (hasUnsupportedTypeArg) {
			return false;
		}

		return true;
	}

	private boolean isUnsupportedType(ITypeBinding typeBinding) {

		if (typeBinding.isPrimitive()) {
			return false;
		}
		String qualifiedTypeName = typeBinding.getQualifiedName();
		if (qualifiedTypeName.equals("org.junit.Assert")) { //$NON-NLS-1$
			return false;
		}
		if (isJUnitName(qualifiedTypeName)) {
			return !isJUnitJupiterName(qualifiedTypeName);
		}
		return false;
	}

	private boolean hasUnsupportedSuperType(ITypeBinding typeBinding) {

		List<ITypeBinding> ancestors = ClassRelationUtil.findAncestors(typeBinding);
		for (ITypeBinding ancestor : ancestors) {
			if (isUnsupportedType(ancestor)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasUnsupportedTypeArgument(ITypeBinding typeBinding) {
		if (!typeBinding.isParameterizedType()) {
			return false;

		}
		ITypeBinding[] typeParameters = typeBinding.getTypeArguments();
		for (ITypeBinding parameterType : typeParameters) {
			if (isUnsupportedType(parameterType)) {
				return true;
			}
			if (hasUnsupportedSuperType(parameterType)) {
				return true;
			}

			if (parameterType.isParameterizedType()) {
				boolean hasUnsupportedTypeArg = hasUnsupportedTypeArgument(parameterType);
				if (hasUnsupportedTypeArg) {
					return true;
				}
			}
		}

		return false;
	}

	boolean isTransformationPossible() {
		return transformationPossible;
	}
}