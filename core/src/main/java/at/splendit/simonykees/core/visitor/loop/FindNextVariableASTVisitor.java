package at.splendit.simonykees.core.visitor.loop;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import at.splendit.simonykees.core.visitor.AbstractCompilationUnitAstVisitor;

/**
 * also checks if remove or forEachRemaining is used on the iterator.
 * 
 * @since 9.2.0
 * @author Martin Huter
 *
 */
class FindNextVariableAstVisitor extends AbstractCompilationUnitAstVisitor {
	private SimpleName iteratorName;

	private Type iteratorVariableType = null;
	private SimpleName variableName = null;
	private Statement removeWithTransformation;
	private boolean transformable = false;
	private boolean doubleNext = false;

	public FindNextVariableAstVisitor(SimpleName iteratorName) {
		this.iteratorName = iteratorName;
	}

	@Override
	public void endVisit(VariableDeclarationFragment node) {
		if (transformable) {
			variableName = node.getName();
		}
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (new ASTMatcher().match(iteratorName, node.getExpression())) {
			if ("remove".equals(node.getName().getFullyQualifiedName()) || //$NON-NLS-1$
					"forEachRemaining".equals(node.getName().getFullyQualifiedName())) { //$NON-NLS-1$
				transformable = false;
				return false;
			} else if ("next".equals(node.getName().getFullyQualifiedName())) { //$NON-NLS-1$
				if (transformable || doubleNext) {
					iteratorVariableType = null;
					variableName = null;
					transformable = false;
					doubleNext = true;
					return false;
				}
				// this.astRewrite.remove(node.getInitializer(), null);
				//
				transformable = true;
				return true;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void endVisit(VariableDeclarationStatement node) {
		if (transformable && iteratorVariableType == null && variableName != null) {
			for (VariableDeclarationFragment fragment : (List<VariableDeclarationFragment>) node.fragments()) {
				if (fragment.getName().getFullyQualifiedName().equals(variableName.getFullyQualifiedName())) {
					iteratorVariableType = node.getType();
					removeWithTransformation = node;
					break;
				}
			}
		}
	}

	@Override
	public void endVisit(Assignment node) {
		if (transformable && variableName == null) {
			if (node.getLeftHandSide() instanceof SimpleName) {
				if (node.getParent() instanceof Statement) {
					variableName = (SimpleName) node.getLeftHandSide();
					removeWithTransformation = (Statement) node.getParent();
				}
			} else {
				transformable = false;
			}
		}

	}

	public Type getIteratorVariableType() {
		return iteratorVariableType;
	}

	public SimpleName getVariableName() {
		return variableName;
	}

	public boolean isTransformable() {
		return transformable;
	}

	public Statement getRemoveWithTransformation() {
		return removeWithTransformation;
	}

}