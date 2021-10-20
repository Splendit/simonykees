package eu.jsparrow.rules.java16.javarecords;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;

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
		unsupportedReferenceExisting = !isSupportedBinding(node.resolveBinding());
		return false;
	}

	@Override
	public boolean visit(SimpleName node) {
		unsupportedReferenceExisting = !isSupportedBinding(node.resolveBinding());
		return false;
	}

	private boolean isSupportedBinding(IBinding binding) {
		if (binding == null) {
			return false;
		}
		if (binding.getKind() == IBinding.VARIABLE) {
			IVariableBinding variableBinding = (IVariableBinding) binding;

			if (variableBinding.isField()) {
				if (Modifier.isStatic(variableBinding.getModifiers())) {
					return true;
				}
				ITypeBinding declaringClass = variableBinding.getDeclaringClass();
				return ClassRelationUtil.isContentOfType(declaringClass, typeDeclarationQualifiedName);
			}
			return ClassRelationUtil.isContentOfType(variableBinding.getDeclaringMethod()
				.getDeclaringClass(), typeDeclarationQualifiedName);
		}

		if (binding.getKind() == IBinding.METHOD) {
			IMethodBinding methodBinding = (IMethodBinding) binding;
			if (Modifier.isStatic(methodBinding.getModifiers())) {
				return true;
			}
			ITypeBinding declaringClass = methodBinding.getDeclaringClass();
			return ClassRelationUtil.isContentOfType(declaringClass, typeDeclarationQualifiedName);
		}
		return true;
	}

	public boolean isUnsupportedReferenceExisting() {
		return unsupportedReferenceExisting;
	}

}
