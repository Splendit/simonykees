package eu.jsparrow.rules.common.visitor.helper;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;

/**
 * Checks whether an expression contains other {@link InfixExpression},
 * {@link PrefixExpression} or {@link PostfixExpression} expressions.
 * 
 * @since 2.7.0
 *
 */
public class SimpleExpressionVisitor extends ASTVisitor {

	private boolean isSimple = true;

	@Override
	public boolean preVisit2(ASTNode node) {
		return isSimple;
	}

	@Override
	public boolean visit(InfixExpression expression) {
		isSimple = false;
		return true;
	}

	@Override
	public boolean visit(PostfixExpression expression) {
		isSimple = false;
		return true;
	}

	@Override
	public boolean visit(PrefixExpression expression) {
		isSimple = false;
		return true;
	}

	public boolean isSimple() {
		return isSimple;
	}

}