package at.splendit.simonykees.core.visitor;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.i18n.Messages;

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
public class DiamondOperatorASTVisitor extends AbstractASTRewriteASTVisitor {

	/**
	 * Covers the case when a diamond operator can be used in the initialization
	 * or in an assignment expression.
	 */
	@Override
	public boolean visit(ClassInstanceCreation node) {
		Type nodeType = node.getType();
		if (ASTNode.PARAMETERIZED_TYPE == nodeType.getNodeType()) {
			ParameterizedType parameterizedType = (ParameterizedType) nodeType;
			// safe casting to typed list
			@SuppressWarnings("unchecked")
			List<Type> rhsTypeArguments = ((List<Object>) parameterizedType.typeArguments()).stream()
					.filter(Type.class::isInstance).map(Type.class::cast).collect(Collectors.toList());

			if (rhsTypeArguments != null && !rhsTypeArguments.isEmpty()) {
				ASTNode parent = node.getParent();

				/*
				 * It is important that the type arguments in the initialization
				 * matches with the type arguments in initialization/assignment.
				 * If the declaration is a raw type, we cannot replace the type
				 * arguments with a diamond operator.
				 */

				if (ASTNode.VARIABLE_DECLARATION_FRAGMENT == parent.getNodeType()) {
					/*
					 * Declaration and initialization occur in the same
					 * statement. For example: List<String> names = new
					 * ArrayList<String>(); should be replaced with:
					 * List<String> names = new ArrayList<>();
					 */
					ASTNode varDeclarationStatement = parent.getParent();
					if (ASTNode.VARIABLE_DECLARATION_STATEMENT == varDeclarationStatement.getNodeType()) {
						Type lhsType = ((VariableDeclarationStatement) varDeclarationStatement).getType();
						if (ASTNode.PARAMETERIZED_TYPE == lhsType.getNodeType()) {
							// safe casting to typed list
							@SuppressWarnings("unchecked")
							List<Object> typeArguments = ((ParameterizedType) lhsType).typeArguments();
							List<Type> lhsTypeArguments = ((List<Object>)typeArguments).stream()
									.filter(Type.class::isInstance).map(Type.class::cast).collect(Collectors.toList());

							// checking if type arguments in declaration match
							// with the ones in initialization
							ASTMatcher astMatcher = new ASTMatcher();
							if (astMatcher.safeSubtreeListMatch(lhsTypeArguments, rhsTypeArguments)) {
								replaceWithDiamond(parameterizedType, rhsTypeArguments);
							}
						}
					}

				} else if (ASTNode.ASSIGNMENT == parent.getNodeType()) {
					/*
					 * Declaration and assignment occur on different statements:
					 * For example: List<String> names; names = new
					 * ArrayList<String>();
					 * 
					 * should be replaced with: List<String> names; names = new
					 * ArrayList<>();
					 */
					Assignment assignmentNode = ((Assignment) parent);
					Expression lhsNode = assignmentNode.getLeftHandSide();
					if (ASTNode.SIMPLE_NAME == lhsNode.getNodeType()) {
						ITypeBinding lhsTypeBinding = lhsNode.resolveTypeBinding();
						ITypeBinding[] lhsTypeArguments = lhsTypeBinding.getTypeArguments();
						ITypeBinding rhsTypeBinding = node.resolveTypeBinding();
						ITypeBinding[] rhsTypeBindingArguments = rhsTypeBinding.getTypeArguments();
						// compare type arguments in new instance creation with
						// the ones in declaration
						boolean sameTypes = compareTypeBindingArguments(lhsTypeArguments, rhsTypeBindingArguments);
						if (sameTypes) {
							replaceWithDiamond(parameterizedType, rhsTypeArguments);
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * Covers the case when diamond operator can be used on the arguments of a
	 * method invocation. e.g:
	 * <p>
	 * {@code map.put("key", new ArrayList<String>());} <br/>
	 * can be replaced with: <br/>
	 * {@code map.put("key", new ArrayList<>());} <br/>
	 */
	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding methodBinding = node.resolveMethodBinding();
		if (methodBinding != null) {
			ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
			// safe casting to typed list
			@SuppressWarnings("unchecked")
			List<ASTNode> arguments = ((List<Object>)node.arguments()).stream()
					.filter(Type.class::isInstance).map(ASTNode.class::cast).collect(Collectors.toList());
			if (arguments.size() == parameterTypes.length) {
				for (int i = 0; i < arguments.size(); i++) {
					ASTNode argument = arguments.get(i);
					if (ASTNode.CLASS_INSTANCE_CREATION == argument.getNodeType()) {
						ClassInstanceCreation clsInstatnceCreateion = (ClassInstanceCreation) argument;
						Type argType = clsInstatnceCreateion.getType();

						if (ASTNode.PARAMETERIZED_TYPE == argType.getNodeType()) {
							ParameterizedType parArgType = (ParameterizedType) argType;
							// safe casting to typed list
							@SuppressWarnings("unchecked")
							List<Type> parTypeArguments = ((List<Object>)parArgType.typeArguments()).stream()
									.filter(Type.class::isInstance).map(Type.class::cast).collect(Collectors.toList());
							ITypeBinding parameterType = parameterTypes[i];
							ITypeBinding[] parameterTypeArgs = parameterType.getTypeArguments();

							if (!parTypeArguments.isEmpty() && parTypeArguments.size() == parameterTypeArgs.length) {
								ITypeBinding argBinding = parArgType.resolveBinding();
								ITypeBinding[] argTypeBindings = argBinding.getTypeArguments();

								if (compareTypeBindingArguments(parameterTypeArgs, argTypeBindings)) {
									replaceWithDiamond(parArgType, parTypeArguments);
								}
							}
						}
					}
				}
			}
		}

		return false;
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
		Activator.log(Messages.DiamondOperatorASTVisitor_using_diamond_operator);
		ListRewrite typeArgumentsListRewrite = astRewrite.getListRewrite(parameterizedType,
				ParameterizedType.TYPE_ARGUMENTS_PROPERTY);
		rhsTypeArguments.stream().forEach(type -> typeArgumentsListRewrite.remove(type, null));
	}

	/**
	 * Compares the given lists by getting the qualified name of corresponding
	 * elements on same positions.
	 * 
	 * @return if both lists have the same size and all corresponding elements
	 *         have the same qualified name.
	 */
	private boolean compareTypeBindingArguments(ITypeBinding[] lhsTypeArguments,
			ITypeBinding[] rhsTypeBindingArguments) {
		boolean equals = true;
		int lhsSize = lhsTypeArguments.length;
		int rhsSize = rhsTypeBindingArguments.length;
		if (lhsSize == rhsSize) {
			for (int i = 0; i < lhsSize; i++) {
				ITypeBinding lhsType = lhsTypeArguments[i];
				ITypeBinding rhsType = rhsTypeBindingArguments[i];
				String lhsTypeName = lhsType.getQualifiedName();
				String rhsTypeName = rhsType.getQualifiedName();
				if (!lhsTypeName.equals(rhsTypeName)) {
					equals = false;
					break;
				}
			}

		} else {
			equals = false;
		}

		return equals;
	}
}
