package eu.jsparrow.core.visitor.junit.junit3;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

public class JUnit3ReferencesAnalyzerVisitor extends ASTVisitor {
	private static final String JUNIT = "junit"; //$NON-NLS-1$
	private static final String JUNIT_PREFIX = "junit."; //$NON-NLS-1$
	private boolean transformationPossible = true;

	public static boolean isJUnit3QualifiedName(String declaringClassQualifiedName) {
		return declaringClassQualifiedName.startsWith(JUNIT_PREFIX);
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return transformationPossible;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		String packageName = node.resolveBinding()
			.getName();
		transformationPossible = !packageName.equals(JUNIT) &&
				!isJUnit3QualifiedName(packageName);
		return false;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
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

	private boolean analyzeName(Name name) {
		if (name.getLocationInParent() == MethodInvocation.NAME_PROPERTY
				|| name.getLocationInParent() == LabeledStatement.LABEL_PROPERTY
				|| name.getLocationInParent() == ContinueStatement.LABEL_PROPERTY
				|| name.getLocationInParent() == BreakStatement.LABEL_PROPERTY

		) {
			return true;
		}

		IBinding binding = name.resolveBinding();
		if (binding == null) {
			return false;
		}

		ITypeBinding typeBinding = null;
		if (binding.getKind() == IBinding.METHOD) {
			IMethodBinding methodBinding = (IMethodBinding) binding;
			typeBinding = methodBinding.getDeclaringClass();
		}

		if (binding.getKind() == IBinding.TYPE) {
			typeBinding = (ITypeBinding) binding;
		}

		if (binding.getKind() == IBinding.ANNOTATION) {
			IAnnotationBinding annotationBinding = (IAnnotationBinding) binding;
			typeBinding = annotationBinding.getAnnotationType();
		}

		if (binding.getKind() == IBinding.MEMBER_VALUE_PAIR) {
			IMemberValuePairBinding memberValuePairBinding = (IMemberValuePairBinding) binding;
			IMethodBinding methodBinding = memberValuePairBinding.getMethodBinding();
			typeBinding = methodBinding.getDeclaringClass();
		}

		if (typeBinding != null) {
			return !isJUnit3QualifiedName(typeBinding.getQualifiedName());
		}

		if (binding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;
			ITypeBinding variableTypeBinding = variableBinding.getVariableDeclaration().getType();
			if(isJUnit3QualifiedName(variableTypeBinding.getQualifiedName())) {
				return false;
			}
			if(variableBinding.isField()) {
				return !isJUnit3QualifiedName(variableBinding.getDeclaringClass().getQualifiedName());
			}
		}
		return false;
	}
}
