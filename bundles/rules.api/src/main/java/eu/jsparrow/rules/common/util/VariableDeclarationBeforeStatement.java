package eu.jsparrow.rules.common.util;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
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
			Statement statement, CompilationUnit compilationUnit) {

		VariableDeclarationStatement declarationStatementBefore = ASTNodeUtil
			.findPreviousStatementInBlock(statement, VariableDeclarationStatement.class)
			.orElse(null);

		if (declarationStatementBefore == null) {
			return Optional.empty();
		}

		VariableDeclarationFragment declarationFragment = ASTNodeUtil
			.findSingletonListElement(declarationStatementBefore.fragments(), VariableDeclarationFragment.class)
			.orElse(null);

		if (declarationFragment == null) {
			return Optional.empty();
		}

		if (hasInitializerWithSideEffect(declarationFragment)) {
			return Optional.empty();
		}

		IBinding binding = simpleName.resolveBinding();
		ASTNode declaringNode = compilationUnit.findDeclaringNode(binding);

		if (declaringNode != declarationFragment) {
			return Optional.empty();
		}

		return Optional.of(declarationFragment);
	}

	private static boolean hasInitializerWithSideEffect(VariableDeclarationFragment fragment) {
		Expression initializer = fragment.getInitializer();
		if (initializer == null) {
			return false;
		}
		if (ASTNodeUtil.isLiteral(initializer)) {
			return false;
		}
		return !VariableWithoutSideEffect.isVariableWithoutSideEffect(initializer);
	}

	private VariableDeclarationBeforeStatement() {

	}
}
