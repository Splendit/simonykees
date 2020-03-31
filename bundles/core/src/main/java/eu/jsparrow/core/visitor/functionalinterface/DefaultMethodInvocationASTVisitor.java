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

	private static final String JAVA_LANG_OBJECT = java.lang.Object.class.getName();

	private final ITypeBinding anonymousClassTypeBinding;

	private boolean flagCancelTransformation;

	public DefaultMethodInvocationASTVisitor(AnonymousClassDeclaration anonymousClassDeclaration) {
		anonymousClassTypeBinding = anonymousClassDeclaration.resolveBinding();
	}

	@Override
	public boolean visit(MethodInvocation node) {

		Expression expression = node.getExpression();
		if (expression == null) {
			String declaringClassQualifiedName = node.resolveMethodBinding()
				.getDeclaringClass()
				.getQualifiedName();
			if (!declaringClassQualifiedName.equals(JAVA_LANG_OBJECT) &&
					ClassRelationUtil.isInheritingContentOfTypes(
							anonymousClassTypeBinding,
							Collections.singletonList(declaringClassQualifiedName))) {
				flagCancelTransformation = true;

			}

		} else if (expression.getNodeType() == ASTNode.THIS_EXPRESSION) {
			ThisExpression thisExpression = (ThisExpression) expression;
			if (thisExpression.getQualifier() == null) {
				String declaringClassQualifiedName = node.resolveMethodBinding()
					.getDeclaringClass()
					.getQualifiedName();
				if (!declaringClassQualifiedName.equals(JAVA_LANG_OBJECT)) {
					flagCancelTransformation = true;
				}
			}
		}

		return !flagCancelTransformation;
	}

	public boolean isFlagCancelTransformation() {
		return flagCancelTransformation;
	}

}
