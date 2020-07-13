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
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

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

	private CompilationUnit compilationUnit;
	private SimpleName simpleNameAtUsage;
	private ASTNode declarationFragment;
	private VariableDeclarationFragment variableDeclarationFragment;
	private final DynamicQueryComponentsStore componentStore = new DynamicQueryComponentsStore();
	private boolean beforeDeclaration = true;
	private boolean beforeUsage = true;
	private boolean unsafe = false;
	private Expression initializer;

	public SqlVariableAnalyzerVisitor(SimpleName variableName, ASTNode declaration, CompilationUnit compilationUnit) {
		this.simpleNameAtUsage = variableName;
		this.declarationFragment = declaration;
		this.compilationUnit = compilationUnit;
	}
	
	@Override
	public boolean preVisit2(ASTNode node) {
		return !unsafe;
	}

	@Override
	public boolean visit(VariableDeclarationFragment fragment) {
		if (this.declarationFragment == fragment) {
			variableDeclarationFragment = fragment;
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

	@Override
	public boolean visit(SimpleName simpleName) {
		if (beforeDeclaration) {
			return false;
		}

		if (simpleName == simpleNameAtUsage) {
			beforeUsage = false;
			return false;
		}

		if (!simpleNameAtUsage.getIdentifier()
			.equals(simpleName.getIdentifier())) {
			return false;
		}

		IBinding binding = simpleName.resolveBinding();
		if (binding.getKind() != IBinding.VARIABLE) {
			return false;
		}

		if (((IVariableBinding) binding).isField()) {
			return false;
		}

		ASTNode declaringNode = compilationUnit.findDeclaringNode(simpleName.resolveBinding());
		if (declaringNode != declarationFragment) {
			return false;
		}

		if (!beforeUsage) {
			unsafe = true;
			return false;
		}

		StructuralPropertyDescriptor structuralDescriptor = simpleName.getLocationInParent();
		if (structuralDescriptor == Assignment.LEFT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) simpleName.getParent();
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

	public boolean isUnsafe() {
		return unsafe;
	}

	public List<Expression> getDynamicQueryComponents() {
		return componentStore.getComponents();
	}

	public VariableDeclarationFragment getVariableDeclarationFragment() {
		return variableDeclarationFragment;
	}

	public SimpleName getSimpleNameAtUsage() {
		return simpleNameAtUsage;
	}	
	
	Block getBlockSurroundingSimpleNameAtUsage(SimpleName simpleNameAtUsage) {

		if (simpleNameAtUsage.getLocationInParent() != MethodInvocation.ARGUMENTS_PROPERTY) {
			return null;
		}
		MethodInvocation invocation = (MethodInvocation) simpleNameAtUsage.getParent();

		Statement statement = null;
		if (invocation.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
			statement = (ExpressionStatement) invocation.getParent();
		}

		if (invocation.getLocationInParent() == Assignment.RIGHT_HAND_SIDE_PROPERTY) {
			Assignment assignment = (Assignment) invocation.getParent();
			if (assignment.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
				statement = (ExpressionStatement) assignment.getParent();
			}
		}
		
		if (invocation.getLocationInParent() == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) invocation.getParent();
			if (fragment.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
				statement = (VariableDeclarationStatement) fragment.getParent();
			}
		}
		if (statement == null) {
			return null;
		}
		if (statement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return null;
		}
		return (Block) statement.getParent();

	}

	/**
	 * 
	 * @param variableDeclarationFragment
	 * @return if the given {@link VariableDeclarationFragment} is a fragment of
	 *         a {@link VariableDeclarationStatement} surrounded by a
	 *         {@link Block}, then the {@link Block} surrounding the
	 *         {@link VariableDeclarationFragment} is returned. Otherwise, null
	 *         is returned.
	 */
	Block getBlockOfLocalVariableDeclaration(VariableDeclarationFragment variableDeclarationFragment) {
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
	 * 
	 * @return a list containing at least one {@link Block} if a valid path of
	 *         ancestor blocks can be found. Otherwise an empty list is
	 *         returned.
	 */
	List<Block> createBlockAncestorList(SimpleName simpleNameAtUsage,
			VariableDeclarationFragment variableDeclarationFragment) {

		Block blockOfLocalVariableDeclaration = getBlockOfLocalVariableDeclaration(variableDeclarationFragment);
		if (blockOfLocalVariableDeclaration == null) {
			return Collections.emptyList();
		}
		Block blockSurroundingSimpleNameAtUsage = getBlockSurroundingSimpleNameAtUsage(simpleNameAtUsage);
		if (blockSurroundingSimpleNameAtUsage == null) {
			return Collections.emptyList();
		}

		List<Block> ancestorsList = new ArrayList<>();
		ASTNode parentNode = blockSurroundingSimpleNameAtUsage;
		while (parentNode != null) {
			if (parentNode.getNodeType() == ASTNode.BLOCK) {
				ancestorsList.add((Block) parentNode);
			}
			if (parentNode == blockOfLocalVariableDeclaration) {
				break;
			}
			parentNode = parentNode.getParent();
		}
		return ancestorsList;
	}
}
