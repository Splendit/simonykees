package eu.jsparrow.core.visitor.assertj;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;

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
		List<Statement> statements = ASTNodeUtil.convertToTypedList(node.statements(), Statement.class);
		List<TransformationData> transformationDataList = collectTransformationData(statements);
		// transformation does not work correctly
		// transformationDataList.forEach(this::transform);
		return true;
	}

	private void transform(TransformationData data) {

		data.getAssertThatInvocationReplacememnts()
			.forEach(replacementData -> {
				MethodInvocation assertThatInvocationToReplace = replacementData.getAssertThatInvocationToReplace();
				ASTNode assertThatInvocationReplacement = astRewrite
					.createMoveTarget(replacementData.getChainFromPreviousStatement());
				astRewrite.replace(assertThatInvocationToReplace, assertThatInvocationReplacement, null);
			});

		data.getAssertThatStatementsToRemove()
			.forEach(statement -> astRewrite.remove(statement, null));
	}

	private List<TransformationData> collectTransformationData(List<Statement> statements) {
		List<TransformationData> transformationDataList = new ArrayList<>();
		int i = 0;
		while (i < statements.size()) {
			ArrayList<AssertJAssertThatStatementData> assertThatStatementDataOnSameObject = null;
			AssertJAssertThatStatementData assertJAssertThatStatementData = findAssertThatStatementData(
					statements.get(i)).orElse(null);
			if (assertJAssertThatStatementData != null) {
				String expectedArgumentIdentifier = assertJAssertThatStatementData.getAssertThatArgumentIdentifier();
				List<AssertJAssertThatStatementData> subsequentDataOnSameObject = findSubsequentAssertionDataOnSameObject(
						expectedArgumentIdentifier, i + 1, statements);
				if (!subsequentDataOnSameObject.isEmpty()) {
					assertThatStatementDataOnSameObject = new ArrayList<>();
					assertThatStatementDataOnSameObject.add(assertJAssertThatStatementData);
					assertThatStatementDataOnSameObject.addAll(subsequentDataOnSameObject);
				}
			}

			if (assertThatStatementDataOnSameObject != null) {
				i += assertThatStatementDataOnSameObject.size();
				transformationDataList.add(createTransformationData(assertThatStatementDataOnSameObject));
			} else {
				i += 1;
			}

		}
		return transformationDataList;
	}

	private List<AssertJAssertThatStatementData> findSubsequentAssertionDataOnSameObject(
			String expectedArgumentIdentifier,
			final int beginIndex, List<Statement> statements) {

		List<AssertJAssertThatStatementData> subsequentAssertionDataList = new ArrayList<>();
		for (int i = beginIndex; i < statements.size(); i++) {
			Statement statement = statements.get(i);
			AssertJAssertThatStatementData assertJAssertThatStatementData = findAssertThatStatementData(statement)
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
		MethodInvocation leftMostChainElement = findLeftMostChainElement(methodInvocation);
		if (methodInvocation == leftMostChainElement) {
			return Optional.empty();
		}

		if (!"assertThat".equals(leftMostChainElement.getName() //$NON-NLS-1$
			.getIdentifier())) {
			return Optional.empty();
		}

		List<SimpleName> simpleNameArguments = ASTNodeUtil.returnTypedList(leftMostChainElement.arguments(),
				SimpleName.class);
		if (simpleNameArguments.size() != 1) {
			return Optional.empty();
		}

		IMethodBinding methodBinding = leftMostChainElement.resolveMethodBinding();
		if (methodBinding == null) {
			return Optional.empty();
		}
		if (!ClassRelationUtil.isContentOfType(methodBinding.getDeclaringClass(), ORG_ASSERTJ_CORE_API_ASSERTIONS)) {
			return Optional.empty();
		}

		String assertThatArgumentIdentifier = simpleNameArguments.get(0)
			.getIdentifier();

		AssertJAssertThatStatementData assertJAssertThatStatementData = new AssertJAssertThatStatementData(
				leftMostChainElement, assertThatArgumentIdentifier, methodInvocation, expressionStatement);

		return Optional.of(assertJAssertThatStatementData);
	}

	private MethodInvocation findLeftMostChainElement(MethodInvocation methodInvocation) {
		Expression expression = methodInvocation.getExpression();
		if (expression != null && expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
			return findLeftMostChainElement((MethodInvocation) expression);
		}
		return methodInvocation;
	}

	private TransformationData createTransformationData(
			List<AssertJAssertThatStatementData> assertThatStatementDataOnSameObject) {

		List<AssertThatInvocationReplacementData> assertThatInvocationReplacememnts = new ArrayList<>();
		List<ExpressionStatement> assertThatStatementsToRemove = new ArrayList<>();
		int iLast = assertThatStatementDataOnSameObject.size() - 1;
		for (int i = iLast; i >= 1; i--) {
			MethodInvocation assertThatInvocationToReplace = assertThatStatementDataOnSameObject.get(i)
				.getAssertThatInvocation();
			MethodInvocation chainFromPreviousStatement = assertThatStatementDataOnSameObject.get(i - 1)
				.getCompleteInvocationChain();
			ExpressionStatement assertThatStatementToRemove = assertThatStatementDataOnSameObject.get(i - 1)
				.getAssertThatStatement();
			assertThatStatementsToRemove.add(assertThatStatementToRemove);
			assertThatInvocationReplacememnts.add(
					new AssertThatInvocationReplacementData(assertThatInvocationToReplace, chainFromPreviousStatement));
		}
		return new TransformationData(assertThatInvocationReplacememnts, assertThatStatementsToRemove);
	}

}
