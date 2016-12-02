package at.splendit.simonykees.core.util;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;

/**
 * TODO SIM-103 add class description
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class ASTNodeUtil {

	/**
	 * Finds the surrounding Block node if there is one, otherwise returns null
	 * 
	 * @param node
	 *            ASTNode where the backward search is started
	 * @return surrounding {@link Block}, null if non exists
	 */
	public static Block getSurroundingBlock(ASTNode node) {
		if (node == null) {
			return null;
		}
		if (node.getParent() instanceof Block) {
			return (Block) node.getParent();
		} else {
			return getSurroundingBlock(node.getParent());
		}
	}
}
