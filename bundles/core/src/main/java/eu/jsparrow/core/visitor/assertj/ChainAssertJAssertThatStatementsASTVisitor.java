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
		List<TransformationData_NEW> transformationDataList_NEW = collectTransformationData_NEW(node);
		transformationDataList_NEW.forEach(this::transform_NEW);
		return true;
	}

	private List<TransformationData_NEW> collectTransformationData_NEW(Block block) {
		List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
		List<TransformationData_NEW> transformationDataList = new ArrayList<>();
		int i = 0;
		while (i < statements.size()) {
			Statement firstStatement = statements.get(i);
			List<Statement> followingStatements = statements.subList(i + 1, statements.size());
			TransformationData_NEW transformationData = findTransformationData_NEW(block, firstStatement,
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

	private Optional<AssertJAssertThatStatementData_NEW> findAssertThatStatementData_NEW(Statement statement) {

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

		AssertJAssertThatStatementData_NEW assertJAssertThatStatementData = new AssertJAssertThatStatementData_NEW(
				assumedAssertThatInvocation, assertThatArgumentIdentifier, chainFollowingAssertThat,
				expressionStatement);

		return Optional.of(assertJAssertThatStatementData);
	}

	private List<AssertJAssertThatStatementData_NEW> findSubsequentAssertionDataOnSameObject_NEW(
			String expectedArgumentIdentifier, List<Statement> statements) {

		List<AssertJAssertThatStatementData_NEW> subsequentAssertionDataList = new ArrayList<>();
		for (int i = 0; i < statements.size(); i++) {
			Statement statement = statements.get(i);
			AssertJAssertThatStatementData_NEW assertJAssertThatStatementData = findAssertThatStatementData_NEW(
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

	private Optional<TransformationData_NEW> findTransformationData_NEW(Block block, Statement firstStatement,
			List<Statement> followingStatements) {

		AssertJAssertThatStatementData_NEW firstAssertJAssertThatStatementData = findAssertThatStatementData_NEW(
				firstStatement).orElse(null);
		if (firstAssertJAssertThatStatementData == null) {
			return Optional.empty();
		}

		String expectedArgumentIdentifier = firstAssertJAssertThatStatementData
			.getAssertThatArgumentIdentifier();
		List<AssertJAssertThatStatementData_NEW> subsequentDataOnSameObject = findSubsequentAssertionDataOnSameObject_NEW(
				expectedArgumentIdentifier, followingStatements);

		if (subsequentDataOnSameObject.isEmpty()) {
			return Optional.empty();
		}

		ExpressionStatement firstAssertThatStatement = firstAssertJAssertThatStatementData
			.getAssertThatStatement();

		List<ExpressionStatement> statementsToRemove = new ArrayList<>();
		statementsToRemove.add(firstAssertThatStatement);

		List<ExpressionStatement> subsequentStatementsToRemove = subsequentDataOnSameObject.stream()
			.map(AssertJAssertThatStatementData_NEW::getAssertThatStatement)
			.collect(Collectors.toList());

		statementsToRemove.addAll(subsequentStatementsToRemove);

		return Optional
			.of(createTransformationData_NEW(block, firstAssertJAssertThatStatementData, subsequentDataOnSameObject));
	}

	private TransformationData_NEW createTransformationData_NEW(Block block,
			AssertJAssertThatStatementData_NEW firstAssertJAssertThatStatementData,
			List<AssertJAssertThatStatementData_NEW> subsequentDataOnSameObject) {

		ExpressionStatement firstAssertThatStatement = firstAssertJAssertThatStatementData
			.getAssertThatStatement();
		MethodInvocation assertThatInvocation = firstAssertJAssertThatStatementData.getAssertThatInvocation();

		List<AssertJAssertThatStatementData_NEW> listOfAllAssertThatData = new ArrayList<>();
		listOfAllAssertThatData.add(firstAssertJAssertThatStatementData);
		listOfAllAssertThatData.addAll(subsequentDataOnSameObject);

		List<ExpressionStatement> statementsToRemove = listOfAllAssertThatData.stream()
			.map(AssertJAssertThatStatementData_NEW::getAssertThatStatement)
			.collect(Collectors.toList());

		List<InvocationChainElement> invocationChainElementList = listOfAllAssertThatData.stream()
			.map(AssertJAssertThatStatementData_NEW::getChainFollowingAssertThat)
			.flatMap(List<MethodInvocation>::stream)
			.map(this::createInvocationChainElement)
			.collect(Collectors.toList());

		return new TransformationData_NEW(block, firstAssertThatStatement, statementsToRemove, assertThatInvocation,
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

	private void transform_NEW(TransformationData_NEW data) {
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
