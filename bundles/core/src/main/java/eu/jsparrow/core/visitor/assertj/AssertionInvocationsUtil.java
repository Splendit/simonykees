package eu.jsparrow.core.visitor.assertj;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

class AssertionInvocationsUtil {

	private AssertionInvocationsUtil() {
		// hiding implicit public default constructor
	}

	@SuppressWarnings("unchecked")
	public static MethodInvocation copyMethodInvocationWithoutExpression(MethodInvocation methodInvocation,
			ASTRewrite astRewrite) {
		AST ast = astRewrite.getAST();
		MethodInvocation newMethodInvocation = ast.newMethodInvocation();
		newMethodInvocation.setName(ast.newSimpleName(methodInvocation.getName()
			.getIdentifier()));

		List<Expression> argumentCopies = ASTNodeUtil
			.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.stream()
			.map(argument -> (Expression) astRewrite.createCopyTarget(argument))
			.collect(Collectors.toList());

		List<Expression> newArguments = newMethodInvocation.arguments();
		newArguments.addAll(argumentCopies);

		List<Type> typeArgumentCopies = ASTNodeUtil.convertToTypedList(methodInvocation.typeArguments(), Type.class)
			.stream()
			.map(typeArgument -> (Type) astRewrite.createCopyTarget(typeArgument))
			.collect(Collectors.toList());
		List<Type> newTypeArguments = newMethodInvocation.typeArguments();
		newTypeArguments.addAll(typeArgumentCopies);

		return newMethodInvocation;
	}

}
