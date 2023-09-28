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
		AssertJAssertThatWithAssertionData dataExpectedToChange = initialAnalysisData.getInitialAnalysisChainData();

		/**
		 * For example, if dataExpectedToChange represents<br>
		 * assertThat(emptyList.size() == 0).isEqualTo(true)<br>
		 * then the return value of findDataForAssertionWithBooleanLiteral which
		 * is assigned again to dataExpectedToChange is expected to
		 * represent<br>
		 * assertThat(emptyList.size() == 0).isTrue()
		 */
		dataExpectedToChange = AssertionWithLiteralArgumentAnalyzer
			.findDataForAssertionWithBooleanLiteral(dataExpectedToChange)
			.orElse(dataExpectedToChange);

		AllBooleanAssertionsAnalyzer allBooleanAssertinsAnalyzer = new AllBooleanAssertionsAnalyzer();
		allBooleanAssertinsAnalyzer.analyzeBooleanAssertion(dataExpectedToChange);
		BooleanAssertionWithInstanceofAnalysisResult analysisResultForInstanceOf = allBooleanAssertinsAnalyzer
			.getAnalysisResultForInstanceofExpression()
			.orElse(null);

		if (analysisResultForInstanceOf != null) {
			transform(assertThatInvocation, node, analysisResultForInstanceOf);
			return true;
		}

		/**
		 * For example, if dataExpectedToChange represents<br>
		 * assertThat(emptyList.size() == 0).isTrue();<br>
		 * then the new value which is assigned to dataExpectedToChange will
		 * represent<br>
		 * assertThat(emptyList.size()).isEqualTo(0);
		 * 
		 */
		dataExpectedToChange = allBooleanAssertinsAnalyzer.getAnalysisResult()
			.orElse(dataExpectedToChange);

		/**
		 * For example, if dataExpectedToChange represents<br>
		 * assertThat(emptyList.size()).isEqualTo(0)<br>
		 * then the new value which is assigned to dataExpectedToChange will
		 * represent<br>
		 * assertThat(emptyList).isEmpty();
		 */
		dataExpectedToChange = AssertionWithSizeAndLengthAnalyzer
			.findResultForAssertionWithSizeOrLength(dataExpectedToChange)
			.orElse(dataExpectedToChange);

		/**
		 * If dataExpectedToChange represents<br>
		 * assertThat(list1).hasSize(list2.size()); <br>
		 * then the new value which is assigned to dataExpectedToChange will
		 * represent<br>
		 * assertThat(list1).hasSameSizeAs(list2);
		 */
		dataExpectedToChange = AssertionWithSizeAndLengthAnalyzer
			.findHasSameSizeAssertionData(dataExpectedToChange)
			.orElse(dataExpectedToChange);

		/**
		 * If dataExpectedToChange represents<br>
		 * assertThat(x).isEqualTo(0);<br>
		 * then the new value which is assigned to dataExpectedToChange will
		 * represent<br>
		 * assertThat(x).isZero();
		 */
		dataExpectedToChange = AssertionWithLiteralArgumentAnalyzer
			.findDataForAssertionWithLiteral(dataExpectedToChange)
			.orElse(dataExpectedToChange);

		if (dataExpectedToChange != initialAnalysisData.getInitialAnalysisChainData()) {
			/*
			 * Each successful analysis will return a new instance of {@link
			 * AssertJAssertThatWithAssertionData} and therefore this condition
			 * is true as soon as at least one of the analyzers in the chain has
			 * been successful.
			 */
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
		Expression assumedAssertThatArgument = ASTNodeUtil.findSingleInvocationArgument(assumedAssertThatInvocation)
			.orElse(null);
		if (assumedAssertThatArgument == null) {
			return Optional.empty();
		}

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
