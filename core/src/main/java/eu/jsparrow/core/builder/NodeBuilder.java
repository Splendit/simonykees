package eu.jsparrow.core.builder;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WildcardType;

/**
 * Helper Class to generate new ASTNodes
 * 
 * @author Martin Huter
 * @since 0.9.0
 */
public class NodeBuilder {

	private NodeBuilder() {
		// util class
	}

	/**
	 * Creates an method invocation on an expression with
	 * 
	 * @param ast
	 *            the AbstractSyntaxTree thats the target of the node
	 * @param optionalExpression
	 *            target of the invocation
	 * @param name
	 *            is the name of the invoked method
	 * @param argument
	 *            argument of the invoked method
	 * @return returns a new method with fills with the parameters
	 */
	@SuppressWarnings("unchecked")
	public static MethodInvocation newMethodInvocation(AST ast, Expression optionalExpression, SimpleName name,
			Expression argument) {
		MethodInvocation resultMI = newMethodInvocation(ast, optionalExpression, name);
		resultMI.arguments().add(argument);
		return resultMI;
	}

	/**
	 * Creates an method invocation on an expression with
	 * 
	 * @param ast
	 *            the AbstractSyntaxTree thats the target of the node
	 * @param optionalExpression
	 *            target of the invocation
	 * @param name
	 *            is the name of the invoked method
	 * @param arguments
	 *            arguments of the invoked method
	 * @return returns a new method with fills with the parameters
	 */
	@SuppressWarnings("unchecked")
	public static MethodInvocation newMethodInvocation(AST ast, Expression optionalExpression, SimpleName name,
			List<Expression> arguments) {
		MethodInvocation resultMI = newMethodInvocation(ast, optionalExpression, name);
		resultMI.arguments().addAll(arguments);
		return resultMI;
	}

	/**
	 * Creates an method invocation on an expression with
	 * 
	 * @param ast
	 *            the AbstractSyntaxTree thats the target of the node
	 * @param optionalExpression
	 *            target of the invocation
	 * @param name
	 *            is the name of the invoked method
	 * @return returns a new method with fills with the parameters
	 */
	public static MethodInvocation newMethodInvocation(AST ast, Expression optionalExpression, SimpleName name) {
		MethodInvocation resultMI = ast.newMethodInvocation();
		resultMI.setExpression(optionalExpression);
		resultMI.setName(name);
		return resultMI;
	}

	/**
	 * 
	 * @param ast
	 *            the AbstractSyntaxTree thats the target of the node
	 * @param identifier
	 *            name that is used for the SimpleName
	 * @return an {@link SimpleName} that encapsulates the identifier
	 */
	public static SimpleName newSimpleName(AST ast, String identifier) {
		return ast.newSimpleName(identifier);
	}

	/**
	 * Generates an enhanced for Statement with the following parameters:
	 * for([expression]:[parameter]{[block]}
	 * 
	 * @param ast
	 *            return ast.newSimpleName(identifier); }
	 * 
	 *            /** Generates an enhanced for Statement with the following
	 *            parameters: for([expression]:[parameter]{[block]}
	 * 
	 * @param ast
	 *            the AbstractSyntaxTree thats the target of the node
	 * @param body
	 *            are the statements in of the for loop
	 * @param expression
	 *            iteration variable
	 * @param parameter
	 *            iteration base object
	 * @return an {@link EnhancedForStatement} that combines all the input
	 *         parameters
	 */
	public static EnhancedForStatement newEnhancedForStatement(AST ast, Statement body, Expression expression,
			SingleVariableDeclaration parameter) {
		EnhancedForStatement newFor = ast.newEnhancedForStatement();
		newFor.setBody(body);
		newFor.setExpression(expression);
		newFor.setParameter(parameter);
		return newFor;
	}

	/**
	 * 
	 * @param ast
	 *            the AbstractSyntaxTree thats the target of the node
	 * @param name
	 *            is the name of the generated variable
	 * @param variableType
	 *            is the type of the generated variable
	 * @return {@link SingleVariableDeclaration} with the name as identifier
	 *         name and variableType as object type
	 */
	public static SingleVariableDeclaration newSingleVariableDeclaration(AST ast, SimpleName name, Type variableType) {
		SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
		svd.setName(name);
		svd.setType(variableType);
		return svd;
	}

	@SuppressWarnings("unchecked")
	public static FieldDeclaration newFieldDeclaration(AST ast, Type type, VariableDeclarationFragment serialUidNode,
			List<ASTNode> newModifier) {
		FieldDeclaration fieldDeclaration = ast.newFieldDeclaration(serialUidNode);
		fieldDeclaration.setType(type);
		fieldDeclaration.modifiers().addAll(newModifier);
		return fieldDeclaration;
	}

	/**
	 * Creates an {@link StringLiteral} from an escaped string value
	 * 
	 * @param ast
	 *            the AbstractSyntaxTree thats the target of the node
	 * @param escapedString
	 *            is the value of the resulting {@link StringLiteral}
	 * @return the wrapped string value
	 */
	public static StringLiteral newStringLiteral(AST ast, String escapedString) {
		StringLiteral result = ast.newStringLiteral();
		result.setEscapedValue(escapedString);
		return result;
	}

	/**
	 * 
	 * @param ast
	 *            the AbstractSyntaxTree thats the target of the node
	 * @param typeBinding
	 *            is a type binding of a variable that is resolved to its Type
	 * @return {@link Type} of the given {@link ITypeBinding}
	 */
	public static Type typeFromBinding(AST ast, ITypeBinding typeBinding) {
		if (ast == null)
		 {
			throw new NullPointerException("ast is null"); //$NON-NLS-1$
		}
		if (typeBinding == null)
		 {
			throw new NullPointerException("typeBinding is null"); //$NON-NLS-1$
		}

		if (typeBinding.isPrimitive()) {
			return ast.newPrimitiveType(PrimitiveType.toCode(typeBinding.getName()));
		}

		if (typeBinding.isCapture()) {
			ITypeBinding wildCard = typeBinding.getWildcard();
			WildcardType capType = ast.newWildcardType();
			ITypeBinding bound = wildCard.getBound();
			if (bound != null) {
				capType.setBound(typeFromBinding(ast, bound));
			}
			return capType;
		}

		if (typeBinding.isArray()) {
			Type elType = typeFromBinding(ast, typeBinding.getElementType());
			return ast.newArrayType(elType, typeBinding.getDimensions());
		}

		if (typeBinding.isParameterizedType()) {
			ParameterizedType type = ast.newParameterizedType(typeFromBinding(ast, typeBinding.getErasure()));

			@SuppressWarnings("unchecked")
			List<Type> newTypeArgs = type.typeArguments();
			for (ITypeBinding typeArg : typeBinding.getTypeArguments()) {
				newTypeArgs.add(typeFromBinding(ast, typeArg));
			}

			return type;
		}

		// simple or raw type
		String qualName = typeBinding.getQualifiedName();
		if ("".equals(qualName)) { //$NON-NLS-1$
			throw new IllegalArgumentException("No name for type binding."); //$NON-NLS-1$
		}
		return ast.newSimpleType(ast.newName(qualName));
	}

	/**
	 * @param ast
	 *            The AbstractSyntaxTree that is the target of the node
	 * @param operator
	 *            {@link PrefixExpression.Operator} of the
	 *            {@link PrefixExpression}
	 * @param expression
	 *            {@link Expression} for the operand
	 * @return {@link PrefixExpression} with the given operator and operands
	 */
	public static PrefixExpression newPrefixExpression(AST ast, PrefixExpression.Operator operator,
			Expression expression) {
		PrefixExpression result = ast.newPrefixExpression();
		result.setOperator(operator);
		result.setOperand(expression);
		return result;
	}

	/**
	 * 
	 * @param ast
	 *            the AbstractSyntaxTree thats the target of the node
	 * @param operator
	 *            {@link InfixExpression.Operator} of the
	 *            {@link InfixExpression}
	 * @param left
	 *            {@link Expression} for the left-operand
	 * @param right
	 *            {@link Expression} for the right-operand
	 * @return {@link InfixExpression} with the given operator and operands
	 */
	public static InfixExpression newInfixExpression(AST ast, InfixExpression.Operator operator, Expression left,
			Expression right) {
		InfixExpression result = ast.newInfixExpression();
		result.setOperator(operator);
		result.setLeftOperand(left);
		result.setRightOperand(right);
		return result;
	}

	/**
	 * 
	 * @param ast
	 *            the AbstractSyntaxTree thats the target of the node
	 * @param expression
	 *            {@link Expression} that is wrapped by the new
	 *            {@link ParenthesizedExpression}
	 * @return {@link ParenthesizedExpression} that wraps the expression
	 */
	public static ParenthesizedExpression newParenthesizedExpression(AST ast, Expression expression) {
		ParenthesizedExpression result = ast.newParenthesizedExpression();
		result.setExpression(expression);
		return result;
	}

	/**
	 * Creates a {@link MarkerAnnotation} for the given {@link Name}
	 * 
	 * @param ast
	 *            the AbstractSyntaxTree thats the target of the node
	 * @param name
	 *            name of the annotation
	 * @return an {@link MarkerAnnotation}
	 */
	public static MarkerAnnotation newMarkerAnnotation(AST ast, Name name) {
		MarkerAnnotation markerAnnotation = ast.newMarkerAnnotation();
		markerAnnotation.setTypeName(name);
		return markerAnnotation;
	}
}
