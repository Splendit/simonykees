package eu.jsparrow.core.visitor.impl;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.markers.common.ReplaceStringFormatByFormattedEvent;
import eu.jsparrow.rules.common.markers.Resolver;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * This visitor looks for {@link MethodInvocation}-nodes which represent
 * invocations of the method {@link String#format(String, Object...)} and
 * replaces them by invocations of the Java 15 method
 * {@code String#formatted(Object...)}
 * 
 * Example:
 * 
 * <pre>
 * String output = String.format(
 * 		"Name: %s, Phone: %s, Address: %s, Salary: $%.2f",
 * 		name, phone, address, salary);
 * </pre>
 * 
 * is transformed to
 * 
 * <pre>
 * String output = "Name: %s, Phone: %s, Address: %s, Salary: $%.2f"
 * 	.formatted(name, phone, address, salary);
 * </pre>
 * 
 * @since 4.3.0
 * 
 */
public class ReplaceStringFormatByFormattedASTVisitor extends AbstractASTRewriteASTVisitor implements ReplaceStringFormatByFormattedEvent {

	@Override
	public boolean visit(MethodInvocation invocation) {

		List<Expression> stringFormatArguments = findStringFormatArguments(invocation);
		if (!stringFormatArguments.isEmpty()) {
			AST ast = astRewrite.getAST();
			MethodInvocation formattedMethodInvocation = ast.newMethodInvocation();
			SimpleName formattedMethodName = ast.newSimpleName("formatted"); //$NON-NLS-1$
			formattedMethodInvocation.setName(formattedMethodName);

			Expression firstArgument = stringFormatArguments.get(0);
			Expression stringInstanceExpression = (Expression) astRewrite.createMoveTarget(firstArgument);

			formattedMethodInvocation.setExpression(stringInstanceExpression);
			int formatArgumentsSize = stringFormatArguments.size();
			if (formatArgumentsSize > 1) {
				@SuppressWarnings("unchecked")
				List<Expression> formattedArguments = formattedMethodInvocation.arguments();
				for (int i = 1; i < formatArgumentsSize; i++) {
					Expression objectArgument = stringFormatArguments.get(i);
					Expression movedObjectArgument = (Expression) astRewrite.createMoveTarget(objectArgument);
					formattedArguments.add(movedObjectArgument);
				}
			}
			astRewrite.replace(invocation, formattedMethodInvocation, null);
			onRewrite();
			addMarkerEvent(invocation);
		}
		return true;
	}

	private List<Expression> findStringFormatArguments(MethodInvocation invocation) {
		SimpleName name = invocation.getName();
		if (!"format".equals(name.getIdentifier())) { //$NON-NLS-1$
			return Collections.emptyList();
		}

		if (!verifyFirstParameterType(invocation)) {
			return Collections.emptyList();
		}

		IMethodBinding methodBinding = invocation.resolveMethodBinding();
		if (methodBinding == null) {
			return Collections.emptyList();
		}

		if (!ClassRelationUtil.isContentOfType(methodBinding.getDeclaringClass(), String.class.getName())) {
			return Collections.emptyList();
		}

		IMethodBinding methodDeclaration = methodBinding.getMethodDeclaration();

		ITypeBinding[] parameterTypes = methodDeclaration.getParameterTypes();

		if (parameterTypes.length != 2) {
			return Collections.emptyList();
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(invocation.arguments(), Expression.class);
		if (!arguments.isEmpty() &&
				arguments.get(0)
					.getNodeType() == ASTNode.INFIX_EXPRESSION) {

			return Collections.emptyList();
		}
		return arguments;
	}

	private boolean verifyFirstParameterType(MethodInvocation invocation) {
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(invocation.arguments(), Expression.class);
		if (arguments.isEmpty()) {
			return false;
		}
		Expression first = arguments.get(0);
		ITypeBinding firstArgType = first.resolveTypeBinding();
		return ClassRelationUtil.isContentOfType(firstArgType, java.lang.String.class.getName());
	}
}