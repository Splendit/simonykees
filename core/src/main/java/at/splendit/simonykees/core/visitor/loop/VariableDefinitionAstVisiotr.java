package at.splendit.simonykees.core.visitor.loop;

import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import at.splendit.simonykees.core.visitor.AbstractCompilationUnitAstVisitor;


/**
 * @since 9.2.0
 * @author mgh
 *
 */
class VariableDefinitionAstVisiotr extends AbstractCompilationUnitAstVisitor {
	private SimpleName variableName;
	private WhileStatement whileStatement;
	private boolean useableLoopVariable = false;
	private VariableDeclarationStatement variableDeclarationStatement = null;

	public VariableDefinitionAstVisiotr(SimpleName variableName, WhileStatement whileStatement) {
		this.variableName = variableName;
		this.whileStatement = whileStatement;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		if (node.getName().getIdentifier().equals(variableName.getIdentifier())) {
			// this case can only happen once in the scope of the tree
			useableLoopVariable = true;
			return false;
		}
		return true;
	}

	@Override
	public void endVisit(VariableDeclarationStatement node) {
		if (useableLoopVariable && variableDeclarationStatement == null) {
			variableDeclarationStatement = node;
		}

	}

	@Override
	public boolean visit(WhileStatement node) {
		if (whileStatement.equals(node)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean visit(SimpleName node) {
		if ((node.getFullyQualifiedName().equals(variableName.getFullyQualifiedName()))) {
			useableLoopVariable = false;
			variableDeclarationStatement = null;
			return false;
		}
		return true;
	}

	public VariableDeclarationStatement getVariableDeclarationStatement() {
		return variableDeclarationStatement;
	}
}