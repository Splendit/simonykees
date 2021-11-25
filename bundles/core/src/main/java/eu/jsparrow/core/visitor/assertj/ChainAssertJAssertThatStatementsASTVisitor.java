package eu.jsparrow.core.visitor.assertj;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
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

	private Optional<AssertJAssertThatStatementData> findAssertThatStatementData(Statement statement) {

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

		MethodInvocation assumedAssertThatInvocation = invocationChainElements.get(0);
		List<MethodInvocation> chainFollowingAssertThat = invocationChainElements.subList(1, chainElementsCount);

		if (!"assertThat".equals(assumedAssertThatInvocation.getName() //$NON-NLS-1$
			.getIdentifier())) {
			return Optional.empty();
		}

		List<Expression> assertThatArguments = ASTNodeUtil.convertToTypedList(assumedAssertThatInvocation.arguments(),
				Expression.class);
		if (assertThatArguments.size() != 1) {
			return Optional.empty();
		}

		Expression assertThatArgument = assertThatArguments.get(0);

		IMethodBinding assumedAssertThatMethodBinding = assumedAssertThatInvocation.resolveMethodBinding();
		if (assumedAssertThatMethodBinding == null) {
			return Optional.empty();
		}

		if (!ClassRelationUtil.isContentOfType(assumedAssertThatMethodBinding.getDeclaringClass(),
				ORG_ASSERTJ_CORE_API_ASSERTIONS)) {
			return Optional.empty();
		}

		ITypeBinding assertThatInvocationTypeBinding = assumedAssertThatInvocation.resolveTypeBinding();
		ITypeBinding methodInvocationTypeBinding = methodInvocation.resolveTypeBinding();

		if (!ClassRelationUtil.compareITypeBinding(assertThatInvocationTypeBinding,
				methodInvocationTypeBinding)) {
			return Optional.empty();
		}

		AssertJAssertThatStatementData assertJAssertThatStatementData = new AssertJAssertThatStatementData(
				assumedAssertThatInvocation, assertThatArgument, chainFollowingAssertThat,
				expressionStatement);

		return Optional.of(assertJAssertThatStatementData);
	}

	private List<AssertJAssertThatStatementData> findSubsequentAssertionDataOnSameObject(
			AssertThatArgumentMatcher argumentMatcher, List<Statement> statements) {

		List<AssertJAssertThatStatementData> subsequentAssertionDataList = new ArrayList<>();
		for (int i = 0; i < statements.size(); i++) {
			Statement statement = statements.get(i);
			AssertJAssertThatStatementData assertJAssertThatStatementData = findAssertThatStatementData(
					statement)
						.filter(argumentMatcher::isMatchingAssertThatArgument)
						.orElse(null);
			if (assertJAssertThatStatementData != null) {
				subsequentAssertionDataList.add(assertJAssertThatStatementData);
			} else {
				break;
			}
		}
		return subsequentAssertionDataList;
	}

	private Optional<TransformationData> findTransformationData(Statement firstStatement,
			List<Statement> followingStatements) {

		AssertJAssertThatStatementData firstAssertJAssertThatStatementData = findAssertThatStatementData(
				firstStatement).orElse(null);
		if (firstAssertJAssertThatStatementData == null) {
			return Optional.empty();
		}

		AssertThatArgumentMatcher argumentMatcher = AssertThatArgumentMatcher
			.findAssertThatArgumentMatcher(firstAssertJAssertThatStatementData)
			.orElse(null);

		if (argumentMatcher == null) {
			return Optional.empty();
		}

		List<AssertJAssertThatStatementData> subsequentDataOnSameObject = findSubsequentAssertionDataOnSameObject(
				argumentMatcher, followingStatements);

		if (subsequentDataOnSameObject.isEmpty()) {
			return Optional.empty();
		}

		ExpressionStatement firstAssertThatStatement = firstAssertJAssertThatStatementData
			.getAssertThatStatement();

		List<ExpressionStatement> statementsToRemove = new ArrayList<>();
		statementsToRemove.add(firstAssertThatStatement);

		List<ExpressionStatement> subsequentStatementsToRemove = subsequentDataOnSameObject.stream()
			.map(AssertJAssertThatStatementData::getAssertThatStatement)
			.collect(Collectors.toList());

		statementsToRemove.addAll(subsequentStatementsToRemove);

		return Optional
			.of(createTransformationData(firstAssertJAssertThatStatementData, subsequentDataOnSameObject));
	}

	private TransformationData createTransformationData(
			AssertJAssertThatStatementData firstAssertJAssertThatStatementData,
			List<AssertJAssertThatStatementData> subsequentDataOnSameObject) {

		ExpressionStatement firstAssertThatStatement = firstAssertJAssertThatStatementData
			.getAssertThatStatement();
		MethodInvocation assertThatInvocation = firstAssertJAssertThatStatementData.getAssertThatInvocation();

		List<AssertJAssertThatStatementData> listOfAllAssertThatData = new ArrayList<>();
		listOfAllAssertThatData.add(firstAssertJAssertThatStatementData);
		listOfAllAssertThatData.addAll(subsequentDataOnSameObject);

		List<ExpressionStatement> statementsToRemove = listOfAllAssertThatData.stream()
			.map(AssertJAssertThatStatementData::getAssertThatStatement)
			.collect(Collectors.toList());

		List<MethodInvocation> invocationChainElementList = listOfAllAssertThatData.stream()
			.map(AssertJAssertThatStatementData::getChainFollowingAssertThat)
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
