package eu.jsparrow.core.visitor.security;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * A query string in the language JPQL (Java Persistence Query Language) defined
 * by JPA may be constructed by concatenating string literals with user defined
 * expressions and this may cause vulnerabilities to injection attacks because
 * the user input may be interpreted as JPQL code.
 * <P>
 * This visitor looks for queries of the type javax.persistence.Query which are
 * created by the createQuery method of javax.persistence.EntityManager.
 * Vulnerable components found in the concatenation of the JPQL query string
 * concatenation are replaced by parameterizing, using the setParameter method
 * of javax.persistence.Query.
 * 
 * <pre>
 * Query jpqlQuery = entityManager.createQuery("Select order from Orders order where order.id = " + orderId); *
 * </pre>
 * 
 * is transformed to:
 * 
 * <pre>
 * Query jpqlQuery = entityManager.createQuery("Select order from Orders order where order.id = ?1 ");
 * jpqlQuery.setParameter(1, orderId);
 * </pre>
 * 
 * @since 3.18.0
 *
 */
public class UseParameterizedJPAQueryASTVisitor extends AbstractDynamicQueryASTVisitor {

	private static final String ENTITY_MANAGER_QUALIFIED_NAME = "javax.persistence.EntityManager"; //$NON-NLS-1$
	private static final List<String> ENTITY_MANAGER_SINGLETON_LIST = Collections
		.singletonList(ENTITY_MANAGER_QUALIFIED_NAME);
	private static final String CREATE_QUERY = "createQuery"; //$NON-NLS-1$

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		Expression queryMethodArgument = analyzeStatementExecuteQuery(methodInvocation);
		if (queryMethodArgument == null || queryMethodArgument.getNodeType() != ASTNode.INFIX_EXPRESSION) {
			return true;
		}

		InfixExpression infixExpression = (InfixExpression) queryMethodArgument;

		DynamicQueryComponentsStore componentStore = new DynamicQueryComponentsStore();

		componentStore.storeComponents(infixExpression);
		List<Expression> queryComponents = componentStore.getComponents();
		JPAQueryComponentsAnalyzer componentsAnalyzer = new JPAQueryComponentsAnalyzer(queryComponents);

		List<ReplaceableParameter> replaceableParameters = componentsAnalyzer.createReplaceableParameterList();
		if (replaceableParameters.isEmpty()) {
			return true;
		}

		SimpleName querySimpleName = findJPAQuerySimpleName(methodInvocation);
		if (querySimpleName == null) {
			return true;
		}
		Block surroundingBody = this.findSurroundingBody(methodInvocation);
		if (surroundingBody == null) {
			return true;
		}

		JPAQueryVariableAnalyzerASTVisitor queryVariableAnalyzerVisitor = new JPAQueryVariableAnalyzerASTVisitor(
				querySimpleName);

		surroundingBody.accept(queryVariableAnalyzerVisitor);

		if (queryVariableAnalyzerVisitor.isUnsafe()) {
			return true;
		}
		if (queryVariableAnalyzerVisitor.getExecutionInvocation() == null) {
			return true;
		}

		replaceQuery(replaceableParameters);
		List<ExpressionStatement> setParameterStatements = createSetParameterStatements(replaceableParameters,
				querySimpleName);
		addSetters(methodInvocation, setParameterStatements);
		onRewrite();
		return true;
	}

	private SimpleName findJPAQuerySimpleName(MethodInvocation methodInvocation) {
		SimpleName simpleQueryName = null;
		if (methodInvocation.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {

			VariableDeclarationFragment queryDeclarationFragment = (VariableDeclarationFragment) methodInvocation
				.getParent();
			simpleQueryName = queryDeclarationFragment.getName();

		} else if (methodInvocation.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) methodInvocation.getParent();
			if (assignment.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
				return null;
			}
			Expression leftHandSide = assignment.getLeftHandSide();
			if (leftHandSide.getNodeType() == ASTNode.SIMPLE_NAME) {
				simpleQueryName = (SimpleName) leftHandSide;
			}
		}
		if (simpleQueryName == null) {
			return null;
		}
		ITypeBinding queryTypeBinding = simpleQueryName.resolveTypeBinding();

		if (ClassRelationUtil.isContentOfType(queryTypeBinding, "javax.persistence.Query")) { //$NON-NLS-1$
			return simpleQueryName;
		}
		return null;
	}

	/**
	 * 
	 * @param methodInvocation
	 * @return true if the method name is {@code createQuery}.
	 */
	@Override
	protected boolean hasRequiredName(MethodInvocation methodInvocation) {
		return CREATE_QUERY.equals(methodInvocation.getName()
			.getIdentifier());
	}

	@Override
	protected boolean hasRequiredMethodExpressionType(ITypeBinding methodExpressionTypeBinding) {
		return ClassRelationUtil.isContentOfType(methodExpressionTypeBinding, ENTITY_MANAGER_QUALIFIED_NAME)
				|| ClassRelationUtil.isInheritingContentOfTypes(methodExpressionTypeBinding,
						ENTITY_MANAGER_SINGLETON_LIST);
	}

	@Override
	protected boolean hasRequiredDeclaringClass(IMethodBinding methodBinding) {
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		return ClassRelationUtil.isContentOfType(declaringClass, ENTITY_MANAGER_QUALIFIED_NAME);
	}

	@Override
	protected String getNewPreviousLiteralValue(ReplaceableParameter parameter) {
		String oldPrevious = parameter.getPrevious()
			.getLiteralValue();
		return oldPrevious + " ?" + parameter.getPosition(); //$NON-NLS-1$
	}

}
