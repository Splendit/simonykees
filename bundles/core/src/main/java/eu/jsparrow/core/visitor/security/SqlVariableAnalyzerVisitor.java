package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * This visitor is intended to be used by visitors which transform dynamic
 * queries in order to prevent injections.
 * <p>
 * It analyzes the declaration and references on a variable which may represent
 * for example:
 * <ul>
 * <li>a dynamic SQL query</li>
 * <li>an LDAP filter expression</li>
 * </ul>
 * 
 * @since 3.16.0
 *
 */
public class SqlVariableAnalyzerVisitor extends ASTVisitor {

	private final CompilationUnit compilationUnit;
	private final SimpleName simpleNameAtUsage;
	private final VariableDeclarationFragment variableDeclarationFragment;
	private final Block blockAroundLocalDeclarationFragment;
	private final List<Block> scopeOfVariableUsage;
	private final List<SimpleName> variableReferences = new ArrayList<>();
	private boolean beforeDeclaration = true;

	public SqlVariableAnalyzerVisitor(SimpleName variableName) {
		this.compilationUnit = ASTNodeUtil.getSpecificAncestor(variableName, CompilationUnit.class);
		this.simpleNameAtUsage = variableName;
		this.variableDeclarationFragment = findVariableDeclarationFragment(variableName, compilationUnit);
		if (this.variableDeclarationFragment != null) {
			this.blockAroundLocalDeclarationFragment = findBlockSurroundingDeclaration(variableDeclarationFragment);
		} else {
			this.blockAroundLocalDeclarationFragment = null;
		}
		this.scopeOfVariableUsage = findScopeOfVariableUsage(simpleNameAtUsage, blockAroundLocalDeclarationFragment);
	}

	private VariableDeclarationFragment findVariableDeclarationFragment(SimpleName variableName,
			CompilationUnit compilationUnit) {
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

	private Block findBlockSurroundingDeclaration(VariableDeclarationFragment variableDeclarationFragment) {
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

	private List<Block> findScopeOfVariableUsage(SimpleName simpleNameAtUsage, Block blockOfDeclarationFragment) {
		List<Block> ancestorsList = new ArrayList<>();
		ASTNode parentNode = simpleNameAtUsage.getParent();
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

	@Override
	public boolean visit(SimpleName simpleName) {
		if (simpleName == variableDeclarationFragment.getName()) {
			beforeDeclaration = false;
			return false;
		}
		if (beforeDeclaration) {
			return false;
		}
		if (!simpleName.getIdentifier()
			.equals(simpleNameAtUsage.getIdentifier())) {
			return false;
		}
		IBinding binding = simpleName.resolveBinding();
		if (binding.getKind() != IBinding.VARIABLE) {
			return false;
		}
		if (compilationUnit.findDeclaringNode(binding) == variableDeclarationFragment) {
			variableReferences.add(simpleName);
		}
		return true;
	}

	private Expression findInitializationAtDeclaration(VariableDeclarationFragment variableDeclarationFragment) {
		Expression initializer = variableDeclarationFragment.getInitializer();
		if (initializer == null) {
			return null;
		}
		if (initializer.getNodeType() == ASTNode.NULL_LITERAL) {
			return null;
		}
		return initializer;
	}

	private boolean isReferencedAfterUsage(List<SimpleName> variableReferences) {
		int lastIndex = variableReferences.size() - 1;
		return lastIndex > variableReferences.indexOf(this.simpleNameAtUsage);
	}

	private boolean analyzeAssignment(Assignment assignment, Expression initializer) {
		if (assignment.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return false;
		}
		ExpressionStatement expressionStatement = (ExpressionStatement) assignment.getParent();
		if (expressionStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return false;
		}
		if (!scopeOfVariableUsage.contains(expressionStatement.getParent())) {
			return false;
		}

		if (assignment.getOperator() == Assignment.Operator.PLUS_ASSIGN) {
			return initializer != null;
		}

		if (assignment.getOperator() == Assignment.Operator.ASSIGN) {
			return initializer == null;
		}
		return false;
	}

	/**
	 * Analyzes all references on the given dynamic query variable which is
	 * expected to be a local variable.
	 * <p>
	 * Examples of unsafe references which should prohibit the transformation of
	 * a dynamic query:
	 * <ul>
	 * <li>Re-assigning the variable after initialization.</li>
	 * <li>Assigning the value of the query variable to other variables or
	 * fields.</li>
	 * <li>Executing a query with the same variable more than once.</li>
	 * <li>Using the value of the query variable in other method invocations
	 * besides the one invocation which executes the query.</li>
	 * </ul>
	 * 
	 * @return a {@link DynamicQueryComponentsStore} containing the components
	 *         of the query concatenation if the given variable is a local
	 *         variable which is used in a safe way. Otherwise null is returned;
	 */
	public DynamicQueryComponentsStore analyze() {
		variableReferences.clear();
		if (variableDeclarationFragment == null) {
			return null;
		}
		if (blockAroundLocalDeclarationFragment == null) {
			return null;
		}
		blockAroundLocalDeclarationFragment.accept(this);

		if (isReferencedAfterUsage(variableReferences)) {
			return null;
		}
		variableReferences.remove(simpleNameAtUsage);

		DynamicQueryComponentsStore componentStore = new DynamicQueryComponentsStore();
		Expression initializer = findInitializationAtDeclaration(variableDeclarationFragment);
		if (initializer != null) {
			componentStore.storeComponents(initializer);
		}

		for (SimpleName simpleName : variableReferences) {
			if (simpleName.getLocationInParent() != Assignment.LEFT_HAND_SIDE_PROPERTY) {
				return null;
			}
			Assignment assignment = (Assignment) simpleName.getParent();
			if (!analyzeAssignment(assignment, initializer)) {
				return null;
			}
			if (assignment.getOperator() == Assignment.Operator.PLUS_ASSIGN && initializer != null) {
				componentStore.storeComponents(assignment.getRightHandSide());
			} else if (assignment.getOperator() == Assignment.Operator.ASSIGN && initializer == null) {
				Expression rightHandSide = assignment.getRightHandSide();
				if (rightHandSide.getNodeType() != ASTNode.NULL_LITERAL) {
					initializer = rightHandSide;
					componentStore.storeComponents(initializer);
				}
			}
		}
		return componentStore;
	}
}
