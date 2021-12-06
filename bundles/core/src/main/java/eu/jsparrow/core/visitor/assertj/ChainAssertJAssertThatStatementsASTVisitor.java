package eu.jsparrow.core.visitor.assertj;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * This rule collects subsequent statements which contain invocations of
 * {@code org.assertj.core.api.Assertions.assertThat} where the argument of
 * {@code assertThat} must be the same object and replaces them by a statement
 * containing the corresponding invocation chain on the given object.
 * <p>
 * For example,
 * 
 * <pre>
 * assertThat(stringList).isNotNull();
 * assertThat(stringList).isNotEmpty();
 * </pre>
 * 
 * can be transformed to
 * 
 * <pre>
 * assertThat(stringList)
 * 	.isNotNull()
 * 	.isNotEmpty();
 * </pre>
 * 
 * @since 4.6.0
 *
 */
public class ChainAssertJAssertThatStatementsASTVisitor extends AbstractASTRewriteASTVisitor {

	public static final String ORG_ASSERTJ_CORE_API_ASSERTIONS = "org.assertj.core.api.Assertions"; //$NON-NLS-1$
	private final ASTMatcher astMatcher = new ASTMatcher();

	@Override
	public boolean visit(Block block) {
		collectTransformationData(block).forEach(data -> transform(block, data));
		return true;
	}

	private List<TransformationData> collectTransformationData(Block block) {
		List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
		List<TransformationData> transformationDataList = new ArrayList<>();
		int i = 0;
		while (i < statements.size()) {
			Statement firstStatement = statements.get(i);
			List<Statement> followingStatements = statements.subList(i + 1, statements.size());
			TransformationData transformationData = findTransformationData(firstStatement,
					followingStatements).orElse(null);
			if (transformationData != null) {
				i += transformationData.getAssertJAssertThatStatementsToRemove()
					.size();
				transformationDataList.add(transformationData);
			} else {
				i += 1;
			}
		}
		return transformationDataList;
	}

	private Optional<InvocationChainData> findInvocationChainData(Statement statement) {

		if (statement.getNodeType() != ASTNode.EXPRESSION_STATEMENT) {
			return Optional.empty();
		}
		ExpressionStatement expressionStatement = (ExpressionStatement) statement;

		Expression expression = expressionStatement.getExpression();
		if (expression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return Optional.empty();
		}
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		List<MethodInvocation> invocationChainElements = collectInvocationChainElements(methodInvocation);

		int chainElementsCount = invocationChainElements.size();
		if (chainElementsCount < 2) {
			return Optional.empty();
		}

		MethodInvocation leftMostInvocation = invocationChainElements.get(0);
		List<MethodInvocation> subsequentInvocations = invocationChainElements.subList(1, chainElementsCount);

		return Optional.of(new InvocationChainData(
				leftMostInvocation, subsequentInvocations, expressionStatement));
	}

	private boolean hasSupportedAssertionChain(InvocationChainData invocationChainStatementData) {
		List<MethodInvocation> chainFollowingAssertThat = invocationChainStatementData.getSubsequentInvocations();
		return chainFollowingAssertThat.stream()
			.allMatch(SupportedAssertJAssertions::isSupportedAssertJAssertion);
	}

	private Optional<TransformationData> findTransformationData(Statement firstStatement,
			List<Statement> followingStatements) {

		InvocationChainData firstInvocationChain = findInvocationChainData(
				firstStatement).orElse(null);
		if (firstInvocationChain == null) {
			return Optional.empty();
		}
		MethodInvocation firstAssertThatInvocation = AssertThatInvocationAnalyzer
			.findSupportedAssertThatInvocation(firstInvocationChain)
			.orElse(null);
		if (firstAssertThatInvocation == null) {
			return Optional.empty();
		}
		if (!hasSupportedAssertionChain(firstInvocationChain)) {
			return Optional.empty();
		}

		List<InvocationChainData> subsequentInvocationChains = new ArrayList<>();
		for (int i = 0; i < followingStatements.size(); i++) {
			Statement statement = followingStatements.get(i);
			InvocationChainData subsequentInvocationChain = findInvocationChainData(statement)
				.filter(data -> this.astMatcher.match(
						firstAssertThatInvocation, data.getLeftMostInvocation()))
				.filter(this::hasSupportedAssertionChain)
				.orElse(null);
			if (subsequentInvocationChain != null) {
				subsequentInvocationChains.add(subsequentInvocationChain);
			} else {
				break;
			}
		}

		if (subsequentInvocationChains.isEmpty()) {
			return Optional.empty();
		}

		return Optional
			.of(createTransformationData(firstInvocationChain, subsequentInvocationChains));
	}

	private TransformationData createTransformationData(
			InvocationChainData firstAssertJAssertThatStatementData,
			List<InvocationChainData> subsequentDataOnSameObject) {

		ExpressionStatement firstAssertThatStatement = firstAssertJAssertThatStatementData
			.getInvocationChainStatement();
		MethodInvocation assertThatInvocation = firstAssertJAssertThatStatementData.getLeftMostInvocation();

		List<InvocationChainData> listOfAllAssertThatData = new ArrayList<>();
		listOfAllAssertThatData.add(firstAssertJAssertThatStatementData);
		listOfAllAssertThatData.addAll(subsequentDataOnSameObject);

		List<ExpressionStatement> statementsToRemove = listOfAllAssertThatData.stream()
			.map(InvocationChainData::getInvocationChainStatement)
			.collect(Collectors.toList());

		List<MethodInvocation> invocationChainElementList = listOfAllAssertThatData.stream()
			.map(InvocationChainData::getSubsequentInvocations)
			.flatMap(List<MethodInvocation>::stream)
			.collect(Collectors.toList());

		return new TransformationData(firstAssertThatStatement, statementsToRemove, assertThatInvocation,
				invocationChainElementList);
	}

	private List<MethodInvocation> collectInvocationChainElements(MethodInvocation methodInvocation) {
		List<MethodInvocation> chainElements = new ArrayList<>();
		chainElements.add(methodInvocation);
		Expression expression = methodInvocation.getExpression();
		while (expression != null) {
			if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
				MethodInvocation leftHandSideChainElement = (MethodInvocation) expression;
				chainElements.add(0, leftHandSideChainElement);
				expression = leftHandSideChainElement.getExpression();
			} else {
				break;
			}
		}
		return chainElements;
	}

	private void transform(Block block, TransformationData data) {
		MethodInvocation newChain = createNewMethodInvocationChain1(data.getAssertThatInvocation(),
				data.getInvocationChainElementList());
		AST ast = astRewrite.getAST();
		ExpressionStatement newExpressionStatement = ast.newExpressionStatement(newChain);
		ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		listRewrite.insertBefore(newExpressionStatement, data.getFirstAssertThatStatement(), null);
		data.getAssertJAssertThatStatementsToRemove()
			.forEach(statement -> astRewrite.remove(statement, null));
		onRewrite();
	}

	private MethodInvocation createNewMethodInvocationChain1(MethodInvocation assertThatInvocation,
			List<MethodInvocation> invocationChainElementList) {
		MethodInvocation chain = (MethodInvocation) astRewrite.createCopyTarget(assertThatInvocation);
		for (MethodInvocation chainElement : invocationChainElementList) {
			MethodInvocation newMethodInvocation = copyMethodInvocationWithoutExpression(chainElement);
			newMethodInvocation.setExpression(chain);
			chain = newMethodInvocation;
		}
		return chain;
	}

	@SuppressWarnings("unchecked")
	private MethodInvocation copyMethodInvocationWithoutExpression(MethodInvocation methodInvocation) {
		AST ast = astRewrite.getAST();
		MethodInvocation newMethodInvocation = ast.newMethodInvocation();
		newMethodInvocation.setName(ast.newSimpleName(methodInvocation.getName()
			.getIdentifier()));

		List<Expression> arguments = methodInvocation.arguments();
		List<Expression> newArguments = newMethodInvocation.arguments();
		arguments.stream()
			.map(argument -> (Expression) astRewrite.createCopyTarget(argument))
			.forEach(newArguments::add);
		return newMethodInvocation;
	}
}
