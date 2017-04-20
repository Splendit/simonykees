package at.splendit.simonykees.core.visitor;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import at.splendit.simonykees.core.Activator;
import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * An Collection that removes it from itself is replaced with clear
 * collectionName.removeAll(collectionName) -> collectionName.clear()
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class CollectionRemoveAllASTVisitor extends AbstractASTRewriteASTVisitor {

	private static String COLLECTION_FULLY_QUALLIFIED_NAME = "java.util.Collection"; //$NON-NLS-1$

	private ASTMatcher astMatcher = new ASTMatcher();

	@Override
	public boolean visit(MethodInvocation node) {
		if (StringUtils.equals("removeAll", node.getName().getFullyQualifiedName()) //$NON-NLS-1$
				&& node.getExpression() instanceof SimpleName && ClassRelationUtil.isInheritingContentOfTypes(
						node.getExpression().resolveTypeBinding(), Collections.singletonList(COLLECTION_FULLY_QUALLIFIED_NAME))) {

			@SuppressWarnings("unchecked")
			List<Expression> arguments = (List<Expression>) node.arguments();
			if (arguments.size() == 1 && arguments.get(0) instanceof SimpleName) {
				if (astMatcher.match((SimpleName) arguments.get(0), node.getExpression())) {
					Activator.log("replace statment"); //$NON-NLS-1$

					SimpleName clear = node.getAST().newSimpleName("clear"); //$NON-NLS-1$
					MethodInvocation newMI = NodeBuilder.newMethodInvocation(node.getAST(),
							(Expression) astRewrite.createMoveTarget(node.getExpression()), clear);
					astRewrite.replace(node, newMI, null);
				}
			}
		}
		return true;
	}
}
