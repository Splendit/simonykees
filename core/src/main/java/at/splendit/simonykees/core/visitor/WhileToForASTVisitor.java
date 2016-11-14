package at.splendit.simonykees.core.visitor;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.WhileStatement;

/**
 * While-loops over Iterators that could be expressed with a for-loop are
 * transformed to a equivalent for-loop.
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class WhileToForASTVisitor extends AbstractCompilationUnitAstVisitor {
	
	private static String ITERATOR = "java.util.Iterator"; //$NON-NLS-1$
	
	@Override
	public boolean visit(WhileStatement node) {
		if(node.getExpression() instanceof MethodInvocation){
			MethodInvocation methodInvocation = (MethodInvocation) node.getExpression();
			//check for hasNext operation on Iterator
			if(StringUtils.equals("hasNext",methodInvocation.getName().getFullyQualifiedName())){ //$NON-NLS-1$
				Expression iteratorExpression = methodInvocation.getExpression();
				if(iteratorExpression != null){
					ITypeBinding iteratorBinding = iteratorExpression.resolveTypeBinding();
					if(isContentofRegistertITypes(iteratorBinding)){
						//TODO iterator in head of while found resolve transformation
					}
				}
			}
		}
		return true;
	}
	
	@Override
	protected String[] relevantClasses() {
		return new String[] { ITERATOR };
	}
}
