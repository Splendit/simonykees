package eu.jsparrow.core.visitor.impl.entryset;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Stores all structural informations about an EnhancedForStatement which seems
 * to be the iteration on the key set of a map where also the value
 * corresponding to the keys is needed.
 * 
 * Example:
 * 
 * <pre>
 * for (String key : map.keySet()) {
 * 	Integer value = map.get(key);
 * 	// ...
 * }
 * </pre>
 * 
 * Example with preceding variable declaration:
 * 
 * <pre>
 * Set<String> keySet = map.keySet();
 * for (String key : keySet) {
 * 	Integer value = map.get(key);
 * 	// ...
 * }
 * </pre>
 * 
 * In both examples {@link #assumedMapVariableName} is the simple name
 * {@code map}.
 * 
 */
class SupportedLoopStructure {

	private static final String GET = "get"; //$NON-NLS-1$
	private static final ASTMatcher AST_MATCHER = new ASTMatcher();
	private static final String KEY_SET = "keySet"; //$NON-NLS-1$

	private final SingleVariableDeclaration parameter;
	private final Expression expression;
	private final SimpleName assumedMapVariableName;
	private final Block body;
	private final MethodInvocation assumedMapGetterInvocation;

	static Optional<SupportedLoopStructure> findSupportedLoopStructure(
			EnhancedForStatement enhancedForStatement) {

		Block body = ASTNodeUtil.castToOptional(enhancedForStatement.getBody(), Block.class)
			.orElse(null);
		if (body == null) {
			return Optional.empty();
		}

		Expression forStatementExpression = enhancedForStatement.getExpression();
		SimpleName assumedMapVariableName = findAssumedMapVariableName(forStatementExpression).orElse(null);
		if (assumedMapVariableName == null) {
			return Optional.empty();
		}

		String expectedKeyIdentifier = enhancedForStatement.getParameter()
			.getName()
			.getIdentifier();

		MethodInvocation assumedMapGetterInvocation = findAssumedMapGetterInvocation(body, assumedMapVariableName,
				expectedKeyIdentifier)
					.orElse(null);

		if (assumedMapGetterInvocation == null) {
			return Optional.empty();
		}

		return Optional
			.of(new SupportedLoopStructure(enhancedForStatement, assumedMapVariableName, body,
					assumedMapGetterInvocation));
	}

	private static Optional<SimpleName> findAssumedMapVariableName(Expression forStatementExpression) {
		if (forStatementExpression.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation methodInvocation = (MethodInvocation) forStatementExpression;
			return extractKeySetInvocationExpression(methodInvocation);
		}

		return Optional.empty();
	}

	private static Optional<SimpleName> extractKeySetInvocationExpression(MethodInvocation methodInvocation) {
		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression == null) {
			return Optional.empty();
		}
		if (methodInvocationExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Optional.empty();
		}

		SimpleName assumedMapExpression = (SimpleName) methodInvocationExpression;

		if (!methodInvocation.arguments()
			.isEmpty()) {
			return Optional.empty();
		}

		String methodIdentifier = methodInvocation.getName()
			.getIdentifier();

		if (!KEY_SET.equals(methodIdentifier)) {
			return Optional.empty();
		}

		return Optional.of(assumedMapExpression);
	}

	static Optional<MethodInvocation> findAssumedMapGetterInvocation(Block block, SimpleName expectedMapName,
			String expectedKeyIdentifier) {

		VariableDeclarationStatement assumedValueDeclaration = ItemAtIndex
			.findItemAtIndex(block.statements(), 0, VariableDeclarationStatement.class)
			.orElse(null);

		if (assumedValueDeclaration == null) {
			return Optional.empty();
		}

		VariableDeclarationFragment firstFragment = ItemAtIndex
			.findItemAtIndex(assumedValueDeclaration.fragments(), 0, VariableDeclarationFragment.class)
			.orElse(null);

		if (firstFragment == null) {
			// This should never happen with valid code
			return Optional.empty();
		}

		Expression initializer = firstFragment.getInitializer();
		if (initializer == null) {
			return Optional.empty();
		}

		if (initializer.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Optional.empty();
		}

		MethodInvocation methodInvocation = (MethodInvocation) initializer;

		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression == null) {
			return Optional.empty();
		}

		if (!AST_MATCHER.match(expectedMapName, methodInvocationExpression)) {
			return Optional.empty();
		}

		String invocationIdentifier = methodInvocation.getName()
			.getIdentifier();
		if (!GET.equals(invocationIdentifier)) {
			return Optional.empty();
		}

		SimpleName expectedUniqueArgument = ASTNodeUtil
			.findSingletonListElement(methodInvocation.arguments(), SimpleName.class)
			.orElse(null);
		if (expectedUniqueArgument == null) {
			return Optional.empty();
		}

		String uniqueArgumentIdentifier = expectedUniqueArgument.getIdentifier();
		if (!expectedKeyIdentifier.equals(uniqueArgumentIdentifier)) {
			return Optional.empty();
		}

		return Optional.of(methodInvocation);
	}

	private SupportedLoopStructure(EnhancedForStatement enhancedForStatement, SimpleName assumedMapVariableName,
			Block body, MethodInvocation assumedMapGetterInvocation) {
		this.parameter = enhancedForStatement.getParameter();
		this.expression = enhancedForStatement.getExpression();
		this.assumedMapVariableName = assumedMapVariableName;
		this.body = body;
		this.assumedMapGetterInvocation = assumedMapGetterInvocation;

	}

	SingleVariableDeclaration getParameter() {
		return parameter;
	}

	Expression getExpression() {
		return expression;
	}

	/**
	 * assumed to be an Expression of the type {@link java.util.Map}
	 */
	SimpleName getAssumedMapVariableName() {
		return assumedMapVariableName;
	}

	Block getBody() {
		return body;
	}

	MethodInvocation getAssumedMapGetterInvocation() {
		return assumedMapGetterInvocation;
	}

}