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

		InitialAnalysisData initialAnalysisData = findInitialData(node).orElse(null);
		if (initialAnalysisData == null) {
			return true;
		}
		MethodInvocation assertThatInvocation = initialAnalysisData.getAssertThatInvocation();
		final AssertJAssertThatWithAssertionData analysisChainInitialData = initialAnalysisData
			.getInitialAnalysisChainData();

		AssertJAssertThatWithAssertionData dataExpectedToChange = analysisChainInitialData;

		AllBooleanAssertionsAnalyzer allBooleanAssertinsAnalyzer = AllBooleanAssertionsAnalyzer
			.conditionalInstance(analysisChainInitialData)
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

		dataExpectedToChange = AssertionWithSizeAndLengthAnalyzer
			.findResultForAssertionWithSizeOrLength(dataExpectedToChange)
			.orElse(dataExpectedToChange);

		dataExpectedToChange = AssertionWithSizeAndLengthAnalyzer
			.findHasSameSizeAssertionData(dataExpectedToChange)
			.orElse(dataExpectedToChange);

		dataExpectedToChange = AssertionWithLiteralArgumentAnalyzer
			.findDataForAssertionWithLiteral(dataExpectedToChange)
			.orElse(dataExpectedToChange);

		if (dataExpectedToChange != analysisChainInitialData) {
			transform(assertThatInvocation, node, dataExpectedToChange);
		}

		return true;
	}

	static Optional<InitialAnalysisData> findInitialData(MethodInvocation assumedAssertion) {

		if (assumedAssertion.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return Optional.empty();
		}

		List<Expression> assumedAssertionArguments = ASTNodeUtil.convertToTypedList(assumedAssertion.arguments(),
				Expression.class);

		if (assumedAssertionArguments.size() > 1) {
			return Optional.empty();
		}

		Expression assertionInvocationExpression = assumedAssertion.getExpression();
		if (assertionInvocationExpression == null
				|| assertionInvocationExpression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Optional.empty();
		}

		MethodInvocation assumedAssertThatInvocation = (MethodInvocation) assertionInvocationExpression;
		List<Expression> assumedAssertThatArguments = ASTNodeUtil
			.convertToTypedList(assumedAssertThatInvocation.arguments(), Expression.class);
		if (assumedAssertThatArguments.size() != 1) {
			return Optional.empty();
		}
		Expression assumedAssertThatArgument = assumedAssertThatArguments.get(0);

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

		String assumedAssertionName = assumedAssertion.getName()
			.getIdentifier();
		AssertJAssertThatWithAssertionData assertJAssertThatWithAssertionData;
		if (assumedAssertionArguments.isEmpty()) {
			assertJAssertThatWithAssertionData = new AssertJAssertThatWithAssertionData(assumedAssertThatArgument,
					assumedAssertionName);
		} else {
			assertJAssertThatWithAssertionData = new AssertJAssertThatWithAssertionData(assumedAssertThatArgument,
					assumedAssertionName, assumedAssertionArguments.get(0));
		}
		return Optional
			.of(new InitialAnalysisData(assumedAssertThatInvocation, assertJAssertThatWithAssertionData));

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
