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
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class JUnit3ReferencesAnalyzerVisitor extends ASTVisitor {

	MethodDeclaration meinMethodToRemove;

	private boolean transformationPossible = true;

	JUnit3ReferencesAnalyzerVisitor(JUnit3DataCollectorVisitor junit3DataCollectorVisitor) {
		meinMethodToRemove = junit3DataCollectorVisitor.getMainMethodToRemove()
			.orElse(null);
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return transformationPossible;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		String packageName = node.resolveBinding()
			.getName();
		transformationPossible = !UnexpectedJunit3References.isUnexpectedJUnitQualifiedName(packageName);
		return false;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		boolean isMainMethodToRemove = meinMethodToRemove != null && meinMethodToRemove == node;
		return !isMainMethodToRemove;
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
				|| name.getLocationInParent() == TypeDeclaration.NAME_PROPERTY
				|| name.getLocationInParent() == LabeledStatement.LABEL_PROPERTY
				|| name.getLocationInParent() == ContinueStatement.LABEL_PROPERTY
				|| name.getLocationInParent() == BreakStatement.LABEL_PROPERTY) {
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
			return !UnexpectedJunit3References.hasUnexpectedJUnitReference(typeBinding);
		}

		if (binding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;
			ITypeBinding variableTypeBinding = variableBinding.getVariableDeclaration()
				.getType();
			if (UnexpectedJunit3References.hasUnexpectedJUnitReference(variableTypeBinding)) {
				return false;
			}
			if (variableBinding.isField()) {
				return !UnexpectedJunit3References
					.hasUnexpectedJUnitReference(variableBinding.getDeclaringClass());
			}
		}
		return false;
	}
}