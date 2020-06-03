package eu.jsparrow.core.visitor.security;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Replaces string concatenations in dynamic JPQL queries which contain user
 * input by parameterizing the given query in order to remove injection
 * vulnerabilities.
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

		if (componentsAnalyzer.getWhereKeywordPosition() < 0) {
			return true;
		}
		componentsAnalyzer.analyze();
		List<ReplaceableParameter> replaceableParameters = componentsAnalyzer.getReplaceableParameters();
		if (replaceableParameters.isEmpty()) {
			return true;
		}

		if (methodInvocation.getLocationInParent() != VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			return true;
		}

		VariableDeclarationFragment queryDeclarationFragment = (VariableDeclarationFragment) methodInvocation
			.getParent();

		if (queryDeclarationFragment.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			return true;
		}

		VariableDeclarationStatement queryDeclarationStatement = (VariableDeclarationStatement) queryDeclarationFragment
			.getParent();

		if (queryDeclarationStatement.fragments()
			.size() != 1) {
			return true;
		}

		ITypeBinding queryTypeBinding = queryDeclarationStatement.getType()
			.resolveBinding();

		if (!ClassRelationUtil.isContentOfType(queryTypeBinding, "javax.persistence.Query")) { //$NON-NLS-1$
			return true;
		}

		SimpleName querySimpleName = queryDeclarationFragment.getName();

		replaceQuery(replaceableParameters);
		List<ExpressionStatement> setParameterStatements = createSetParameterStatements(replaceableParameters,
				querySimpleName);
		addSetters(methodInvocation, setParameterStatements);
		onRewrite();
		return true;
	}

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
