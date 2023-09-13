package eu.jsparrow.core.visitor.impl.inline;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;

/**
 * Contains all structural information which is necessary for the
 * InlineLocalVariablesASTVisitor to carry out the transformation operations if
 * the variable which is described by the LocalVariableDeclarationData can be
 * in-lined.
 * 
 */
class InLineLocalVariablesAnalysisData {
	private final Block block;
	private final LocalVariableDeclarationData localVariableDeclarationData;
	private final Statement statementWithSimpleNameToReplace;
	private final SimpleName simpleNameToReplace;

	static Optional<InLineLocalVariablesAnalysisData> findAnalysisData(ThrowStatement throwStatement) {
		return findAnalysisData(throwStatement, throwStatement.getExpression());
	}

	static Optional<InLineLocalVariablesAnalysisData> findAnalysisData(ReturnStatement returnStatement) {
		Expression returnStatementExpression = returnStatement.getExpression();
		if (returnStatementExpression == null) {
			return Optional.empty();
		}
		return findAnalysisData(returnStatement, returnStatementExpression);
	}

	private static Optional<InLineLocalVariablesAnalysisData> findAnalysisData(Statement statement,
			Expression statementExpression) {
		if (statement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return Optional.empty();
		}
		Block block = (Block) statement.getParent();
		if (statementExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Optional.empty();
		}
		SimpleName simpleName = (SimpleName) statementExpression;
		String identifier = simpleName.getIdentifier();
		return LocalVariableDeclarationData.findData(block, identifier)
			.map(variableDeclarationData -> new InLineLocalVariablesAnalysisData(block, variableDeclarationData,
					statement, simpleName));
	}

	private InLineLocalVariablesAnalysisData(Block block, LocalVariableDeclarationData localVariableDeclarationData,
			Statement statementWithSimpleNameToReplace, SimpleName simpleNameToReplace) {
		this.block = block;
		this.localVariableDeclarationData = localVariableDeclarationData;
		this.statementWithSimpleNameToReplace = statementWithSimpleNameToReplace;
		this.simpleNameToReplace = simpleNameToReplace;
	}

	public Block getBlock() {
		return block;
	}

	public LocalVariableDeclarationData getLocalVariableDeclarationData() {
		return localVariableDeclarationData;
	}

	public Statement getStatementWithSimpleNameToReplace() {
		return statementWithSimpleNameToReplace;
	}

	public SimpleName getSimpleNameToReplace() {
		return simpleNameToReplace;
	}
}
