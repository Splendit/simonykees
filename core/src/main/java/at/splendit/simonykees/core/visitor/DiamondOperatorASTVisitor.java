package at.splendit.simonykees.core.visitor;

import java.util.List;

import org.apache.commons.lang3.JavaVersion;
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
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.i18n.Messages;

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

	private static final Logger logger = LoggerFactory.getLogger(DiamondOperatorASTVisitor.class);
	
	private JavaVersion compilerCompliance;
	
	public DiamondOperatorASTVisitor(JavaVersion compilerCompliance) {
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
				 * It is important that the type arguments in the initialization
				 * matches with the type arguments in initialization/assignment.
				 * If the declaration is a raw type, we cannot replace the type
				 * arguments with a diamond operator.
				 */

				if (ASTNode.VARIABLE_DECLARATION_FRAGMENT == parent.getNodeType()) {

					if(node.arguments().isEmpty() || isMethodArgumentsTypeInferable()) {
						/*
						 * Declaration and initialization occur in the same
						 * statement. For example: List<String> names = new
						 * ArrayList<String>(); should be replaced with:
						 * List<String> names = new ArrayList<>();
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
					}

				} else if (ASTNode.ASSIGNMENT == parent.getNodeType()) {

					if(node.arguments().isEmpty() || isMethodArgumentsTypeInferable()) {
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
						ITypeBinding lhsTypeBinding = lhsNode.resolveTypeBinding();
						if (lhsTypeBinding != null) {
							ITypeBinding[] lhsTypeBindingArguments = lhsTypeBinding.getTypeArguments();
							ITypeBinding rhsTypeBinding = node.resolveTypeBinding();
							if (rhsTypeBinding != null) {
								ITypeBinding[] rhsTypeBindingArguments = rhsTypeBinding.getTypeArguments();
								// compare type arguments in new instance creation
								// with
								// the ones in declaration
	
									sameTypes = ClassRelationUtil.compareITypeBinding(lhsTypeBindingArguments,
											rhsTypeBindingArguments);
								}
							}
					}

				} else if (ASTNode.METHOD_INVOCATION == parent.getNodeType() && isMethodArgumentsTypeInferable()
						&& MethodInvocation.ARGUMENTS_PROPERTY == node.getLocationInParent()) {

					/*
					 * Covers the case when diamond operator can be used on the
					 * arguments of a method invocation. e.g: <p> {@code
					 * map.put("key", new ArrayList<String>());} <br/> can be
					 * replaced with: <br/> {@code map.put("key", new
					 * ArrayList<>());} <br/>
					 */
					List<Expression> argumentList = ASTNodeUtil.returnTypedList(((MethodInvocation) parent).arguments(),
							Expression.class);

					ITypeBinding[] parameterTypeArgs = null;

					// index of the ClassInstanceCreation
					int i = argumentList.indexOf(node);
					/*
					 * resolve the typeBinding of the ClassInstanceCreation
					 * position in MethodHead
					 */
					IMethodBinding methodBinding = ((MethodInvocation) parent).resolveMethodBinding();
					if (-1 != i && methodBinding != null) {
						ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
						if (parameterTypes != null && parameterTypes.length > i) {
							ITypeBinding parameterType = parameterTypes[i];
							parameterTypeArgs = parameterType.getTypeArguments();
						}
					}

					if (!rhsTypeArguments.isEmpty() && rhsTypeArguments.size() == parameterTypeArgs.length) {
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
			}
		}

		return true;
	}

	/**
	 * Checks whether the compiler compliance level is at least
	 * {@value JavaVersion#JAVA_1_8}.
	 * 
	 * @return {@code true} if the compliance level is
	 *         {@value JavaVersion#JAVA_1_8} or new, {@code false} otherwise.
	 */
	private boolean isMethodArgumentsTypeInferable() {
		if(compilerCompliance != null) {
			return compilerCompliance.atLeast(JavaVersion.JAVA_1_8);
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
		List<Type> returnTypeArgumetns = ASTNodeUtil.returnTypedList(((ParameterizedType) parameterizedType).typeArguments(), Type.class);

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
		logger.debug(Messages.DiamondOperatorASTVisitor_using_diamond_operator);
		ListRewrite typeArgumentsListRewrite = astRewrite.getListRewrite(parameterizedType,
				ParameterizedType.TYPE_ARGUMENTS_PROPERTY);
		rhsTypeArguments.stream().forEach(type -> typeArgumentsListRewrite.remove(type, null));
	}
}
