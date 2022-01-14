package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

/**
 * Creates a new {@link MethodInvocation} by copying the data stored in the
 * given instance of {@link MethodInvocationData}
 *
 */
public class CopyMethodInvocation {

	@SuppressWarnings("unchecked")
	public static MethodInvocation createNewMethodInvocation(MethodInvocationData invocationData,
			ASTRewrite astRewrite) {
		AST ast = astRewrite.getAST();
		MethodInvocation newMethodInvocation = ast.newMethodInvocation();
		newMethodInvocation.setName(ast.newSimpleName(invocationData.getMethodName()));

		List<Type> newTpeArguments = invocationData.getTypeArguments()
			.stream()
			.map(typeArgument -> (Type) astRewrite.createCopyTarget(typeArgument))
			.collect(Collectors.toList());
		newMethodInvocation.typeArguments()
			.addAll(newTpeArguments);

		List<Expression> newArguments = invocationData.getArguments()
			.stream()
			.map(argument -> (Expression) astRewrite.createCopyTarget(argument))
			.collect(Collectors.toList());
		newMethodInvocation.arguments()
			.addAll(newArguments);

		invocationData.getExpression()
			.ifPresent(expression -> newMethodInvocation
				.setExpression((Expression) astRewrite.createCopyTarget(expression)));
		return newMethodInvocation;
	}

	private CopyMethodInvocation() {
		// hiding implicit default constructor
	}
}
