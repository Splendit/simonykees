package eu.jsparrow.core.visitor.security;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;

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

		return true;
	}

	@Override
	protected boolean hasRequiredName(MethodInvocation methodInvocation) {
		return CREATE_QUERY.equals(methodInvocation.getName()
			.getIdentifier());
	}

	@Override
	protected boolean hasRequiredMethodExpressionType(ITypeBinding methodExpressionTypeBinding) {
		return ClassRelationUtil.isInheritingContentOfTypes(methodExpressionTypeBinding, ENTITY_MANAGER_SINGLETON_LIST);
	}

	@Override
	protected boolean hasRequiredDeclaringClass(IMethodBinding methodBinding) {
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		return ClassRelationUtil.isContentOfType(declaringClass, ENTITY_MANAGER_QUALIFIED_NAME);
	}

}
