package eu.jsparrow.core.visitor.impl.inline;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;

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
		return findLocalVariableDeclarationData(statement, identifier)
			.map(variableDeclarationData -> new InLineLocalVariablesAnalysisData(block, variableDeclarationData,
					statement, simpleName));
	}

	private static Optional<LocalVariableDeclarationData> findLocalVariableDeclarationData(Statement statement,
			String expectedIdentifier) {
		VariableDeclarationStatement precedingDeclarationStatement = ASTNodeUtil
			.findPreviousStatementInBlock(statement, VariableDeclarationStatement.class)
			.orElse(null);
		if (precedingDeclarationStatement == null) {
			return Optional.empty();
		}
		VariableDeclarationFragment uniqueDeclarationFragment = ASTNodeUtil
			.findSingletonListElement(precedingDeclarationStatement.fragments(),
					VariableDeclarationFragment.class)
			.orElse(null);

		if (uniqueDeclarationFragment == null) {
			return Optional.empty();
		}

		Expression initializer = uniqueDeclarationFragment.getInitializer();
		if (initializer == null) {
			return Optional.empty();
		}
		String identifier = uniqueDeclarationFragment.getName()
			.getIdentifier();
		if (!identifier.equals(expectedIdentifier)) {
			return Optional.empty();
		}

		return Optional.of(new LocalVariableDeclarationData(precedingDeclarationStatement, uniqueDeclarationFragment,
				initializer));

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
