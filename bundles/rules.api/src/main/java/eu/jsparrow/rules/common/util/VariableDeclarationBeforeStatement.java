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
 * This class offers a method to determine whether or not a local variable
 * declaration fragment can be found for a specified variable name which is
 * preceding a specified Statement.
 * 
 * Example: For the following piece of code
 * 
 * <pre>
 * int x = 0;
 * if (condition) {
 * 	x = 1;
 * } else {
 * 	x = 0;
 * }
 * </pre>
 * 
 * a variable declaration fragment can be found which declares a variable with
 * the name x of the type int exactly before the if-statement, and this may be a
 * useful information for further re-factoring operations.
 * 
 * since 4.18.0
 */
public class VariableDeclarationBeforeStatement {

	/**
	 * 
	 * @param simpleName
	 *            expected variable name
	 * @param statement
	 *            statement which is expected to follow the variable declared
	 *            with the specified name.
	 * @param compilationUnit
	 *            necessary to find the object representing the
	 *            VariableDeclarationFragment
	 * @return An Optional containing a VariableDeclarationFragment representing
	 *         the declaration of a variable with the name specified by the
	 *         first parameter. Additionally, the VariableDeclarationStatement
	 *         must precede the statement described by the 2nd parameter. An
	 *         empty Optional is returned if the conditions described above are
	 *         not fulfilled completely.
	 */
	public static Optional<VariableDeclarationFragment> findDeclaringFragment(SimpleName simpleName,
			Statement statement,
			CompilationUnit compilationUnit) {

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
