package eu.jsparrow.core.visitor.security;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Intended to be extended by {@link org.eclipse.jdt.core.dom.ASTVisitor} classes which analyze SQL queries and
 * transform Java code in order to reduce vulnerability by injection of SQL code
 * by user input.
 * <p>
 * For example, a common functionality is the decision whether a class can be
 * imported or not.
 * 
 * @since 3.17.0
 *
 */
public abstract class AbstractDynamicQueryASTVisitor extends AbstractAddImportASTVisitor {

	protected static final String EXECUTE = "execute"; //$NON-NLS-1$
	protected static final String EXECUTE_QUERY = "executeQuery"; //$NON-NLS-1$

	protected boolean analyzeStatementExecuteQuery(MethodInvocation methodInvocation) {
		SimpleName methodName = methodInvocation.getName();
		if (!EXECUTE.equals(methodName.getIdentifier()) && !EXECUTE_QUERY.equals(methodName.getIdentifier())) {
			return false;
		}

		Expression methodExpression = methodInvocation.getExpression();
		if (methodExpression == null) {
			return false;
		}

		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (!ClassRelationUtil.isContentOfType(declaringClass, java.sql.Statement.class.getName())) {
			return false;
		}

		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.size() != 1) {
			return false;
		}

		Expression argument = arguments.get(0);
		ITypeBinding argumentTypeBinding = argument.resolveTypeBinding();
		boolean isString = ClassRelationUtil.isContentOfType(argumentTypeBinding, java.lang.String.class.getName());
		if (!isString) {
			return false;
		}

		if (argument.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}

		ITypeBinding methodExpressionTypeBinding = methodExpression.resolveTypeBinding();
		return ClassRelationUtil.isContentOfType(methodExpressionTypeBinding, java.sql.Statement.class.getName());
	}

	/**
	 * 
	 * @param methodInvocation
	 *            parameter which is examined whether or not it represents an
	 *            execute- or an executeQuery- invocation on a statement object.
	 * @return a SqlVariableAnalyzerVisitor if a query is found which can be
	 *         transformed, otherwise {@code null}.
	 */
	protected SqlVariableAnalyzerVisitor createSqlVariableAnalyzerVisitor(MethodInvocation methodInvocation) {
		boolean hasRightTypeAndName = analyzeStatementExecuteQuery(methodInvocation);
		if (!hasRightTypeAndName) {
			return null;
		}

		SimpleName query = (SimpleName) methodInvocation.arguments()
			.get(0);
		IBinding queryVariableBinding = query.resolveBinding();
		if (queryVariableBinding.getKind() != IBinding.VARIABLE) {
			return null;
		}

		IVariableBinding variableBinding = (IVariableBinding) queryVariableBinding;
		if (variableBinding.isField()) {
			return null;
		}

		ASTNode declaringNode = this.getCompilationUnit()
			.findDeclaringNode(queryVariableBinding);
		if (declaringNode == null || declaringNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return null;
		}

		SqlVariableAnalyzerVisitor sqlVariableVisitor = new SqlVariableAnalyzerVisitor(query, declaringNode,
				getCompilationUnit());
		Block enclosingBlock = ASTNodeUtil.getSpecificAncestor(declaringNode, Block.class);
		enclosingBlock.accept(sqlVariableVisitor);
		if (sqlVariableVisitor.isUnsafe()) {
			return null;
		}
		return sqlVariableVisitor;
	}
}
