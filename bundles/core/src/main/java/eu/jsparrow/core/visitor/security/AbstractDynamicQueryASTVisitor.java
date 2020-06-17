package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Intended to be extended by {@link org.eclipse.jdt.core.dom.ASTVisitor}
 * classes which analyze SQL queries and transform Java code in order to reduce
 * vulnerability by injection of SQL code by user input.
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

	/**
	 * 
	 * @param methodInvocation
	 * @return the expression representing the argument if the method invocation
	 *         has exactly one {@link String} argument, otherwise null.
	 */
	protected Expression getStringExpressionAsTheOnlyArgument(MethodInvocation methodInvocation) {
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		if (arguments.size() != 1) {
			return null;
		}

		Expression argument = arguments.get(0);
		ITypeBinding argumentTypeBinding = argument.resolveTypeBinding();
		boolean isString = ClassRelationUtil.isContentOfType(argumentTypeBinding, java.lang.String.class.getName());
		if (!isString) {
			return null;
		}
		return argument;
	}

	/**
	 * 
	 * @param methodInvocation
	 * @return true if the method name is either {@code execute} or
	 *         {@code executeQuery}
	 */
	protected boolean hasRequiredName(MethodInvocation methodInvocation) {
		String identifier = methodInvocation.getName()
			.getIdentifier();
		return EXECUTE.equals(identifier) || EXECUTE_QUERY.equals(identifier);
	}

	protected boolean hasRequiredMethodExpressionType(ITypeBinding methodExpressionTypeBinding) {
		return ClassRelationUtil.isContentOfType(methodExpressionTypeBinding, java.sql.Statement.class.getName());
	}

	protected boolean hasRequiredDeclaringClass(IMethodBinding methodBinding) {
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		return ClassRelationUtil.isContentOfType(declaringClass, java.sql.Statement.class.getName());
	}

	protected Expression analyzeStatementExecuteQuery(MethodInvocation methodInvocation) {
		if (!hasRequiredName(methodInvocation)) {
			return null;
		}

		Expression methodExpression = methodInvocation.getExpression();
		if (methodExpression == null) {
			return null;
		}

		boolean hasRequiredMethodExpressionType = hasRequiredMethodExpressionType(
				methodExpression.resolveTypeBinding());
		if (!hasRequiredMethodExpressionType) {
			return null;
		}

		if (!hasRequiredDeclaringClass(methodInvocation.resolveMethodBinding())) {
			return null;
		}

		return getStringExpressionAsTheOnlyArgument(methodInvocation);
	}

	/**
	 * 
	 * @param queryMethodArgument
	 *            parameter which is examined whether or not it is a local
	 *            variable storing an SQL query.
	 * @return a SqlVariableAnalyzerVisitor if a query is found which can be
	 *         transformed, otherwise {@code null}.
	 */
	protected SqlVariableAnalyzerVisitor createSqlVariableAnalyzerVisitor(Expression queryMethodArgument) {

		if (queryMethodArgument == null) {
			return null;
		}

		if (queryMethodArgument.getNodeType() != ASTNode.SIMPLE_NAME) {
			return null;
		}

		SimpleName query = (SimpleName) queryMethodArgument;

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

	/**
	 * @return by default the old literal value of the previous string literal
	 *         unless this method is overridden in order to return a different
	 *         value.
	 */
	protected String getNewPreviousLiteralValue(ReplaceableParameter parameter) {
		return parameter.getPrevious()
			.getLiteralValue();
	}

	/**
	 * @return by default the old literal value of the next string literal
	 *         unless this method is overridden in order to return a different
	 *         value.
	 */
	protected String getNewNextLiteralValue(ReplaceableParameter parameter) {
		return parameter.getNext()
			.getLiteralValue();
	}

	private void replaceStringLiteral(StringLiteral oldLiteral, String newLiteralValue) {
		if (newLiteralValue.equals(oldLiteral.getLiteralValue())) {
			return;
		}
		AST ast = astRewrite.getAST();
		StringLiteral newLiteral = ast.newStringLiteral();
		newLiteral.setLiteralValue(newLiteralValue);
		astRewrite.replace(oldLiteral, newLiteral, null);
	}

	protected void replaceQuery(List<ReplaceableParameter> replaceableParameters) {

		for (ReplaceableParameter parameter : replaceableParameters) {
			replaceStringLiteral(parameter.getPrevious(), getNewPreviousLiteralValue(parameter));

			StringLiteral next = parameter.getNext();
			if (next != null) {
				replaceStringLiteral(next, getNewNextLiteralValue(parameter));
			}

			Expression component = parameter.getParameter();
			if (component.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
				Assignment assignment = (Assignment) component.getParent();
				astRewrite.remove(assignment.getParent(), null);
			} else {
				astRewrite.remove(component, null);
			}
		}
	}

	protected List<ExpressionStatement> createSetParameterStatements(List<ReplaceableParameter> replaceableParameters,
			SimpleName statementName) {
		List<ExpressionStatement> statements = new ArrayList<>();
		AST ast = astRewrite.getAST();
		for (ReplaceableParameter parameter : replaceableParameters) {
			Expression component = parameter.getParameter();
			String setterName = parameter.getSetterName();
			SimpleName statementNameCopy = (SimpleName) astRewrite.createCopyTarget(statementName);
			MethodInvocation setter = ast.newMethodInvocation();
			setter.setExpression(statementNameCopy);
			setter.setName(ast.newSimpleName(setterName));
			int position = parameter.getPosition();
			NumberLiteral positionLiteral = ast.newNumberLiteral();
			positionLiteral.setToken(String.valueOf(position));
			Expression parameterExpression = (Expression) astRewrite.createCopyTarget(component);
			@SuppressWarnings("unchecked")
			List<Expression> setterArguments = setter.arguments();
			setterArguments.add(positionLiteral);
			setterArguments.add(parameterExpression);
			ExpressionStatement setterExpressionStatement = ast.newExpressionStatement(setter);
			statements.add(setterExpressionStatement);
		}
		return statements;
	}

	protected void addSetters(Expression initializer, List<ExpressionStatement> setParameterStatements) {
		Statement statement = ASTNodeUtil.getSpecificAncestor(initializer, Statement.class);
		Block block = (Block) statement.getParent();
		ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
		List<ExpressionStatement> setters = new ArrayList<>(setParameterStatements);
		Collections.reverse(setters);
		setters.forEach(setter -> listRewrite.insertAfter(setter, statement, null));
	}

	/**
	 * 
	 *
	 * @param methodInvocation
	 * @return This method looks for the next ancestor of the
	 *         {@link MethodInvocation} given by the parameter which is either a
	 *         {@link BodyDeclaration} or a {@link LambdaExpression}. If the
	 *         ancestor is a method is a declaration or an initializer or a
	 *         lambda expression with a body, then the corresponding body is
	 *         returned. Otherwise null is returned, for example, if the
	 *         ancestor found is a field declaration.
	 */
	protected Block findSurroundingBody(MethodInvocation methodInvocation) {

		BodyDeclaration bodyDeclaration = ASTNodeUtil.getSpecificAncestor(methodInvocation, BodyDeclaration.class);

		ASTNode parent = methodInvocation.getParent();
		while (parent != null) {
			if (parent == bodyDeclaration) {
				if (parent.getNodeType() == ASTNode.METHOD_DECLARATION) {
					MethodDeclaration method = (MethodDeclaration) parent;
					return method.getBody();
				} else if (parent.getNodeType() == ASTNode.INITIALIZER) {
					Initializer initializer = (Initializer) parent;
					return initializer.getBody();
				}
				return null;
			} else if (parent.getNodeType() == ASTNode.LAMBDA_EXPRESSION) {
				LambdaExpression lambda = (LambdaExpression) parent;
				ASTNode lambdaBody = lambda.getBody();
				if (lambdaBody.getNodeType() == ASTNode.BLOCK) {
					return (Block) lambdaBody;
				}
				return null;
			}
			parent = parent.getParent();
		}
		return null;

	}

}
