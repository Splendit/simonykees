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

public class DefaultMethodInvocationASTVisitor extends ASTVisitor {

	private final ITypeBinding anonymousClassTypeBinding;

	private boolean flagCancelTransformation;

	public DefaultMethodInvocationASTVisitor(AnonymousClassDeclaration anonymousClassDeclaration) {
		anonymousClassTypeBinding = anonymousClassDeclaration.resolveBinding();
	}

	@Override
	public boolean visit(MethodInvocation node) {
		Expression expression = node.getExpression();
		if(expression != null) {
			if (expression.getNodeType() != ASTNode.THIS_EXPRESSION) {
				return true;
			}
			ThisExpression thisExpression = (ThisExpression)expression;
			if(thisExpression.getQualifier() != null) {
				return true;
			}			
		}		

		IMethodBinding methodBinding = node.resolveMethodBinding();
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
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
