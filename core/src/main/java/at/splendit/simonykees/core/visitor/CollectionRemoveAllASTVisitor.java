package at.splendit.simonykees.core.visitor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class CollectionRemoveAllASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static final Logger logger = LoggerFactory.getLogger(CollectionRemoveAllASTVisitor.class);
	
	private static Integer COLLECTION_KEY = 1;
	private static String COLLECTION_FULLY_QUALLIFIED_NAME = "java.util.Collection"; //$NON-NLS-1$

	private ASTMatcher astMatcher = new ASTMatcher();

	public CollectionRemoveAllASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(COLLECTION_KEY,
				generateFullyQuallifiedNameList(COLLECTION_FULLY_QUALLIFIED_NAME));
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (StringUtils.equals("removeAll", node.getName().getFullyQualifiedName()) //$NON-NLS-1$
				&& node.getExpression() instanceof SimpleName && ClassRelationUtil.isInheritingContentOfRegistertITypes(
						node.getExpression().resolveTypeBinding(), iTypeMap.get(COLLECTION_KEY))) {

			@SuppressWarnings("unchecked")
			List<Expression> arguments = (List<Expression>) node.arguments();
			if (arguments.size() == 1 && arguments.get(0) instanceof SimpleName) {
				if (astMatcher.match((SimpleName) arguments.get(0), node.getExpression())) {
					logger.debug("replace statement"); //$NON-NLS-1$
					
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
