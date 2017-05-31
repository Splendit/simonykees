package at.splendit.simonykees.core.visitor;

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class LambdaForEachMapASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		
		return true;
	}
}
