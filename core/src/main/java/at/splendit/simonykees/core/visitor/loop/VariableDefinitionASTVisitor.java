package at.splendit.simonykees.core.visitor.loop;

import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import at.splendit.simonykees.core.visitor.AbstractCompilationUnitASTVisitor;

/**
 * Finds the definition of the variable thats given at construction. Checks if
 * the variable is only used in the statement of the executed AST
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
class VariableDefinitionASTVisitor extends AbstractCompilationUnitASTVisitor {
	private SimpleName variableName;
	private Statement statement;
	private boolean useableLoopVariable = false;
	private VariableDeclarationStatement variableDeclarationStatement = null;

	public VariableDefinitionASTVisitor(SimpleName variableName, Statement statement) {
		this.variableName = variableName;
		this.statement = statement;
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
		if (statement.equals(node)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean visit(ForStatement node) {
		if (statement.equals(node)) {
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

	public boolean isUseableLoopVariable() {
		return useableLoopVariable;
	}
}