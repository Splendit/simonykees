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
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
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
	public boolean visit(Block node) {
		collectTransformationData(node).forEach(this::transform);
		return true;
	}

	private List<TransformationData> collectTransformationData(Block block) {
		List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
		List<TransformationData> transformationDataList = new ArrayList<>();
		int i = 0;
		while (i < statements.size()) {
			Statement firstStatement = statements.get(i);
			List<Statement> followingStatements = statements.subList(i + 1, statements.size());
			TransformationData transformationData = findTransformationData(block, firstStatement,
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

		List<SimpleName> simpleNameArguments = ASTNodeUtil.returnTypedList(assumedAssertThatInvocation.arguments(),
				SimpleName.class);
		if (simpleNameArguments.size() != 1) {
			return Optional.empty();
		}

		IMethodBinding methodBinding = assumedAssertThatInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return Optional.empty();
		}
		if (!ClassRelationUtil.isContentOfType(methodBinding.getDeclaringClass(), ORG_ASSERTJ_CORE_API_ASSERTIONS)) {
			return Optional.empty();
		}

		String assertThatArgumentIdentifier = simpleNameArguments.get(0)
			.getIdentifier();

		AssertJAssertThatStatementData assertJAssertThatStatementData = new AssertJAssertThatStatementData(
				assumedAssertThatInvocation, assertThatArgumentIdentifier, chainFollowingAssertThat,
				expressionStatement);

		return Optional.of(assertJAssertThatStatementData);
	}

	private List<AssertJAssertThatStatementData> findSubsequentAssertionDataOnSameObject(
			String expectedArgumentIdentifier, List<Statement> statements) {

		List<AssertJAssertThatStatementData> subsequentAssertionDataList = new ArrayList<>();
		for (int i = 0; i < statements.size(); i++) {
			Statement statement = statements.get(i);
			AssertJAssertThatStatementData assertJAssertThatStatementData = findAssertThatStatementData(
					statement)
						.filter(data -> data.getAssertThatArgumentIdentifier()
							.equals(expectedArgumentIdentifier))
						.orElse(null);
			if (assertJAssertThatStatementData != null) {
				subsequentAssertionDataList.add(assertJAssertThatStatementData);
			} else {
				break;
			}
		}
		return subsequentAssertionDataList;
	}

	private Optional<TransformationData> findTransformationData(Block block, Statement firstStatement,
			List<Statement> followingStatements) {

		AssertJAssertThatStatementData firstAssertJAssertThatStatementData = findAssertThatStatementData(
				firstStatement).orElse(null);
		if (firstAssertJAssertThatStatementData == null) {
			return Optional.empty();
		}

		String expectedArgumentIdentifier = firstAssertJAssertThatStatementData
			.getAssertThatArgumentIdentifier();
		List<AssertJAssertThatStatementData> subsequentDataOnSameObject = findSubsequentAssertionDataOnSameObject(
				expectedArgumentIdentifier, followingStatements);

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
			.of(createTransformationData(block, firstAssertJAssertThatStatementData, subsequentDataOnSameObject));
	}

	private TransformationData createTransformationData(Block block,
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

		List<InvocationChainElement> invocationChainElementList = listOfAllAssertThatData.stream()
			.map(AssertJAssertThatStatementData::getChainFollowingAssertThat)
			.flatMap(List<MethodInvocation>::stream)
			.map(this::createInvocationChainElement)
			.collect(Collectors.toList());

		return new TransformationData(block, firstAssertThatStatement, statementsToRemove, assertThatInvocation,
				invocationChainElementList);
	}

	private InvocationChainElement createInvocationChainElement(MethodInvocation invocation) {
		String methodName = invocation.getName()
			.getIdentifier();
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(invocation.arguments(), Expression.class);
		return new InvocationChainElement(methodName, arguments);
	}

	private List<MethodInvocation> collectInvocationChainElements(MethodInvocation methodInvocation) {
		List<MethodInvocation> chainElements = new ArrayList<>();
		MethodInvocation chainElement = methodInvocation;
		while (chainElement != null) {
			chainElements.add(0, chainElement);
			chainElement = findLeftHandSideInvocation(chainElement).orElse(null);
		}

		return chainElements;
	}

	private Optional<MethodInvocation> findLeftHandSideInvocation(MethodInvocation methodInvocation) {
		Expression expression = methodInvocation.getExpression();
		if (expression != null && expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
			return Optional.of((MethodInvocation) expression);
		}
		return Optional.empty();
	}

	private void transform(TransformationData data) {
		MethodInvocation newChain = createNewMethodInvocationChain(data.getAssertThatInvocation(),
				data.getInvocationChainElementList());
		AST ast = astRewrite.getAST();
		ExpressionStatement newExpressionStatement = ast.newExpressionStatement(newChain);
		ListRewrite listRewrite = astRewrite.getListRewrite(data.getBlock(), Block.STATEMENTS_PROPERTY);
		listRewrite.insertBefore(newExpressionStatement, data.getFirstAssertThatStatement(), null);
		data.getAssertJAssertThatStatementsToRemove()
			.forEach(statement -> astRewrite.remove(statement, null));
		onRewrite();
	}

	private MethodInvocation createNewMethodInvocationChain(MethodInvocation assertThatInvocation,
			List<InvocationChainElement> invocationChainElementList) {
		MethodInvocation chain = (MethodInvocation) astRewrite.createCopyTarget(assertThatInvocation);
		for (InvocationChainElement chainElement : invocationChainElementList) {
			MethodInvocation newMethodInvocation = createNewMethodInvocation(chainElement);
			newMethodInvocation.setExpression(chain);
			chain = newMethodInvocation;
		}
		return chain;
	}

	@SuppressWarnings("unchecked")
	private MethodInvocation createNewMethodInvocation(InvocationChainElement invocationChainElement) {
		AST ast = astRewrite.getAST();
		MethodInvocation newMethodInvocation = ast.newMethodInvocation();
		newMethodInvocation.setName(ast.newSimpleName(invocationChainElement.getMethodName()));
		List<Expression> newMethodArguments = newMethodInvocation.arguments();
		invocationChainElement.getArguments()
			.forEach(argument -> {
				newMethodArguments.add((Expression) astRewrite.createCopyTarget(argument));
			});
		return newMethodInvocation;
	}
}
