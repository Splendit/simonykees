package eu.jsparrow.core.visitor.functionalinterface;

import java.util.Collections;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class DefaultMethodInvocationASTVisitor extends ASTVisitor {

	private final ITypeBinding anonymousClassTypeBinding;

	private boolean flagCancelTransformation;

	public DefaultMethodInvocationASTVisitor(AnonymousClassDeclaration anonymousClassDeclaration) {
		anonymousClassTypeBinding = anonymousClassDeclaration.resolveBinding();
	}

	private boolean isObjectMethod(MethodInvocation node) {
		if (node.resolveMethodBinding() == null) {
			return false;
		}
		if (node.resolveMethodBinding()
			.getDeclaringClass() == null) {
			return false;
		}
		String qualifiedName = node.resolveMethodBinding()
			.getDeclaringClass()
			.getQualifiedName();

		if (qualifiedName == null) {
			return false;
		}
		return qualifiedName.equals(java.lang.Object.class.getName());
	}

	@Override
	public boolean visit(MethodInvocation node) {

		if (isObjectMethod(node)) {
			return true;
		}

		Expression expression = node.getExpression();
		if (expression == null) {
			ITypeBinding declaringClass = node.resolveMethodBinding()
				.getDeclaringClass();
			String declaringClassQualifiedName = declaringClass.getQualifiedName();
			if (declaringClass.isParameterizedType()) {
				declaringClassQualifiedName = declaringClass.getErasure()
					.getQualifiedName();

			}
			if (ClassRelationUtil.isInheritingContentOfTypes(
					anonymousClassTypeBinding,
					Collections.singletonList(declaringClassQualifiedName))) {
				flagCancelTransformation = true;
			}

		} else if (expression.getNodeType() == ASTNode.THIS_EXPRESSION) {
			ThisExpression thisExpression = (ThisExpression) expression;
			if (thisExpression.getQualifier() == null) {
				flagCancelTransformation = true;
			}
		}
		return !flagCancelTransformation;
	}

	public boolean isFlagCancelTransformation() {
		return flagCancelTransformation;
	}

}
