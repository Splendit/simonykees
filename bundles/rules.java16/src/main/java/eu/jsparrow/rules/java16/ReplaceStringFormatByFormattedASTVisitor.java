package eu.jsparrow.rules.java16;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

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
public class ReplaceStringFormatByFormattedASTVisitor extends AbstractASTRewriteASTVisitor {

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
			// astRewrite.remove(firstArgument, null); // ??

			formattedMethodInvocation.setExpression(stringInstanceExpression);
			int formatArgumentsSsize = stringFormatArguments.size();
			if (formatArgumentsSsize > 1) {
				ListRewrite listRewrite = astRewrite.getListRewrite(formattedMethodInvocation,
						MethodInvocation.ARGUMENTS_PROPERTY);

				for (int i = 1; i < formatArgumentsSsize; i++) {
					Expression objectArgument = stringFormatArguments.get(i);
					Expression movedObjectArgument = (Expression) astRewrite.createMoveTarget(objectArgument);
					// astRewrite.remove(objectArgument, null); // ??
					listRewrite.insertLast(movedObjectArgument, null);
				}
			}
			astRewrite.replace(invocation, formattedMethodInvocation, null);
			onRewrite();
		}
		return true;
	}

	private List<Expression> findStringFormatArguments(MethodInvocation invocation) {
		IMethodBinding methodBinding = invocation.resolveMethodBinding();
		if (methodBinding == null) {
			return Collections.emptyList();
		}

		if (!"format".equals(methodBinding.getName())) { //$NON-NLS-1$
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

		if (!ClassRelationUtil.isContentOfType(parameterTypes[0], String.class.getName())) {
			return Collections.emptyList();
		}

		return ASTNodeUtil.convertToTypedList(invocation.arguments(), Expression.class);
	}
}