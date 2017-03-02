package at.splendit.simonykees.core.visitor;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.i18n.Messages;

/**
 * Diamond operator should be used instead of explicit type arguments.
 * 
 * @author Ardit Ymeri
 * @since 1.0
 *
 */
public class DiamondOperatorASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(ClassInstanceCreation node) {
		Type nodeType = node.getType();
		if (ASTNode.PARAMETERIZED_TYPE == nodeType.getNodeType()) {
			ParameterizedType parameterizedType = (ParameterizedType) nodeType;
			List<Type> rhsTypeArguments = 
					// safe casting to typed list
					Lists.newArrayList(Iterables.filter(parameterizedType.typeArguments(), Type.class));
			
			if (rhsTypeArguments != null && !rhsTypeArguments.isEmpty()) {
				ASTNode parent = node.getParent();
				
				if (ASTNode.VARIABLE_DECLARATION_FRAGMENT == parent.getNodeType()) {
					/*
					 * Declaration and initialization occur in the same statement. 
					 * For example:
					 * 		List<String> names = new ArrayList<String>();
					 * should be replaced with:
					 * 		List<String> names = new ArrayList<>();
					 */
					ASTNode varDeclarationStatement = parent.getParent();
					if (ASTNode.VARIABLE_DECLARATION_STATEMENT == varDeclarationStatement.getNodeType()) {
						Type lhsType = ((VariableDeclarationStatement) varDeclarationStatement).getType();
						if (ASTNode.PARAMETERIZED_TYPE == lhsType.getNodeType()) {
							
							List<Type> lhsTypeArguments = 
									// safe casting to typed list
									Lists.newArrayList(Iterables.filter(((ParameterizedType) lhsType).typeArguments(), Type.class));

							// checking if type arguments in declaration match with the ones in initialization
							ASTMatcher astMatcher = new ASTMatcher();
							if (astMatcher.safeSubtreeListMatch(lhsTypeArguments, rhsTypeArguments)) {
								// removing type arguments in new class instance creation
								Activator.log(Messages.DiamondOperatorASTVisitor_using_diamond_operator);
								ListRewrite typeArgumentsListRewrite = astRewrite.getListRewrite(parameterizedType,
										ParameterizedType.TYPE_ARGUMENTS_PROPERTY);
								rhsTypeArguments.stream().forEach(type -> typeArgumentsListRewrite.remove(type, null));
							}
						}
					}

				} else if (ASTNode.ASSIGNMENT == parent.getNodeType()) {
					/*
					 * Declaration and assignment occur on different statements:  
					 * For example:
					 * 		List<String> names;
					 * 		names = new ArrayList<String>();
					 * 
					 * should be replaced with:
					 * 		List<String> names;
					 * 		names = new ArrayList<>();
					 */
					Assignment assignmentNode = ((Assignment) parent);
					Expression lhsNode = assignmentNode.getLeftHandSide();
					if (ASTNode.SIMPLE_NAME == lhsNode.getNodeType()) {
						ITypeBinding lhsTypeBinding = lhsNode.resolveTypeBinding();
						ITypeBinding[] lhsTypeArguments = lhsTypeBinding.getTypeArguments();
						ITypeBinding rhsTypeBinding = node.resolveTypeBinding();
						ITypeBinding[] rhsTypeBindingArguments = rhsTypeBinding.getTypeArguments();
						// compare type arguments in new instance creation with the ones in declaration
						boolean sameTypes = compareTypeBindingArguments(lhsTypeArguments, rhsTypeBindingArguments);
						if (sameTypes) {
							// removing type arguments in new class instance creation
							Activator.log(Messages.DiamondOperatorASTVisitor_using_diamond_operator);
							ListRewrite typeArgumentsListRewrite = astRewrite.getListRewrite(parameterizedType,
									ParameterizedType.TYPE_ARGUMENTS_PROPERTY);
							rhsTypeArguments.stream().forEach(type -> typeArgumentsListRewrite.remove(type, null));
						}
					}
				}
			}
		}

		return false;
	}

	/**
	 * Compares the given lists by getting the qualified name of 
	 * corresponding elements on same positions. 
	 * 
	 * @return if both lists have the same size and all corresponding 
	 * elements have the same qualified name.
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
