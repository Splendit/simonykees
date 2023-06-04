package eu.jsparrow.rules.common.util;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

/**
 * 
 * since 4.18.0
 */
public class VariableDeclarationBeforeStatement {

	public static Optional<VariableDeclarationFragment> findDeclaringFragment(Expression assigned, Statement statement,
			CompilationUnit compilationUnit) {

		if (assigned.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Optional.empty();
		}
		SimpleName simpleName = (SimpleName) assigned;

		IBinding binding = simpleName.resolveBinding();
		ASTNode declaringNode = compilationUnit.findDeclaringNode(binding);
		if (declaringNode == null) {
			return Optional.empty();
		}
		int declaringNodeType = declaringNode.getNodeType();
		if (declaringNodeType != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return Optional.empty();
		}
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) declaringNode;
		if (fragment.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			return Optional.empty();
		}
		VariableDeclarationStatement declaraingStatement = (VariableDeclarationStatement) fragment.getParent();
		if (declaraingStatement.fragments()
			.size() != 1) {
			return Optional.empty();
		}
		boolean areStatementsInBlock = declaraingStatement.getLocationInParent() == Block.STATEMENTS_PROPERTY
				&& statement.getLocationInParent() == Block.STATEMENTS_PROPERTY;
		if (!areStatementsInBlock) {
			return Optional.empty();
		}
		Block declarationParent = (Block) declaraingStatement.getParent();
		Block statementParent = (Block) statement.getParent();
		if (statementParent != declarationParent) {
			return Optional.empty();
		}
		List<Statement> blockStatements = ASTNodeUtil.convertToTypedList(declarationParent.statements(),
				Statement.class);
		int declarationIndex = blockStatements.indexOf(declaraingStatement);
		int statementIndex = blockStatements.indexOf(statement);

		if (statementIndex != declarationIndex + 1) {
			return Optional.empty();
		}

		Expression initializer = fragment.getInitializer();
		if (initializer != null && initializer.getNodeType() != ASTNode.SIMPLE_NAME
				&& !ASTNodeUtil.isLiteral(initializer)) {
			return Optional.empty();
		}
		return Optional.of(fragment);
	}

	private VariableDeclarationBeforeStatement() {

	}

}
