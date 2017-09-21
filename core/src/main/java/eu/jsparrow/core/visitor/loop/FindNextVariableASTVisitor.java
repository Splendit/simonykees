package eu.jsparrow.core.visitor.loop;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

/**
 * also checks if remove or forEachRemaining is used on the iterator.
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
class FindNextVariableASTVisitor extends AbstractASTRewriteASTVisitor {
	private SimpleName iteratorName;

	private Type iteratorVariableType = null;
	private SimpleName variableName = null;
	private Statement removeWithTransformation;
	private boolean transformable = false;
	private boolean doubleNext = false;

	public FindNextVariableASTVisitor(SimpleName iteratorName) {
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
			if ("next".equals(node.getName().getFullyQualifiedName())) { //$NON-NLS-1$
				if (transformable || doubleNext) {
					iteratorVariableType = null;
					variableName = null;
					transformable = false;
					doubleNext = true;
					return false;
				}
				transformable = true;
				return true;
			} else {
				transformable = false;
				return false;
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