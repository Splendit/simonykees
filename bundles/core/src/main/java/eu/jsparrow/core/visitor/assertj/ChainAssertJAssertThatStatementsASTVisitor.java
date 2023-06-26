package eu.jsparrow.core.visitor.assertj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * This visitor collects consecutive AssertJ AsseertThat statements which target
 * the same object and chains them together to a single invocation chain.
 * <p>
 * AssertJ AsseertThat statements are introduced by an invocation of an
 * {@code assertThat} method defined in {@code org.assertj.core.api.Assertions}
 * and followed by AssertJ assertions.
 * <p>
 * For example, the two AssertJ AsseertThat statements
 * 
 * <pre>
 * assertThat(stringList).isNotNull();
 * assertThat(stringList).isNotEmpty();
 * </pre>
 * 
 * can be replaced by a single one like
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
	private static final ASTMatcher astMatcher = new ASTMatcher();

	@Override
	public boolean visit(Block block) {
		List<Statement> statements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
		List<TransformationData> transformationDataList = new ArrayList<>();
		int i = 0;
		while (i < statements.size()) {
			Statement firstStatement = statements.get(i);
			List<Statement> subsequentStatements = statements.subList(i + 1, statements.size());
			TransformationData transformationData = findTransformationData(firstStatement, subsequentStatements)
				.orElse(null);
			if (transformationData != null) {
				i += transformationData.getAssertJAssertThatStatementsToRemove()
					.size();
				transformationDataList.add(transformationData);
			} else {
				i += 1;
			}
		}
		transformationDataList.forEach(data -> transform(block, data));
		return true;
	}

	private Optional<TransformationData> findTransformationData(Statement firstStatement,
			List<Statement> subsequentStatements) {

		InvocationChainData firstInvocationChainData = findInvocationChainData(firstStatement).orElse(null);
		if (firstInvocationChainData == null) {
			return Optional.empty();
		}
		FirstInvocationChainAnalysisResult assertThatInvocationData = analyzeFirstInvocationChain(
				firstInvocationChainData).orElse(null);

		if (assertThatInvocationData == null) {
			return Optional.empty();
		}
		MethodInvocation assertThatInvocation = assertThatInvocationData.getAssertThatInvocation();

		List<InvocationChainData> subsequentInvocationChainDataList = new ArrayList<>();
		for (int i = 0; i < subsequentStatements.size(); i++) {
			InvocationChainData invocationChainData = findInvocationChainData(subsequentStatements.get(i))
				.filter(data -> analyzeSubsequentInvocationChain(assertThatInvocationData, data))
				.orElse(null);

			if (invocationChainData != null) {
				subsequentInvocationChainDataList.add(invocationChainData);
			} else {
				break;
			}
		}
		if (subsequentInvocationChainDataList.isEmpty()) {
			return Optional.empty();
		}

		ExpressionStatement firstAssertThatStatement = firstInvocationChainData.getInvocationChainStatement();

		List<InvocationChainData> allInvocationChainDataList = new ArrayList<>();
		allInvocationChainDataList.add(firstInvocationChainData);
		allInvocationChainDataList.addAll(subsequentInvocationChainDataList);

		List<MethodInvocation> invocationChainElementList = allInvocationChainDataList.stream()
			.map(InvocationChainData::getSubsequentInvocations)
			.flatMap(List<MethodInvocation>::stream)
			.collect(Collectors.toList());

		List<ExpressionStatement> assertThatStatementsToReplace = allInvocationChainDataList.stream()
			.map(InvocationChainData::getInvocationChainStatement)
			.collect(Collectors.toList());

		return Optional
			.of(new TransformationData(firstAssertThatStatement, assertThatStatementsToReplace, assertThatInvocation,
					invocationChainElementList));
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

	private Optional<FirstInvocationChainAnalysisResult> analyzeFirstInvocationChain(
			InvocationChainData invocationChainData) {
		MethodInvocation assumedAssertThatInvocation = invocationChainData.getLeftMostInvocation();
		String assumedAssertThatMethodName = assumedAssertThatInvocation.getName()
			.getIdentifier();

		if (!SupportedAssertJAssertions.isSupportedAssertJAsserThatMethodName(assumedAssertThatMethodName)) {
			return Optional.empty();
		}
		Expression argument = ASTNodeUtil.findSingleInvocationArgument(assumedAssertThatInvocation)
			.orElse(null);

		if (argument == null || !isSupportedAssertThatArgumentStructure(argument)) {
			return Optional.empty();
		}

		IMethodBinding assumedAssertThatMethodBinding = assumedAssertThatInvocation.resolveMethodBinding();
		if (assumedAssertThatMethodBinding == null) {
			return Optional.empty();
		}

		ITypeBinding declaringClass = assumedAssertThatMethodBinding.getDeclaringClass();
		if (!SupportedAssertJAssertions.isSupportedAssertionsType(declaringClass)) {
			return Optional.empty();
		}

		if (!hasSupportedAssertionMethodNames(invocationChainData)) {
			return Optional.empty();
		}

		ITypeBinding assertThatReturnType = assumedAssertThatMethodBinding.getReturnType();
		List<ITypeBinding> assumedAssertionReturnTypes = findAssumedAssertionReturnTypes(invocationChainData);
		if (assumedAssertionReturnTypes.isEmpty()) {
			return Optional.empty();
		}

		ITypeBinding assumedFirstAssertionReturnType = assumedAssertionReturnTypes.get(0);
		List<ITypeBinding> assumedSubsequentAssertionReturnTypes = assumedAssertionReturnTypes.subList(1,
				assumedAssertionReturnTypes.size());

		if (!analyzeFirstAssertionReturnType(assertThatReturnType, assumedFirstAssertionReturnType)) {
			return Optional.empty();
		}

		if (!analyzeAssertionMethodReturnTypes(assumedFirstAssertionReturnType,
				assumedSubsequentAssertionReturnTypes)) {
			return Optional.empty();
		}

		return Optional
			.of(new FirstInvocationChainAnalysisResult(assumedAssertThatInvocation, assumedFirstAssertionReturnType));

	}

	private boolean isSupportedAssertThatArgumentStructure(Expression assertThatArgument) {

		return assertThatArgument.getNodeType() == ASTNode.SIMPLE_NAME
				|| assertThatArgument.getNodeType() == ASTNode.QUALIFIED_NAME
				|| (assertThatArgument.getNodeType() == ASTNode.FIELD_ACCESS
						&& isSupportedFieldAccess((FieldAccess) assertThatArgument))
				|| assertThatArgument.getNodeType() == ASTNode.THIS_EXPRESSION
				|| assertThatArgument.getNodeType() == ASTNode.SUPER_FIELD_ACCESS
				|| assertThatArgument.getNodeType() == ASTNode.NUMBER_LITERAL
				|| assertThatArgument.getNodeType() == ASTNode.CHARACTER_LITERAL
				|| assertThatArgument.getNodeType() == ASTNode.STRING_LITERAL
				|| assertThatArgument.getNodeType() == ASTNode.TYPE_LITERAL;
	}

	private boolean isSupportedFieldAccess(FieldAccess fieldAccess) {
		Expression fieldAccessExpression = fieldAccess.getExpression();

		if (fieldAccessExpression != null) {
			if (fieldAccessExpression.getNodeType() == ASTNode.FIELD_ACCESS) {
				return isSupportedFieldAccess((FieldAccess) fieldAccessExpression);
			}
			return fieldAccessExpression.getNodeType() == ASTNode.THIS_EXPRESSION ||
					fieldAccessExpression.getNodeType() == ASTNode.SUPER_FIELD_ACCESS;
		}
		return false;
	}

	private boolean analyzeSubsequentInvocationChain(FirstInvocationChainAnalysisResult firstChainAnalysisResult,
			InvocationChainData invocationChainData) {

		MethodInvocation assertThatInvocation = firstChainAnalysisResult.getAssertThatInvocation();
		if (astMatcher.match(assertThatInvocation, invocationChainData.getLeftMostInvocation())
				&& hasSupportedAssertionMethodNames(invocationChainData)) {

			ITypeBinding firstAssertionReturnType = firstChainAnalysisResult.getFirstAssertionReturnType();
			List<ITypeBinding> assumedAssertionReturnTypes = findAssumedAssertionReturnTypes(invocationChainData);
			return !assumedAssertionReturnTypes.isEmpty()
					&& analyzeAssertionMethodReturnTypes(firstAssertionReturnType, assumedAssertionReturnTypes);
		}
		return false;
	}

	private boolean hasSupportedAssertionMethodNames(InvocationChainData invocationChainData) {
		List<MethodInvocation> chainFollowingAssertThat = invocationChainData.getSubsequentInvocations();
		return chainFollowingAssertThat.stream()
			.map(MethodInvocation::getName)
			.map(SimpleName::getIdentifier)
			.allMatch(SupportedAssertJAssertions::isSupportedAssertJAssertionMethodName);
	}

	private List<ITypeBinding> findAssumedAssertionReturnTypes(InvocationChainData invocationChainData) {
		List<IMethodBinding> assertionMethodBindings = invocationChainData.getSubsequentInvocations()
			.stream()
			.map(MethodInvocation::resolveMethodBinding)
			.collect(Collectors.toList());

		if (assertionMethodBindings.stream()
			.anyMatch(Objects::isNull)) {
			return Collections.emptyList();
		}
		return assertionMethodBindings.stream()
			.map(IMethodBinding::getReturnType)
			.collect(Collectors.toList());
	}

	private boolean analyzeFirstAssertionReturnType(ITypeBinding assertThatReturnType,
			ITypeBinding assumedFirstAssertionReturnType) {

		ITypeBinding lhsNonParameterizedTypeErasure = getNonParameterizedTypeErasure(assertThatReturnType);
		ITypeBinding rhsNonParameterizedTypeErasure = getNonParameterizedTypeErasure(assumedFirstAssertionReturnType);
		return ClassRelationUtil.compareITypeBinding(lhsNonParameterizedTypeErasure, rhsNonParameterizedTypeErasure);

	}

	private boolean analyzeAssertionMethodReturnTypes(ITypeBinding expectedAssertionReturnType,
			List<ITypeBinding> assertionReturnTypes) {

		return assertionReturnTypes.stream()
			.allMatch(returnType -> ClassRelationUtil.compareITypeBinding(returnType, expectedAssertionReturnType));
	}

	private ITypeBinding getNonParameterizedTypeErasure(ITypeBinding typeBinding) {
		ITypeBinding erasure = typeBinding.getErasure();
		while (erasure.isParameterizedType()) {
			erasure = erasure.getErasure();
		}
		return erasure;
	}

	private void transform(Block block, TransformationData data) {
		MethodInvocation newChain = createNewMethodInvocationChain(data.getAssertThatInvocation(),
				data.getInvocationChainElementList());
		AST ast = astRewrite.getAST();
		ExpressionStatement newExpressionStatement = ast.newExpressionStatement(newChain);
		ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		listRewrite.insertBefore(newExpressionStatement, data.getFirstAssertThatStatement(), null);
		data.getAssertJAssertThatStatementsToRemove()
			.forEach(statement -> astRewrite.remove(statement, null));
		onRewrite();
	}

	private MethodInvocation createNewMethodInvocationChain(MethodInvocation assertThatInvocation,
			List<MethodInvocation> invocationChainElementList) {
		MethodInvocation chain = (MethodInvocation) astRewrite.createCopyTarget(assertThatInvocation);
		for (MethodInvocation chainElement : invocationChainElementList) {
			MethodInvocation newMethodInvocation = AssertionInvocationsUtil
				.copyMethodInvocationWithoutExpression(chainElement, astRewrite);
			newMethodInvocation.setExpression(chain);
			chain = newMethodInvocation;
		}
		return chain;
	}
}
