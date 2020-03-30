package eu.jsparrow.core.visitor.security;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * 
 * @since 3.16.0
 *
 */
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
		if(queryVariableBinding.getKind() != IBinding.VARIABLE) {
			return true;
		}
		
		ASTNode declaringNode = this.getCompilationUnit().findDeclaringNode(queryVariableBinding);
		if(declaringNode == null || declaringNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return true;
		}
		SqlVariableAnalyzerVisitor visitor = new SqlVariableAnalyzerVisitor(query, declaringNode, getCompilationUnit());
		Block enclosingBlock = ASTNodeUtil.getSpecificAncestor(declaringNode, Block.class);
		enclosingBlock.accept(visitor);
		
		List<Expression> queryComponents = visitor.getDynamicQueryComponents();
		QueryComponentsAnalyzer componentsAnalyzer = new QueryComponentsAnalyzer(queryComponents);
		componentsAnalyzer.analyze();
		List<ReplaceableParameter>replaceableParameters = componentsAnalyzer.getReplaceableParameters();
		if(replaceableParameters.isEmpty()) {
			return true;
		}
		
		/*
		 * Find the declaration of the SQL statement. 
		 * Make sure the initialization is a connection.createStatement()
		 * 
		 */
		
		if(methodExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return true;
		}
		SimpleName sqlStatement = (SimpleName)methodExpression;
		ASTNode stmDeclaration = getCompilationUnit().findDeclaringNode(sqlStatement.resolveBinding());
		SqlStatementAnalyzerVisitor sqlStatementVisitor = new SqlStatementAnalyzerVisitor(stmDeclaration, sqlStatement, getCompilationUnit());
		Block stmDeclarationBlock = ASTNodeUtil.getSpecificAncestor(stmDeclaration, Block.class);
		stmDeclarationBlock.accept(sqlStatementVisitor);
		if(sqlStatementVisitor.isUnsafe()) {
			return true;
		}
		Expression connectionCreateStatement = sqlStatementVisitor.getInitializer();
		if(connectionCreateStatement == null) {
			return true;
		}
		
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
		
		/*
		 * Replace old query with the new one.
		 */
		replaceQuery(replaceableParameters);
		
		return true;
	}

	private void replaceQuery(List<ReplaceableParameter> replaceableParameters) {
		AST ast = astRewrite.getAST();
		for(ReplaceableParameter parameter : replaceableParameters) {
			StringLiteral previous = parameter.getPrevious();
			StringLiteral next = parameter.getNext();
			Expression compoExpression = parameter.getParameter();
			String oldPrevious = previous.getLiteralValue();
			String newPrevious = oldPrevious.substring(0, oldPrevious.length()-1) + "?";
			StringLiteral newPreviousLiteral = ast.newStringLiteral();
			newPreviousLiteral.setLiteralValue(newPrevious);
			astRewrite.replace(previous, newPreviousLiteral, null);
			
			String newNext = next.getLiteralValue().replaceFirst("'", "");
			StringLiteral newNextLiteral = ast.newStringLiteral();
			newNextLiteral.setLiteralValue(newNext);
			astRewrite.replace(next, newNextLiteral, null);
			
			astRewrite.remove(compoExpression, null);
			//TODO: doulblecheck the side effects of removing the component. 
		}
		
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
		if(!"createStatement".equals(createStatement.getIdentifier())) { //$NON-NLS-1$
			return false;
		}
		return sqlStatementInitializer.arguments().isEmpty();
		
	}

}
