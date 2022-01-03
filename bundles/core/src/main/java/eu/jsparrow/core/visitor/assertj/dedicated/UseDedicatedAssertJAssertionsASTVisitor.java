package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.assertj.SupportedAssertJAssertions;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * This visitor looks for AssertJ assertions which can be replaced by more
 * specific, dedicated AssertJ assertions.
 * <p>
 * For example, the AssertJ assertion
 * 
 * <pre>
 * assertThat(string.equals("Hello World!")).isTrue();
 * </pre>
 * 
 * can be replaced by a single one like
 * 
 * <pre>
 * assertThat(string).isEqualTo("Hello World!");
 * </pre>
 * 
 * @since 4.7.0
 *
 */
public class UseDedicatedAssertJAssertionsASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(MethodInvocation node) {

		findDedicatedAssertionData(node).ifPresent(data -> {
			MethodInvocation newAssertion = CopyMethodInvocation
				.createNewMethodInvocation(data.getNewAssertionData(), astRewrite);
			MethodInvocation newAssertThat = CopyMethodInvocation
				.createNewMethodInvocation(data.getNewAssertThatData(), astRewrite);
			newAssertion.setExpression(newAssertThat);
			astRewrite.replace(node, newAssertion, null);
			onRewrite();
		});

		return true;
	}

	private Optional<DedicatedAssertionData> findDedicatedAssertionData(MethodInvocation node) {

		if (node.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}

		Expression invocationExpression = node.getExpression();
		if (invocationExpression == null || invocationExpression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Optional.empty();
		}

		MethodInvocation assumedAssertThatInvocation = (MethodInvocation) invocationExpression;
		List<Expression> assertThatArguments = ASTNodeUtil.convertToTypedList(assumedAssertThatInvocation.arguments(),
				Expression.class);

		if (assertThatArguments.size() != 1) {
			return Optional.empty();
		}
		Expression assertThatArgument = assertThatArguments.get(0);

		if (!SupportedAssertJAssertions.isSupportedAssertJAsserThatMethodName(assumedAssertThatInvocation.getName()
			.getIdentifier())) {
			return Optional.empty();
		}

		IMethodBinding assertThatMethodBinding = assumedAssertThatInvocation.resolveMethodBinding();

		if (assertThatMethodBinding == null ||
				!SupportedAssertJAssertions.isSupportedAssertionsType(assertThatMethodBinding.getDeclaringClass())) {
			return Optional.empty();
		}

		return ReplaceBooleanAssertionsByDedicatedAssertionsAnalyzer
			.analyzeBooleanAssertion(assumedAssertThatInvocation, assertThatArgument, node);
	}

}
