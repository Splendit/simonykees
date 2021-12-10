package eu.jsparrow.core.visitor.impl;

import java.util.List;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.core.markers.common.DiamondOperatorEvent;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Diamond operator should be used instead of explicit type arguments.
 * <p>
 * For example:<br/>
 * {@code List<Integer> numbers = new ArrayList<Integer>();} <br/>
 * should be replaced with:<br/>
 * {@code List<Integer> numbers = new ArrayList<>();}
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class DiamondOperatorASTVisitor extends AbstractASTRewriteASTVisitor implements DiamondOperatorEvent {

	private String compilerCompliance;

	public DiamondOperatorASTVisitor(String compilerCompliance) {
		this.compilerCompliance = compilerCompliance;
	}

	/**
	 * Covers the case when a diamond operator can be used in the initialization
	 * or in an assignment expression.
	 */
	@Override
	public boolean visit(ClassInstanceCreation node) {
		Type nodeType = node.getType();
		if (ASTNode.PARAMETERIZED_TYPE == nodeType.getNodeType() && node.getAnonymousClassDeclaration() == null) {
			boolean sameTypes = false;
			ParameterizedType parameterizedType = (ParameterizedType) nodeType;
			// safe casting to typed list

			List<Type> rhsTypeArguments = ASTNodeUtil.returnTypedList(parameterizedType.typeArguments(), Type.class);

			if (rhsTypeArguments != null && !rhsTypeArguments.isEmpty()) {
				ASTNode parent = node.getParent();

				/*
				 * It is important that the type arguments in the declaration
				 * matches with the type arguments in initialization/assignment.
				 * If the declaration is a raw type, we cannot replace the type
				 * arguments with a diamond operator.
				 */

				if (ASTNode.VARIABLE_DECLARATION_FRAGMENT == parent.getNodeType()
						&& (!hasParameterizedArguments(node) || isMethodArgumentsTypeInferable())
						&& !hasMissingLambdaTypeArguments(node)) {

					/*
					 * Declaration and initialization occur in the same
					 * statement. For example: List<String> names = new
					 * ArrayList<String>(); should be replaced with:
					 * List<String> names = new ArrayList<>()
					 */
					ASTNode declarationStatement = parent.getParent();
					Type lhsType = null;

					if (ASTNode.VARIABLE_DECLARATION_STATEMENT == declarationStatement.getNodeType()) {
						lhsType = ((VariableDeclarationStatement) declarationStatement).getType();
					} else if (ASTNode.FIELD_DECLARATION == declarationStatement.getNodeType()) {
						lhsType = ((FieldDeclaration) declarationStatement).getType();
					} else if (ASTNode.VARIABLE_DECLARATION_EXPRESSION == declarationStatement.getNodeType()) {
						lhsType = ((VariableDeclarationExpression) declarationStatement).getType();
					}
					if (lhsType != null && ASTNode.PARAMETERIZED_TYPE == lhsType.getNodeType()) {
						sameTypes = areParameterizedTypeEqual((ParameterizedType) lhsType, rhsTypeArguments);
					}

				} else if (ASTNode.ASSIGNMENT == parent.getNodeType()
						&& (!hasParameterizedArguments(node) || isMethodArgumentsTypeInferable())
						&& !hasMissingLambdaTypeArguments(node)) {

					/*
					 * Declaration and assignment occur on different statements:
					 * For example: List<String> names; names = new
					 * ArrayList<String>()
					 * 
					 * should be replaced with: List<String> names; names = new
					 * ArrayList<>()
					 */
					Assignment assignmentNode = ((Assignment) parent);
					Expression lhsNode = assignmentNode.getLeftHandSide();
					ITypeBinding lhsTypeBinding = lhsNode.resolveTypeBinding();
					if (lhsTypeBinding != null) {
						ITypeBinding[] lhsTypeBindingArguments = lhsTypeBinding.getTypeArguments();
						ITypeBinding rhsTypeBinding = node.resolveTypeBinding();
						if (rhsTypeBinding != null) {
							ITypeBinding[] rhsTypeBindingArguments = rhsTypeBinding.getTypeArguments();
							/*
							 * compare type arguments in new instance creation
							 * with the ones in declaration
							 */
							sameTypes = ClassRelationUtil.compareITypeBinding(lhsTypeBindingArguments,
									rhsTypeBindingArguments);
						}
					}

				} else if (ASTNode.METHOD_INVOCATION == parent.getNodeType() && isMethodArgumentsTypeInferable()
						&& MethodInvocation.ARGUMENTS_PROPERTY == node.getLocationInParent()) {
					MethodInvocation methodInvocation = (MethodInvocation) parent;
					/*
					 * Covers the case when diamond operator can be used on the
					 * arguments of a method invocation. e.g: map.put("key", new
					 * ArrayList<String>()). It can be replaced with:
					 * map.put("key", new ArrayList<>())
					 */
					List<Expression> argumentList = ASTNodeUtil.returnTypedList(methodInvocation.arguments(),
							Expression.class);

					ITypeBinding[] parameterTypeArgs = null;

					// index of the ClassInstanceCreation
					int i = argumentList.indexOf(node);
					/*
					 * resolve the typeBinding of the ClassInstanceCreation
					 * position in MethodHead
					 */
					IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
					if (-1 != i && methodBinding != null) {
						ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
						if (parameterTypes != null && parameterTypes.length > i && !ClassRelationUtil
							.isOverloadedWithParameterizedTypes(methodInvocation, methodBinding, i)) {
							ITypeBinding parameterType = parameterTypes[i];
							parameterTypeArgs = parameterType.getTypeArguments();
						}
					}

					if (parameterTypeArgs != null && rhsTypeArguments.size() == parameterTypeArgs.length) {
						ITypeBinding argBinding = parameterizedType.resolveBinding();
						ITypeBinding[] argTypeBindings = argBinding.getTypeArguments();

						sameTypes = ClassRelationUtil.compareITypeBinding(parameterTypeArgs, argTypeBindings);
					}
				} else if (ASTNode.RETURN_STATEMENT == parent.getNodeType()) {
					ReturnStatement returnStatement = (ReturnStatement) parent;
					MethodDeclaration methodDeclaration = findMethodSignature(returnStatement);
					if (methodDeclaration != null) {
						Type returnType = methodDeclaration.getReturnType2();
						if (returnType != null && ASTNode.PARAMETERIZED_TYPE == returnType.getNodeType()) {
							sameTypes = areParameterizedTypeEqual((ParameterizedType) returnType, rhsTypeArguments);
						}
					}
				}
			}
			if (sameTypes) {
				replaceWithDiamond(parameterizedType, rhsTypeArguments);
				addMarkerEvent(parameterizedType, rhsTypeArguments);
				onRewrite();
			}
		}
		return true;
	}

	/**
	 * Checks whether the given {@link ClassInstanceCreation} node uses raw
	 * method references as parameters (i.e. references to parameterized methods
	 * without explicitly providing the type arguments).
	 * 
	 * @param node
	 *            a node representing a parameterized constructor invocation
	 * @return {@code true} if the number of the missing type arguments in a
	 *         method reference is bigger than 1, or {@code false} otherwise.
	 */
	private boolean hasMissingLambdaTypeArguments(ClassInstanceCreation node) {
		/*
		 * in SIM-820 was discovered that a diamond operator cannot be used when
		 * invoking a parameterized constructor which takes as parameters method
		 * references having more than one missing type arguments.
		 */
		return ASTNodeUtil.returnTypedList(node.arguments(), Expression.class)
			.stream()
			.anyMatch(this::isLambdaWithMissingTypeArguments);
	}

	/**
	 * Checks if the given expression is a {@link MethodReference} invoked
	 * without the required type arguments.
	 * 
	 * @param argument
	 *            an expression representing an argument of a method invocation.
	 * @return {@code true} if the above condition is met, or {@code false}
	 *         otherwise.
	 */
	private boolean isLambdaWithMissingTypeArguments(Expression argument) {

		if (argument instanceof MethodReference) {
			MethodReference lambda = (MethodReference) argument;
			ITypeBinding argBinding = argument.resolveTypeBinding();
			if (argBinding == null) {
				return false;
			}
			if (!argBinding.isParameterizedType()) {
				return false;
			}

			ITypeBinding[] expectedReferenceTypes = argBinding.getTypeArguments();
			if (expectedReferenceTypes.length != 1) {
				return false;
			}

			ITypeBinding expectedReferenceType = expectedReferenceTypes[0];
			if (!expectedReferenceType.isParameterizedType()) {
				return false;
			}
			int numExpectedTypeArgs = expectedReferenceType.getTypeArguments().length;
			return numExpectedTypeArgs > 1 && numExpectedTypeArgs > lambda.typeArguments()
				.size();
		}

		return false;
	}

	/**
	 * Checks whether any of the arguments of the class creation node is a
	 * parameterized type.
	 * 
	 * @param node
	 *            representing a new instance creation
	 * 
	 * @return {@code true} if any of the arguments is parameterized, or
	 *         {@code false} otherwise.
	 */
	protected boolean hasParameterizedArguments(ClassInstanceCreation node) {
		List<Expression> arguments = ASTNodeUtil.returnTypedList(node.arguments(), Expression.class);
		return arguments.stream()
			.map(Expression::resolveTypeBinding)
			.anyMatch(ITypeBinding::isParameterizedType);
	}

	/**
	 * Checks whether the compiler compliance level is at least
	 * {@value JavaVersion#JAVA_1_8}.
	 * 
	 * @return {@code true} if the compliance level is
	 *         {@value JavaVersion#JAVA_1_8} or new, {@code false} otherwise.
	 */
	private boolean isMethodArgumentsTypeInferable() {
		if (compilerCompliance != null) {
			return JavaCore.compareJavaVersions(compilerCompliance, JavaCore.VERSION_1_8) >= 0;
		}
		return false;
	}

	private MethodDeclaration findMethodSignature(ASTNode node) {
		MethodDeclaration methodDeclaration = null;
		if (node != null) {
			ASTNode parent = node;
			do {
				parent = parent.getParent();
			} while (parent != null && parent.getNodeType() != ASTNode.METHOD_DECLARATION);

			if (parent != null) {
				methodDeclaration = (MethodDeclaration) parent;
			}
		}

		return methodDeclaration;
	}

	private boolean areParameterizedTypeEqual(ParameterizedType parameterizedType, List<Type> referenceGenerics) {
		List<Type> returnTypeArgumetns = ASTNodeUtil.returnTypedList(parameterizedType.typeArguments(), Type.class);

		ASTMatcher matcher = new ASTMatcher();
		return matcher.safeSubtreeListMatch(returnTypeArgumetns, referenceGenerics);
	}

	/**
	 * Replaces the type arguments of the given parameterized node with the
	 * diamond operator.
	 * 
	 * @param parameterizedType
	 *            ast node with type arguments.
	 * @param rhsTypeArguments
	 *            list of type arguments to be removed.
	 */
	private void replaceWithDiamond(ParameterizedType parameterizedType, List<Type> rhsTypeArguments) {
		// removing type arguments in new class instance creation
		ListRewrite typeArgumentsListRewrite = astRewrite.getListRewrite(parameterizedType,
				ParameterizedType.TYPE_ARGUMENTS_PROPERTY);
		Statement statement = ASTNodeUtil.getSpecificAncestor(parameterizedType, Statement.class);
		rhsTypeArguments.stream()
			.forEach(type -> {
				getCommentRewriter().saveRelatedComments(type, statement);
				typeArgumentsListRewrite.remove(type, null);
			});
	}
}
