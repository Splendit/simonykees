package eu.jsparrow.rules.java16.switchexpression.ifstatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;

public class MultiBranchIfStatement {
	private final IfStatement ifStatement;
	private final List<IfStatement> elseIfStatements;
	private Statement lastElseStatement;

	public static Optional<MultiBranchIfStatement> toMultiBranchIfStatement(IfStatement ifStatement) {
		List<IfStatement> elseIfStatements = new ArrayList<>();
		Statement elseStatement = ifStatement.getElseStatement();
		while (elseStatement != null && elseStatement.getNodeType() == ASTNode.IF_STATEMENT) {
			IfStatement eliseIfStatement = (IfStatement) elseStatement;
			elseIfStatements.add(eliseIfStatement);
			elseStatement = eliseIfStatement.getElseStatement();
		}
		if (elseStatement != null && !elseIfStatements.isEmpty()) {
			return Optional.of(new MultiBranchIfStatement(ifStatement, elseIfStatements, elseStatement));
		} else if (elseIfStatements.size() >= 2) {
			return Optional.of(new MultiBranchIfStatement(ifStatement, elseIfStatements));
		}
		return Optional.empty();
	}

	private MultiBranchIfStatement(IfStatement ifStatement, List<IfStatement> elseIfStatements,
			Statement lastElseStatement) {
		this(ifStatement, elseIfStatements);
		this.lastElseStatement = lastElseStatement;
	}

	private MultiBranchIfStatement(IfStatement ifStatement, List<IfStatement> elseIfStatements) {
		this.ifStatement = ifStatement;
		this.elseIfStatements = elseIfStatements;
	}

	public IfStatement getIfStatement() {
		return ifStatement;
	}

	public List<IfStatement> getElseIfStatements() {
		return elseIfStatements;
	}

	public Optional<Statement> getLastElseStatement() {
		return Optional.ofNullable(lastElseStatement);
	}
}
