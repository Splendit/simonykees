package eu.jsparrow.core.visitor.functionalinterface;

import java.util.Collections;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ThisExpression;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Finds invocations of methods which prohibit transformation of a functional
 * anonymous class to a lambda.
 * <p>
 * A method invocation will prohibit the transformation of a functional
 * anonymous class to a lambda if the corresponding method is declared (mostly
 * as default method) by a super-interface of the anonymous class and is invoked
 * either without qualifier or with the this-qualifier.
 * 
 * @since 3.16.0
 */
public class DefaultMethodInvocationASTVisitor extends ASTVisitor {

	private final AnonymousClassDeclaration anonymousClassDeclaration;

	private final ITypeBinding anonymousClassTypeBinding;

	private boolean flagCancelTransformation;

	public DefaultMethodInvocationASTVisitor(AnonymousClassDeclaration anonymousClassDeclaration) {
		this.anonymousClassDeclaration = anonymousClassDeclaration;
		anonymousClassTypeBinding = anonymousClassDeclaration.resolveBinding();
	}

	private AnonymousClassDeclaration findSurroundingAnonymousClassDeclaration(MethodInvocation methodInvocation) {
		ASTNode parent = methodInvocation.getParent();
		while (parent != null) {
			if (parent.getNodeType() == ASTNode.ANONYMOUS_CLASS_DECLARATION) {
				return (AnonymousClassDeclaration) parent;
			}
			parent = parent.getParent();
		}
		return null;
	}

	@Override
	public boolean visit(MethodInvocation node) {

		AnonymousClassDeclaration surroundingAnonymous = findSurroundingAnonymousClassDeclaration(node);
		if (surroundingAnonymous != this.anonymousClassDeclaration) {
			return true;
		}

		Expression expression = node.getExpression();
		if (expression != null) {
			if (expression.getNodeType() != ASTNode.THIS_EXPRESSION) {
				return true;
			}
			ThisExpression thisExpression = (ThisExpression) expression;
			if (thisExpression.getQualifier() != null) {
				return true;
			}
		}

		IMethodBinding methodBinding = node.resolveMethodBinding();
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if(declaringClass == anonymousClassTypeBinding) {
			flagCancelTransformation = true;
			return false;
		}
		String declaringClassQualifiedName = declaringClass.getQualifiedName();
		if (declaringClass.isParameterizedType()) {
			declaringClassQualifiedName = declaringClass.getErasure()
				.getQualifiedName();
		}
		if (declaringClassQualifiedName.equals(java.lang.Object.class.getName())) {
			return true;
		}

		if (ClassRelationUtil.isInheritingContentOfTypes(
				anonymousClassTypeBinding,
				Collections.singletonList(declaringClassQualifiedName))) {
			flagCancelTransformation = true;
		}
		return !flagCancelTransformation;
	}

	public boolean isFlagCancelTransformation() {
		return flagCancelTransformation;
	}

}
