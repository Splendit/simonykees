package eu.jsparrow.rules.java16.patternmatching;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;

/**
 * Helper class to determine whether two instances of {@link Expression} are
 * either representing
 * <ul>
 * <li>the same local variable or</li>
 * <li>the same field</li>
 * </ul>
 * 
 * @since 4.2.0
 *
 */
public class ExpressionMatcher {

	private final ASTMatcher astMatcher = new ASTMatcher();

	/**
	 * @return {@code true} if both expressions represent the same local
	 *         variable or the same field, otherwise {@code false}.
	 */
	boolean match(Expression first, Expression second) {

		if (first.getNodeType() == ASTNode.SIMPLE_NAME) {
			return astMatcher.match((SimpleName) first, second);
		}

		if (first.getNodeType() == ASTNode.QUALIFIED_NAME) {
			return astMatcher.match((QualifiedName) first, second);
		}

		if (first.getNodeType() == ASTNode.FIELD_ACCESS) {
			return astMatcher.match((FieldAccess) first, second);
		}

		if (first.getNodeType() == ASTNode.SUPER_FIELD_ACCESS) {
			return astMatcher.match((SuperFieldAccess) first, second);
		}

		return false;
	}

}
