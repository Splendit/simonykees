package eu.jsparrow.core.visitor.functionalinterface;

import java.util.Collections;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Finds invocations of default methods which prohibit transformation of a
 * functional anonymous class to a lambda.
 * <p>
 * A method invocation will prohibit the transformation of a functional
 * anonymous class to a lambda if the corresponding method is declared (mostly
 * as default method) by a super-interface of the anonymous class and is invoked
 * either without qualifier or with the this-qualifier.
 * 
 * @since 3.16.0
 */
public class DefaultMethodInvocationVisitor extends ASTVisitor {

	private final ITypeBinding anonymousClassTypeBinding;

	private boolean flagCancelTransformation;

	public DefaultMethodInvocationVisitor(AnonymousClassDeclaration anonymousClassDeclaration) {
		anonymousClassTypeBinding = anonymousClassDeclaration.resolveBinding();
	}

	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding methodBinding = node.resolveMethodBinding();
		if (methodBinding == null) {
			return true;
		}
		int modifiers = methodBinding.getModifiers();
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();

		if (Modifier.isDefault(modifiers)) {

			String declaringClassQualifiedName = declaringClass.getErasure()
				.getQualifiedName();

			boolean isInheritingContentOfTypes = ClassRelationUtil.isInheritingContentOfTypes(
					anonymousClassTypeBinding,
					Collections.singletonList(declaringClassQualifiedName));

			if (isInheritingContentOfTypes) {
				flagCancelTransformation = true;
				return false;
			}
		}

		if (ClassRelationUtil.isContentOfType(declaringClass, java.lang.Object.class.getName())) {
			Expression expression = node.getExpression();
			if (expression == null || expression.getNodeType() == ASTNode.THIS_EXPRESSION) {
				flagCancelTransformation = true;
				return false;
			}
		}

		return true;
	}

	public boolean isFlagCancelTransformation() {
		return flagCancelTransformation;
	}

}
