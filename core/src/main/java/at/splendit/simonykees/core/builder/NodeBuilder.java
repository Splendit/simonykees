package at.splendit.simonykees.core.builder;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.WildcardType;

/**
 * Helper Class to generate new ASTNodes
 * 
 * @since 0.9.0
 * @author Martin Huter
 * @sice 0.9.2
 */
public class NodeBuilder {

	@Deprecated
	@SuppressWarnings("unchecked")
	public static MethodInvocation newMethodInvocation(AST ast, Expression optinoalExpression, SimpleName name,
			Expression argument) {
		MethodInvocation resultMI = ast.newMethodInvocation();
		resultMI.setExpression(optinoalExpression);
		resultMI.setName(name);
		resultMI.arguments().add(argument);
		return resultMI;
	}

	@Deprecated
	@SuppressWarnings("unchecked")
	public static MethodInvocation newMethodInvocation(AST ast, Expression optinoalExpression, SimpleName name,
			List<Expression> arguments) {
		MethodInvocation resultMI = ast.newMethodInvocation();
		resultMI.setExpression(optinoalExpression);
		resultMI.setName(name);
		resultMI.arguments().addAll(arguments);
		return resultMI;
	}

	/**
	 * 
	 * @param ast
	 *            the AbastractSyntaxTree thats the target of the node
	 * @param identifier name that is used for the SimpleName
	 * @return an {@link SimpleName} that capsules the identifier
	 */
	public static SimpleName newSimpleName(AST ast, String identifier) {
		return ast.newSimpleName(identifier);
	}

	/**
	 * Generates an enhanced for Statement with the following parameters:
	 * for([expression]:[parameter]{[block]} 
	 * 
	 * @param ast
		return ast.newSimpleName(identifier);
	}

	/**
	 * Generates an enhanced for Statement with the following parameters:
	 * for([expression]:[parameter]{[block]} 
	 * 
	 * @param ast
	 *            the AbastractSyntaxTree thats the target of the node
	 * @param body are the statements in of the for loop 
	 * @param expression iteration variable
	 * @param parameter iteration base object
	 * @return an {@link EnhancedForStatement} that combines all the input parameters
	 */
	public static EnhancedForStatement newEnhandesForStatement(AST ast, Statement body, Expression expression,
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
	 *            the AbastractSyntaxTree thats the target of the node
	 * @param name is the name of the generated variable
	 * @param variableType is the type of the generated variable
	 * @return {@link SingleVariableDeclaration} with the name as identifier name and variableType as object type
	 */
	public static SingleVariableDeclaration newSingleVariableDeclaration(AST ast, SimpleName name, Type variableType) {
		SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
		svd.setName(name);
		svd.setType(variableType);
		return svd;
	}

	/**
	 * 
	 * @param ast
	 *            the AbastractSyntaxTree thats the target of the node
	 * @param typeBinding is a type binding of a variable that is resolved to its Type
	 * @return {@link Type} of the given {@link ITypeBinding}
	 */
	public static Type typeFromBinding(AST ast, ITypeBinding typeBinding) {
		if (ast == null)
			throw new NullPointerException("ast is null"); //$NON-NLS-1$
		if (typeBinding == null)
			throw new NullPointerException("typeBinding is null"); //$NON-NLS-1$

		if (typeBinding.isPrimitive()) {
			return ast.newPrimitiveType(PrimitiveType.toCode(typeBinding.getName()));
		}

		if (typeBinding.isCapture()) {
			ITypeBinding wildCard = typeBinding.getWildcard();
			WildcardType capType = ast.newWildcardType();
			ITypeBinding bound = wildCard.getBound();
			if (bound != null) {
				capType.setBound(typeFromBinding(ast, bound));// ),
				// wildCard.isUpperbound());
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
}
