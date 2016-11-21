package at.splendit.simonykees.core.visitor.loop;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import at.splendit.simonykees.core.visitor.AbstractCompilationUnitAstVisitor;

/**
 * 
 * @since 9.2.0
 * @author mgh
 *
 */
class IteratorDefinitionAstVisior extends AbstractCompilationUnitAstVisitor {

	private static String ITERATOR = "java.util.Iterator"; //$NON-NLS-1$
	
	private SimpleName iteratorName;
	private Expression listName = null;
	private VariableDeclarationStatement iteratorDeclarationStatement = null;

	public IteratorDefinitionAstVisior(SimpleName iteratorName) {
		this.iteratorName = iteratorName;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		if (node.getName().getIdentifier().equals(iteratorName.getIdentifier())) {
			if (node.getInitializer() instanceof MethodInvocation) {
				MethodInvocation nodeInitializer = (MethodInvocation) node.getInitializer();
				if ("iterator".equals(nodeInitializer.getName().getFullyQualifiedName())) { //$NON-NLS-1$
					listName = nodeInitializer.getExpression();
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void endVisit(VariableDeclarationStatement node) {
		if (listName != null && iteratorDeclarationStatement == null) {
			iteratorDeclarationStatement = node;
		}

	}

	public Expression getList() {
		return listName;
	}

	public VariableDeclarationStatement getIteratorDeclarationStatement() {
		return iteratorDeclarationStatement;
	}
	
	@Override
	protected String[] relevantClasses() {
		return new String[] { ITERATOR };
	}
}