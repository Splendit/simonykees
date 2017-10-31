package eu.jsparrow.core.visitor.loop;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * A functional interface for pointing to a method that constructs a
 * {@link LoopIteratingIndexASTVisitor}.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 * @param <T>
 *            represents the type of the loop node. Expected to be either
 *            {@link ForStatement} or {@link WhileStatement}.
 */
@FunctionalInterface
public interface IteratingIndexVisitorFactory<T extends Statement> {
	LoopIteratingIndexASTVisitor create(SimpleName iteratingIndexName, SimpleName iterableName, T loopStatement,
			Block scopeBlock);
}
