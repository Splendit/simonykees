package at.splendit.simonykees.core.visitor;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
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
		if (node.getExpression() instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) node.getExpression();
			// check for hasNext operation on Iterator
			if (StringUtils.equals("hasNext", methodInvocation.getName().getFullyQualifiedName())) { //$NON-NLS-1$
				Expression iteratorExpression = methodInvocation.getExpression();
				if (iteratorExpression != null && iteratorExpression instanceof SimpleName) {
					ITypeBinding iteratorBinding = iteratorExpression.resolveTypeBinding();
					if (isContentofRegistertITypes(iteratorBinding)) {
						ASTNode parentNode = node.getParent();
						while (!(parentNode instanceof Block) && parentNode != null) {
							parentNode = parentNode.getParent();
						}
						if (parentNode == null) {
							// No surrounding parent block found
							// should not happen, because the Iterator has to be
							// defined in an parent block.
							return false;
						}
						IteratorDefinitionAstVisior iteratorDefinitionAstVisior = new IteratorDefinitionAstVisior(
								(SimpleName) iteratorExpression);
						iteratorDefinitionAstVisior.setAstRewrite(this.astRewrite);
						parentNode.accept(iteratorDefinitionAstVisior);
						FindNextVariableAstVisitor findNextVariableAstVisitor = new FindNextVariableAstVisitor(
								(SimpleName) iteratorExpression);
						findNextVariableAstVisitor.setAstRewrite(this.astRewrite);
						node.getBody().accept(findNextVariableAstVisitor);
						if (iteratorDefinitionAstVisior.getList() != null
								&& findNextVariableAstVisitor.getVariableName() != null
								&& findNextVariableAstVisitor.isTransformable()) {
							EnhancedForStatement newFor = node.getAST().newEnhancedForStatement();
							newFor.setBody((Statement) astRewrite.createMoveTarget(node.getBody()));
							newFor.setExpression(
									(Expression) astRewrite.createMoveTarget(iteratorDefinitionAstVisior.getList()));
							// need to find the parameter in the block!
							SingleVariableDeclaration svd = node.getAST().newSingleVariableDeclaration();
							svd.setName((SimpleName) astRewrite
									.createMoveTarget(findNextVariableAstVisitor.getVariableName()));
							svd.setType((Type) astRewrite
									.createMoveTarget(findNextVariableAstVisitor.getIteratorVariableType()));
							newFor.setParameter(svd);
							astRewrite.replace(node, newFor, null);
							// executed here, because a breaking statement can
							// be found after the setting of the type
							this.astRewrite.remove(findNextVariableAstVisitor.getIteratorVariableType().getParent(),
									null);
							this.astRewrite.remove(iteratorDefinitionAstVisior.getIteratorDeclarationStatement(), null);
						}
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

	private class IteratorDefinitionAstVisior extends AbstractCompilationUnitAstVisitor {
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
	}

	/**
	 * also checks if remove or forEachRemaining is used on the iterator.
	 * 
	 * @author mgh
	 *
	 */
	private class FindNextVariableAstVisitor extends AbstractCompilationUnitAstVisitor {
		private SimpleName iteratorName;

		private Type iteratorVariableType = null;
		private SimpleName variableName = null;
		private boolean transformable = false;

		public FindNextVariableAstVisitor(SimpleName iteratorName) {
			this.iteratorName = iteratorName;
		}

		@Override
		public void endVisit(VariableDeclarationFragment node) {
			if (transformable){
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
				}
				else if ("next".equals(node.getName().getFullyQualifiedName())) { //$NON-NLS-1$
					if(transformable){
						iteratorVariableType = null;
						variableName = null;
						transformable = false;
						return false;
					}
					//this.astRewrite.remove(node.getInitializer(), null);
					//
					transformable = true;
					return true;
				}
			}
			return true;
		}

		@Override
		public void endVisit(VariableDeclarationStatement node) {
			if (transformable) {
				iteratorVariableType = node.getType();
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
	}
}
