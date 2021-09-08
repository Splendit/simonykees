package eu.jsparrow.core.visitor.junit.junit3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeLiteral;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * 
 * Helper visitor to find all expression statements containing the invocation of
 * {@code TestRunner.run} like for example:
 * 
 * <pre>
 * TestRunner.run(ExampleJUnit3TestCases.class);
 * </pre>
 *
 */
public class RunTestInvocationStatementsVisitor extends ASTVisitor {

	private final Map<ExpressionStatement, TypeLiteral> runInvocationToTypeLiteralMap = new HashMap<>();

	@Override
	public boolean visit(MethodInvocation node) {

		ExpressionStatement expressionStatement = findSurroundingExpressionStatement(node).orElse(null);
		TypeLiteral typeLiteralArgument = findTypeLiteralArgument(node).orElse(null);
		if (expressionStatement != null && typeLiteralArgument != null) {
			runInvocationToTypeLiteralMap.put(expressionStatement, typeLiteralArgument);
		}
		return true;
	}

	private Optional<ExpressionStatement> findSurroundingExpressionStatement(MethodInvocation methodInvocation) {

		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}
		ExpressionStatement expressionStatement = (ExpressionStatement) methodInvocation.getParent();
		if (expressionStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return Optional.empty();
		}

		return Optional.of(expressionStatement);
	}

	private Optional<TypeLiteral> findTypeLiteralArgument(MethodInvocation methodInvocation) {

		if (!methodInvocation.getName()
			.getIdentifier()
			.equals("run")) { //$NON-NLS-1$
			return Optional.empty();
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.size() != 1) {
			return Optional.empty();
		}

		Expression argument = arguments.get(0);
		if (argument.getNodeType() != ASTNode.TYPE_LITERAL) {
			return Optional.empty();
		}

		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return Optional.empty();
		}

		ITypeBinding declaringClass = methodBinding.getDeclaringClass();

		if (ClassRelationUtil.isContentOfType(declaringClass, "junit.textui.TestRunner")) { //$NON-NLS-1$
			return Optional.of((TypeLiteral) argument);
		}

		return Optional.empty();
	}

	public Map<ExpressionStatement, TypeLiteral> getRunInvocationToTypeLiteralMap() {
		return runInvocationToTypeLiteralMap;
	}
}
