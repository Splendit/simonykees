package eu.jsparrow.core.visitor.security;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class ReplaceDynamicQueryByPreparedStatementASTVisitor extends AbstractASTRewriteASTVisitor {
	
	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		
		SimpleName methodName = methodInvocation.getName();
		if(!"execute".equals(methodName.getIdentifier()) && !"executeQuery".equals(methodName.getIdentifier())) { //$NON-NLS-1$ //$NON-NLS-2$
			return true;
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if(!ClassRelationUtil.isContentOfType(declaringClass, java.sql.Statement.class.getName())) {
			return true;
		}
		
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if(arguments.size() != 1) {
			return true;
		}
		
		Expression argument = arguments.get(0);
		ITypeBinding argumentTypeBinding = argument.resolveTypeBinding();
		boolean isString = ClassRelationUtil.isContentOfType(argumentTypeBinding, java.lang.String.class.getName());
		if(!isString) {
			return true;
		}
		
		Expression methodExpression = methodInvocation.getExpression();
		if(methodExpression == null) {
			return true;
		}
		ITypeBinding methodExpressionTypeBinding = methodExpression.resolveTypeBinding();
		boolean isSqlStatement = ClassRelationUtil.isContentOfType(methodExpressionTypeBinding, java.sql.Statement.class.getName());
		if(!isSqlStatement) {
			return true;
		}
		
		
		//TODO: analyze the parameter
		/*
		 * 1. The argument has to be a simple name representing a local variable. 
		 * 2. Find the declaration of the argument
		 * 3. Find the initialization. 
		 * 4. Make sure the initialization is a concatenation of strings (string literals + expressions)
		 * 5. Analyze the sql parts. Get the expression representing parameters on the rhs of WHERE
		 * 6. Store each parameter on an indexed map
		 * 
		 * 
		 */
		//TODO: make sure the query variable is not used in other places 
		if(argument.getNodeType() != ASTNode.SIMPLE_NAME) {
			return true;
		}
		SimpleName query = (SimpleName)argument;
		IBinding queryVariableBinding = query.resolveBinding();
		ASTNode declaringNode = this.getCompilationUnit().findDeclaringNode(queryVariableBinding);
		
		if(declaringNode == null || declaringNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return true;
		}
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)declaringNode;
		Expression initializer = fragment.getInitializer();
		if(initializer == null) {
			// TODO: do we want to check if the variable is assigned afterwards?
			return true;
		}
		if(initializer.getNodeType() != ASTNode.INFIX_EXPRESSION) {
			return true;
		}
		//TODO: create a class which analyzes the infix expression. extracts the arguments and is able to produce the new string with placeholders.
		
		
		//TODO: analyze the method invocation expression
		/*
		 * Find the declaration of the sql statement. 
		 * Make sure the initialization is a connection.createStatement();
		 * 
		 */
		
		if(methodExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return true;
		}
		SimpleName sqlStatmement = (SimpleName)methodExpression;
		ASTNode stmDeclaration = getCompilationUnit().findDeclaringNode(sqlStatmement.resolveBinding());
		if(stmDeclaration.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return false;
		}
		VariableDeclarationFragment sqlStatementDeclarationFragment = (VariableDeclarationFragment)stmDeclaration;
		Expression connectionCreateStatement = sqlStatementDeclarationFragment.getInitializer();
		
		boolean isConnectionPrepareStatement = analyzeSqlStatementInitializer(connectionCreateStatement);
		if(!isConnectionPrepareStatement) {
			return false;
		}
		
		// TODO: analyze the next references of statement. 
		/*
		 * Is there any getResultSet() executed?
		 * 
		 */
		/*
		 * If the method invocation is executeQuery(String) then we just need to 
		 * remove the string parameter. 
		 * Otherwise, if the method invocation is execute() then: 
		 * 		1. We need to make sure the result of the execute is discarded. 
		 * 			(It returns a boolean value)
		 * 		2. We need to search for invocations of statemetn.getResultSet(). 
		 * 		If that is ever invoked, we have to move the declaration of the resultset to 
		 * 		the new statement.executeQuery() method.  
		 */
		
		return true;
	}

	private boolean analyzeSqlStatementInitializer(Expression sqlStatementInitializerExpression) {
		if(sqlStatementInitializerExpression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return false;
		}
		MethodInvocation sqlStatementInitializer = (MethodInvocation)sqlStatementInitializerExpression; 
		Expression connection = sqlStatementInitializer.getExpression();
		ITypeBinding connectionTypeBinding = connection.resolveTypeBinding();
		if(!ClassRelationUtil.isContentOfType(connectionTypeBinding, java.sql.Connection.class.getName())) {
			return false;
		}
		SimpleName createStatement = sqlStatementInitializer.getName();
		if(!"createStatement".equals(createStatement.getIdentifier())) {
			return false;
		}
		return sqlStatementInitializer.arguments().isEmpty();
		
	}

}
