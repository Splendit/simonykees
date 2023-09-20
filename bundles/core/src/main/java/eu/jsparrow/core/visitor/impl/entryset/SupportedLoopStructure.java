package eu.jsparrow.core.visitor.impl.entryset;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

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
 * In both examples {@link #assumedMapExpression} is the simple name
 * {@code map}.
 * 
 */
class SupportedLoopStructure {
	private static final String KEY_SET = "keySet"; //$NON-NLS-1$

	private final SingleVariableDeclaration parameter;
	private final Expression expression;
	private final Expression assumedMapExpression;
	private final Block body;
	private final ValueDeclarationStructure valueDeclarationData;

	static Optional<SupportedLoopStructure> findSupportedLoopStructure(
			EnhancedForStatement enhancedForStatement) {

		Block body = ASTNodeUtil.castToOptional(enhancedForStatement.getBody(), Block.class)
			.orElse(null);
		if (body == null) {
			return Optional.empty();
		}

		Expression forStatementExpression = enhancedForStatement.getExpression();
		Expression assumedMapExpression = findAssumedMapExpression(forStatementExpression).orElse(null);
		if (assumedMapExpression == null) {
			return Optional.empty();
		}

		String expectedKeyIdentifier = enhancedForStatement.getParameter()
			.getName()
			.getIdentifier();

		ValueDeclarationStructure valueDeclarationData = ValueDeclarationStructure.findSupportedValueDeclaration(body,
				assumedMapExpression, expectedKeyIdentifier)
			.orElse(null);

		if (valueDeclarationData == null) {
			return Optional.empty();
		}

		return Optional
			.of(new SupportedLoopStructure(enhancedForStatement, assumedMapExpression, body,
					valueDeclarationData));
	}

	private static Optional<Expression> findAssumedMapExpression(Expression forStatementExpression) {
		if (forStatementExpression.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation methodInvocation = (MethodInvocation) forStatementExpression;
			return extractKeySetInvocationExpression(methodInvocation);
		}

		return Optional.empty();
	}

	private static Optional<Expression> extractKeySetInvocationExpression(MethodInvocation methodInvocation) {
		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression == null) {
			return Optional.empty();
		}

		if (!methodInvocation.arguments()
			.isEmpty()) {
			return Optional.empty();
		}

		String methodIdentifier = methodInvocation.getName()
			.getIdentifier();

		if (!KEY_SET.equals(methodIdentifier)) {
			return Optional.empty();
		}

		return Optional.of(methodInvocationExpression);
	}

	private SupportedLoopStructure(EnhancedForStatement enhancedForStatement, Expression assumedMapExpression,
			Block body,
			ValueDeclarationStructure valueDeclarationData) {
		this.parameter = enhancedForStatement.getParameter();
		this.expression = enhancedForStatement.getExpression();
		this.assumedMapExpression = assumedMapExpression;
		this.body = body;
		this.valueDeclarationData = valueDeclarationData;

	}

	public SingleVariableDeclaration getParameter() {
		return parameter;
	}

	Expression getExpression() {
		return expression;
	}

	/**
	 * assumed to be an Expression of the type {@link java.util.Map}
	 */
	Expression getAssumedMapExpression() {
		return assumedMapExpression;
	}

	public Block getBody() {
		return body;
	}

	public ValueDeclarationStructure getValueDeclarationData() {
		return valueDeclarationData;
	}
}