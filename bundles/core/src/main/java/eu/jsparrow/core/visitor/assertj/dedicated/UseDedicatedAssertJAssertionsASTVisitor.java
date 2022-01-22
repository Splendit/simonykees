package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeLiteral;

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

		if (node.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return true;
		}
		List<Expression> assumedAssertionArguments = ASTNodeUtil.convertToTypedList(node.arguments(),
				Expression.class);
		if (assumedAssertionArguments.size() > 1) {
			return true;
		}

		MethodInvocation assertThatInvocation = findAssertThatAsAssertionExpression(node).orElse(null);
		if (assertThatInvocation == null || assertThatInvocation.arguments()
			.size() != 1) {
			return true;
		}
		List<Expression> assertThatArguments = ASTNodeUtil
			.convertToTypedList(assertThatInvocation.arguments(), Expression.class);

		if (assertThatArguments.size() != 1) {
			return true;
		}
		Expression assertThatArgument = assertThatArguments.get(0);

		final AssertJAssertThatWithAssertionData initialData = AssertJAssertThatWithAssertionData
			.findDataForAssumedAssertion(node)
			.orElse(null);

		if (initialData == null) {
			return true;
		}
		AssertJAssertThatWithAssertionData dataExpectedToChange = initialData;

		AllBooleanAssertionsAnalyzer allBooleanAssertinsAnalyzer = AllBooleanAssertionsAnalyzer
			.conditionalInstance(initialData)
			.orElse(null);

		if (allBooleanAssertinsAnalyzer != null) {
			BooleanAssertionWithInstanceofAnalysisResult resultForAssertionWithInstanceof = allBooleanAssertinsAnalyzer
				.findResultForInstanceofAsAssertThatArgument()
				.orElse(null);
			if (resultForAssertionWithInstanceof != null) {
				transform(assertThatInvocation, node, resultForAssertionWithInstanceof);
				return true;
			}
			dataExpectedToChange = allBooleanAssertinsAnalyzer.findResultForOtherAssertThatArgument()
				.orElse(null);
			if (dataExpectedToChange == null) {
				return true;
			}
		}

		dataExpectedToChange = AssertionWithLiteralArgumentAnalyzer
			.findDataForAssertionWithLiteral(dataExpectedToChange)
			.orElse(dataExpectedToChange);

		if (dataExpectedToChange != initialData) {
			transform(assertThatInvocation, node, dataExpectedToChange);
		}

		return true;
	}

	Optional<MethodInvocation> findAssertThatAsAssertionExpression(MethodInvocation assumedAssertion) {

		Expression assertionInvocationExpression = assumedAssertion.getExpression();
		if (assertionInvocationExpression == null
				|| assertionInvocationExpression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Optional.empty();
		}

		MethodInvocation assumedAssertThatInvocation = (MethodInvocation) assertionInvocationExpression;
		String assumedAssertThatMethodName = assumedAssertThatInvocation.getName()
			.getIdentifier();

		if (!SupportedAssertJAssertions.isSupportedAssertJAsserThatMethodName(assumedAssertThatMethodName)) {
			return Optional.empty();
		}
		IMethodBinding assertThatMethodBinding = assumedAssertThatInvocation.resolveMethodBinding();

		if (assertThatMethodBinding == null ||
				!SupportedAssertJAssertions.isSupportedAssertionsType(assertThatMethodBinding.getDeclaringClass())) {
			return Optional.empty();
		}
		return Optional.of(assumedAssertThatInvocation);
	}

	private void transform(MethodInvocation assertThatInvocation, MethodInvocation node,
			BooleanAssertionWithInstanceofAnalysisResult data) {
		SimpleType instanceofRightOperand = data.getInstanceofRightOperand();
		MethodInvocation newAssertion = createIsInstanceofInvocation(instanceofRightOperand);
		MethodInvocation newAssertThatInvocation = createNewAssertThatInvocation(
				assertThatInvocation, data.getInstanceOfLeftOperand());
		newAssertion.setExpression(newAssertThatInvocation);
		astRewrite.replace(node, newAssertion, null);
		onRewrite();
	}

	@SuppressWarnings("unchecked")
	private void transform(MethodInvocation assertThatInvocation, MethodInvocation node,
			AssertJAssertThatWithAssertionData data) {
		AST ast = astRewrite.getAST();
		MethodInvocation newAssertion = ast.newMethodInvocation();
		String newAssertionName = data.getAssertionName();
		newAssertion.setName(ast.newSimpleName(newAssertionName));
		data.getAssertionArgument()
			.map(assertionArgument -> (Expression) astRewrite.createCopyTarget(assertionArgument))
			.ifPresent(assertionArgument -> newAssertion.arguments()
				.add(assertionArgument));

		MethodInvocation newAssertThatInvocation = createNewAssertThatInvocation(
				assertThatInvocation, data.getAssertThatArgument());
		newAssertion.setExpression(newAssertThatInvocation);
		astRewrite.replace(node, newAssertion, null);
		onRewrite();
	}

	@SuppressWarnings("unchecked")
	private MethodInvocation createNewAssertThatInvocation(MethodInvocation assertThatInvocation,
			Expression assertThatArgument) {
		AST ast = astRewrite.getAST();
		MethodInvocation newAssertThatInvocation = ast.newMethodInvocation();
		String assertThatMethodName = assertThatInvocation.getName()
			.getIdentifier();
		newAssertThatInvocation.setName(ast.newSimpleName(assertThatMethodName));
		Expression newAssertThatArgument = (Expression) astRewrite.createCopyTarget(assertThatArgument);
		newAssertThatInvocation.arguments()
			.add(newAssertThatArgument);
		Optional.ofNullable(assertThatInvocation.getExpression())
			.map(expression -> (Expression) astRewrite.createCopyTarget(expression))
			.ifPresent(newAssertThatInvocation::setExpression);
		return newAssertThatInvocation;
	}

	@SuppressWarnings("unchecked")
	private MethodInvocation createIsInstanceofInvocation(SimpleType instanceofRightOperand) {
		AST ast = astRewrite.getAST();
		MethodInvocation newAssertion = ast.newMethodInvocation();
		newAssertion.setName(ast.newSimpleName("isInstanceOf")); //$NON-NLS-1$
		SimpleType simpleTypeCopy = (SimpleType) astRewrite.createCopyTarget(instanceofRightOperand);
		TypeLiteral newAssertionArgument = ast.newTypeLiteral();
		newAssertionArgument.setType(simpleTypeCopy);
		newAssertion.arguments()
			.add(newAssertionArgument);
		return newAssertion;
	}
}
