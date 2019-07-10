package eu.jsparrow.rules.common.util;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

import eu.jsparrow.rules.common.visitor.helper.RemoveGeneratedNodesASTVisitor;

/**
 * This class is used to remove {@link ASTNode}s that contain the
 * {@code $isGenerated} field and have it set to true.
 * <p>
 * As soon as one node does not contain the {@code $isGenerated} field, no
 * further removal attempts will be performed.
 * <p>
 * Background: We need to remove those Lombok generated nodes because rewriting
 * them in any visitor will result in a MalformedTreeException (see SIM-1578).
 * 
 * @since 3.7.0
 */
public class RemoveGeneratedNodesUtil {

	private static boolean needsChecking = true;

	/**
	 * See {@link RemoveGeneratedNodesASTVisitor} for more details.
	 * 
	 * @param astRoot
	 *            the {@link CompilationUnit} where generated nodes should be
	 *            removed.
	 */
	public static void removeAllGeneratedNodes(CompilationUnit astRoot) {
		if (needsChecking) {
			RemoveGeneratedNodesASTVisitor visitor = new RemoveGeneratedNodesASTVisitor();
			astRoot.accept(visitor);

			if (!visitor.isHasIsGeneratedField()) {
				needsChecking = false;
			}
		}
	}

}
