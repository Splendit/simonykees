package eu.jsparrow.rules.java16.javarecords;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class NonStaticReferencesVisitor extends ASTVisitor {

	private final String typeDeclarationQualifiedName;
	private boolean unsupportedReferenceExisting;

	public NonStaticReferencesVisitor(TypeDeclaration typeDeclaration) {
		this.typeDeclarationQualifiedName = typeDeclaration.resolveBinding()
			.getErasure()
			.getQualifiedName();
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		return !unsupportedReferenceExisting;
	}

	@Override
	public boolean visit(ThisExpression node) {
		if (node.getQualifier() != null) {
			unsupportedReferenceExisting = true;
		}
		return false;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		if (node.getQualifier() != null) {
			unsupportedReferenceExisting = true;
		}
		return false;
	}

	@Override
	public boolean visit(QualifiedName node) {
		unsupportedReferenceExisting = !analyzeNameBinding(node);
		return false;
	}

	@Override
	public boolean visit(SimpleName node) {
		unsupportedReferenceExisting = !analyzeSimpleNameLocationInParent(node) && !analyzeNameBinding(node);
		return false;
	}

	private boolean analyzeSimpleNameLocationInParent(SimpleName node) {
		if (node.getLocationInParent() == MethodDeclaration.NAME_PROPERTY) {
			return true;
		}
		if (node.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY) {
			return true;
		}
		if (node.getLocationInParent() == SingleVariableDeclaration.NAME_PROPERTY) {
			return true;
		}
		if (node.getLocationInParent() == LabeledStatement.LABEL_PROPERTY
				|| node.getLocationInParent() == ContinueStatement.LABEL_PROPERTY
				|| node.getLocationInParent() == BreakStatement.LABEL_PROPERTY

		) {
			return true;
		}

		return false;
	}

	private boolean analyzeNameBinding(Name name) {
		IBinding binding = name.resolveBinding();
		if (binding == null) {
			return false;
		}
		return isSupportedBinding(binding);
	}

	private boolean isSupportedBinding(IBinding binding) {
		if (Modifier.isStatic(binding.getModifiers())) {
			return true;
		}

		if (binding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;

			if (variableBinding.isField()) {
				ITypeBinding declaringClass = variableBinding.getDeclaringClass();
				return ClassRelationUtil.isContentOfType(declaringClass, typeDeclarationQualifiedName);
			}
			return ClassRelationUtil.isContentOfType(variableBinding.getDeclaringMethod()
				.getDeclaringClass(), typeDeclarationQualifiedName);
		}

		if (binding.getKind() == IBinding.METHOD) {
			IMethodBinding methodBinding = (IMethodBinding) binding;
			ITypeBinding declaringClass = methodBinding.getDeclaringClass();
			return ClassRelationUtil.isContentOfType(declaringClass, typeDeclarationQualifiedName);
		}
		return true;
	}

	public boolean isUnsupportedReferenceExisting() {
		return unsupportedReferenceExisting;
	}

}
