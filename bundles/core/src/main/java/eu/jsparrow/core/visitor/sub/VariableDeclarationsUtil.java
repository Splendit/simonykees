package eu.jsparrow.core.visitor.sub;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * A helper class for extracting information from
 * {@link VariableDeclarationFragment} nodes.
 * 
 * @since 3.22.0
 *
 */
public class VariableDeclarationsUtil {

	private VariableDeclarationsUtil() {
		/*
		 * Hide the default constructor
		 */
	}

	/**
	 * 
	 * @param variableName
	 * @return If the specified {@link SimpleName} references a variable
	 *         declaration, then this method returns the corresponding
	 *         {@link VariableDeclarationFragment}. Otherwise, null is returned.
	 */
	public static VariableDeclarationFragment findVariableDeclarationFragment(SimpleName variableName) {
		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(variableName, CompilationUnit.class);
		IBinding queryVariableBinding = variableName.resolveBinding();
		if (queryVariableBinding.getKind() != IBinding.VARIABLE) {
			return null;
		}
		ASTNode declarationNode = compilationUnit.findDeclaringNode(queryVariableBinding);
		if (declarationNode == null || declarationNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return null;
		}
		return (VariableDeclarationFragment) declarationNode;
	}

	/**
	 * @return If the given {@link VariableDeclarationFragment} declares a local
	 *         variable, then this method returns the {@link Block} containing
	 *         the given variable declaration. Otherwise, null is returned.
	 */
	public static Block findBlockSurroundingDeclaration(VariableDeclarationFragment variableDeclarationFragment) {
		if (variableDeclarationFragment.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			return null;
		}
		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) variableDeclarationFragment
			.getParent();
		if (variableDeclarationStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return null;
		}
		return (Block) variableDeclarationStatement.getParent();
	}

	/**
	 * Collects all blocks which belong to the scope of a given variable
	 * referencing a local variable.
	 * 
	 * 
	 * @param blockOfDeclarationFragment
	 *            expected to contain the declaration of the variable which is
	 *            referenced by the {@link SimpleName} specified by the second
	 *            parameter.
	 * @param variableName
	 * @return A list of {@link Block} instances belonging to the scope of the
	 *         specified variable name, with the {@link Block} surrounding the
	 *         variable name as first element and the {@link Block} where the
	 *         variable is declared as last element.
	 * 
	 */
	public static List<Block> findScopeOfVariableUsage(Block blockOfDeclarationFragment, SimpleName variableName) {
		List<Block> ancestorsList = new ArrayList<>();
		ASTNode parentNode = variableName.getParent();
		while (parentNode != null) {
			if (parentNode.getNodeType() == ASTNode.BLOCK) {
				ancestorsList.add((Block) parentNode);
				if (parentNode == blockOfDeclarationFragment) {
					break;
				}
			}
			parentNode = parentNode.getParent();
		}
		return ancestorsList;
	}

	/**
	 * 
	 * @return The {@link Expression} specifying the initializer of the given
	 *         {@link VariableDeclarationFragment} if an initializer can be
	 *         found which is not the {@link ASTNode#NULL_LITERAL}. Otherwise,
	 *         null is returned.
	 */
	public static Expression findInitializationAtDeclaration(VariableDeclarationFragment variableDeclarationFragment) {
		Expression initializer = variableDeclarationFragment.getInitializer();
		if (initializer == null) {
			return null;
		}
		if (initializer.getNodeType() == ASTNode.NULL_LITERAL) {
			return null;
		}
		return initializer;
	}

}
