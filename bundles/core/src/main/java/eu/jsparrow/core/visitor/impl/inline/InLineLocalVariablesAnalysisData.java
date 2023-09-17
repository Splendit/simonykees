package eu.jsparrow.core.visitor.impl.inline;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
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
	private final SupportedVariableData localVariableDeclarationData;
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
		if (statementExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Optional.empty();
		}
		
		SimpleName simpleName = (SimpleName) statementExpression;
		String identifier = simpleName.getIdentifier();

		VariableDeclarationStatement precedingDeclarationStatement = ASTNodeUtil
			.findPreviousStatementInBlock(statement, VariableDeclarationStatement.class)
			.orElse(null);
		if (precedingDeclarationStatement == null) {
			return Optional.empty();
		}

		return SupportedVariableData.extractVariableData(precedingDeclarationStatement, identifier)
			.filter(variableDeclarationData -> {
				if (statement.getNodeType() == ASTNode.THROW_STATEMENT) {
					Expression initializer = variableDeclarationData.getInitializer();
					if (initializer.getNodeType() == ASTNode.NULL_LITERAL) {
						return false;
					}
				}
				return true;
			})
			.map(variableDeclarationData -> new InLineLocalVariablesAnalysisData(variableDeclarationData,
					statement, simpleName));
	}

	private InLineLocalVariablesAnalysisData(SupportedVariableData localVariableDeclarationData,
			Statement statementWithSimpleNameToReplace, SimpleName simpleNameToReplace) {
		this.localVariableDeclarationData = localVariableDeclarationData;
		this.statementWithSimpleNameToReplace = statementWithSimpleNameToReplace;
		this.simpleNameToReplace = simpleNameToReplace;
	}

	SupportedVariableData getLocalVariableDeclarationData() {
		return localVariableDeclarationData;
	}

	Statement getStatementWithSimpleNameToReplace() {
		return statementWithSimpleNameToReplace;
	}

	SimpleName getSimpleNameToReplace() {
		return simpleNameToReplace;
	}
}
