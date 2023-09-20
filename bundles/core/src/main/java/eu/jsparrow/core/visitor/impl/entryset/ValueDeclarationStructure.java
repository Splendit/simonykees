package eu.jsparrow.core.visitor.impl.entryset;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Stores all structural informations about a variable which is obviously
 * initialized with a value corresponding to a key in a map.
 * 
 * Example:
 * 
 * <pre>
 * {
 * 	Integer value = map.get(key);
 * 	// ...
 * }
 * </pre>
 * 
 * 
 */
class ValueDeclarationStructure {
	private static final String GET = "get"; //$NON-NLS-1$
	private static final ASTMatcher AST_MATCHER = new ASTMatcher();
	private final VariableDeclarationStatement declarationStatement;
	private final VariableDeclarationFragment declarationFragment;
	private final MethodInvocation valueByKeyGetterInvocation;

	static Optional<ValueDeclarationStructure> findSupportedValueDeclaration(Block block,
			Expression expectedMapExpression, String expectedKeyIdentifier) {

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

		return extractValueByKeyGetterInvocation(firstFragment, expectedMapExpression, expectedKeyIdentifier)
			.map(valueByKeyGetterInvocation -> new ValueDeclarationStructure(assumedValueDeclaration, firstFragment,
					valueByKeyGetterInvocation));
	}

	private static Optional<MethodInvocation> extractValueByKeyGetterInvocation(VariableDeclarationFragment fragment,
			Expression expectedMapExpression, String expectedKeyIdentifier) {
		Expression initializer = fragment.getInitializer();
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

		if (!methodInvocationExpression.subtreeMatch(AST_MATCHER, expectedMapExpression)) {
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

	private ValueDeclarationStructure(VariableDeclarationStatement declarationStatement,
			VariableDeclarationFragment declarationFragment, MethodInvocation valueByKeyGetterInvocation) {
		this.declarationStatement = declarationStatement;
		this.declarationFragment = declarationFragment;
		this.valueByKeyGetterInvocation = valueByKeyGetterInvocation;
	}

	VariableDeclarationStatement getDeclarationStatement() {
		return declarationStatement;
	}

	VariableDeclarationFragment getDeclarationFragment() {
		return declarationFragment;
	}

	MethodInvocation getValueByKeyGetterInvocation() {
		return valueByKeyGetterInvocation;
	}
}