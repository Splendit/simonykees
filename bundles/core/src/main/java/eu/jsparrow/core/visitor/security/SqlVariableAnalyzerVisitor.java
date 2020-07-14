package eu.jsparrow.core.visitor.security;

import java.util.ArrayList;
import java.util.Collections;
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
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
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
	private final List<Block> simpleNameAtUsageBlockAncestors;
	private final DynamicQueryComponentsStore componentStore = new DynamicQueryComponentsStore();
	private boolean beforeDeclaration = true;
	private boolean beforeUsage = true;
	private boolean unsafe = false;
	private Expression initializer;

	public SqlVariableAnalyzerVisitor(SimpleName variableName) {
		this.compilationUnit = ASTNodeUtil.getSpecificAncestor(variableName, CompilationUnit.class);
		this.simpleNameAtUsage = variableName;
		this.variableDeclarationFragment = findVariableDeclarationFragment(variableName, compilationUnit);
		this.blockAroundLocalDeclarationFragment = findBlockSurroundingDeclaration(variableDeclarationFragment);
		if (blockAroundLocalDeclarationFragment != null) {
			this.simpleNameAtUsageBlockAncestors = createBlockAncestorList(simpleNameAtUsage,
					blockAroundLocalDeclarationFragment);
		} else {
			this.simpleNameAtUsageBlockAncestors = Collections.emptyList();
		}
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
		if (variableDeclarationFragment == null) {
			return null;
		}
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

	private List<Block> createBlockAncestorList(SimpleName simpleNameAtUsage, Block blockOfDeclarationFragment) {

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
	public boolean preVisit2(ASTNode node) {
		return !unsafe;
	}

	@Override
	public boolean visit(VariableDeclarationFragment fragment) {
		if (this.variableDeclarationFragment == fragment) {
			beforeDeclaration = false;
			initializer = fragment.getInitializer();
			if (initializer != null) {
				if (initializer.getNodeType() != ASTNode.NULL_LITERAL) {
					componentStore.storeComponents(initializer);
				} else {
					initializer = null;
				}
			}
			return false;
		}
		return true;
	}

	private Block findBlockSurroundingAssignement(Assignment assignment) {
		if (assignment.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return null;
		}
		ExpressionStatement expressionStatement = (ExpressionStatement) assignment.getParent();
		if (expressionStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return null;
		}
		return (Block) expressionStatement.getParent();
	}
	
	protected boolean isVariableReference(SimpleName simpleName) {		
		if (!simpleName.getIdentifier()
			.equals(simpleNameAtUsage.getIdentifier())) {
			return false;
		}
		IBinding binding = simpleName.resolveBinding();
		if (binding.getKind() != IBinding.VARIABLE) {
			return false;
		}
		ASTNode declaringNode = compilationUnit.findDeclaringNode(binding);
		return declaringNode == variableDeclarationFragment;
	}

	@Override
	public boolean visit(SimpleName simpleName) {
		
		if (beforeDeclaration) {
			return false;
		}

		if (simpleName == simpleNameAtUsage) {
			beforeUsage = false;
			return false;
		}
		
		if(!isVariableReference(simpleName)) {
			return false;
		}

		if (!beforeUsage) {
			unsafe = true;
			return false;
		}

		StructuralPropertyDescriptor structuralDescriptor = simpleName.getLocationInParent();
		if (structuralDescriptor == Assignment.LEFT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) simpleName.getParent();
			
			Block blockAroundAssignement = findBlockSurroundingAssignement(assignment);
			if(blockAroundAssignement == null) {
				unsafe = true;
				return false;
			}
			if (!simpleNameAtUsageBlockAncestors.contains(blockAroundAssignement)) {
				unsafe = true;
				return false;
			}

			if (assignment.getOperator() == Assignment.Operator.PLUS_ASSIGN && initializer != null) {
				componentStore.storeComponents(assignment.getRightHandSide());
			} else if (assignment.getOperator() == Assignment.Operator.ASSIGN && initializer == null) {
				Expression rightHandSide = assignment.getRightHandSide();
				if (rightHandSide.getNodeType() != ASTNode.NULL_LITERAL) {
					initializer = rightHandSide;
					componentStore.storeComponents(initializer);
				}
			} else {
				unsafe = true;
			}
		} else {
			unsafe = true;
		}
		return true;
	}

	public List<Expression> getDynamicQueryComponents() {
		return componentStore.getComponents();
	}

	public boolean analyze() {

		if (blockAroundLocalDeclarationFragment == null) {
			return false;
		}
		if (simpleNameAtUsageBlockAncestors.isEmpty()) {
			return false;
		}
		blockAroundLocalDeclarationFragment.accept(this);
		return !unsafe;
	}
}
