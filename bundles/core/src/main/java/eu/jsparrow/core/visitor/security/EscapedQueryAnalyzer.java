package eu.jsparrow.core.visitor.security;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * This visitor is intended to be used by visitors which escape user supplied
 * input.
 * <p>
 * It analyzes the declaration and the references on a string variable which may
 * represent for example a dynamic SQL query where string concatenation
 * components will be escaped in order to prevent injection.
 * 
 * @since 3.21.0
 *
 */
public class EscapedQueryAnalyzer {

	private final SimpleName simpleNameAtUsage;

	public EscapedQueryAnalyzer(SimpleName simpleNameAtUsage) {
		this.simpleNameAtUsage = simpleNameAtUsage;
	}

	private VariableDeclarationFragment findVariableDeclarationFragment() {
		CompilationUnit compilationUnit = ASTNodeUtil.getSpecificAncestor(simpleNameAtUsage, CompilationUnit.class);
		IBinding queryVariableBinding = simpleNameAtUsage.resolveBinding();
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

	private boolean analyzeVariableReferences(List<SimpleName> variableReferences) {
		int lastIndex = variableReferences.size() - 1;
		if (lastIndex > variableReferences.indexOf(simpleNameAtUsage)) {
			return false;
		}
		return variableReferences.stream()
			.allMatch(r -> r == simpleNameAtUsage || r.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY);
	}

	private List<Assignment> collectAssignmentsToQueryVariable(List<SimpleName> variableReferences) {
		return variableReferences.stream()
			.filter(r -> r.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY)
			.map(r -> (Assignment) r.getParent())
			.collect(Collectors.toList());
	}

	/**
	 * Analyzes all references on the given dynamic query variable which is
	 * expected to be a local variable. Additionally, it is expected that the
	 * usage of the query variable is safe.
	 * <p>
	 * In case of a safe usage all assignments on the query variable are
	 * evaluated to string concatenation components stored in an instance of
	 * {@link DynamicQueryComponentsStore} which is returned as analysis result.
	 * <p>
	 * In case of an unsafe usage the assignments on the query variable cannot
	 * be evaluated and the transformation must be cancelled. In this case, null
	 * is returned.
	 * <p>
	 * Examples of unsafe references which prohibit the transformation of a
	 * dynamic query are the following:
	 * <ul>
	 * <li>Re-assigning the variable after initialization.</li>
	 * <li>Assigning the value of the query variable to other variables or
	 * fields.</li>
	 * <li>Executing a query with the same variable more than once.</li>
	 * <li>Using the value of the query variable in other method invocations
	 * besides the one invocation which executes the query.</li>
	 * </ul>
	 * 
	 * @param simpleNameAtUsage
	 *            specifies the dynamic query variable the usage of which is
	 *            analyzed.
	 * @return a {@link DynamicQueryComponentsStore} containing the components
	 *         of the query concatenation if the given variable is a local
	 *         variable which is used in a safe way. Otherwise null is returned;
	 */
	public DynamicQueryComponentsStore analyze() {

		VariableDeclarationFragment variableDeclarationFragment = findVariableDeclarationFragment();
		if (variableDeclarationFragment == null) {
			return null;
		}

		Block blockAroundLocalDeclarationFragment = findBlockSurroundingDeclaration(variableDeclarationFragment);
		if (blockAroundLocalDeclarationFragment == null) {
			return null;
		}

		SqlVariableAnalyzerVisitor visitor = new SqlVariableAnalyzerVisitor(variableDeclarationFragment);
		blockAroundLocalDeclarationFragment.accept(visitor);
		if (!analyzeVariableReferences(visitor.getVariableReferences())) {
			return null;
		}

		List<Assignment> assignmentsToQueryVariable = collectAssignmentsToQueryVariable(
				visitor.getVariableReferences());

		DynamicQueryComponentsStore componentStore = new DynamicQueryComponentsStore();
		Expression initializer = findInitializationAtDeclaration(variableDeclarationFragment);
		if (initializer != null) {
			componentStore.storeComponents(initializer);
		}
		for (Assignment assignment : assignmentsToQueryVariable) {
			componentStore.storeComponents(assignment.getRightHandSide());
		}
		return componentStore;
	}

}
