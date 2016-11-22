package at.splendit.simonykees.core.visitor.loop;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.visitor.AbstractCompilationUnitAstVisitor;

/**
 * For loops with an iterator can be replaced with a forEach loop since 1.7
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class ForToForEachASTVisitor extends AbstractCompilationUnitAstVisitor {

	private static String ITERATOR = "java.util.Iterator"; //$NON-NLS-1$
	
	@Override
	public boolean visit(ForStatement node) {
		if (node.getExpression() instanceof MethodInvocation) {
			MethodInvocation methodInvocation = (MethodInvocation) node.getExpression();
			// check for hasNext operation on Iterator
			
			if (StringUtils.equals("hasNext", methodInvocation.getName().getFullyQualifiedName()) //$NON-NLS-1$
					&& methodInvocation.getExpression() instanceof SimpleName) {
				SimpleName iteratorName = (SimpleName) methodInvocation.getExpression();
				if (iteratorName != null && !isContentofRegistertITypes(iteratorName.resolveTypeBinding())) {
					//Type is not an Iterator
					return false;
				}
				IteratorDefinitionAstVisior iteratorDefinitionAstVisior = new IteratorDefinitionAstVisior(iteratorName);
				if(1 == node.initializers().size()){
					((ASTNode)node.initializers().get(0)).accept(iteratorDefinitionAstVisior);
				}

				FindNextVariableAstVisitor findNextVariableAstVisitor = new FindNextVariableAstVisitor(
						(SimpleName) iteratorName);
				findNextVariableAstVisitor.setAstRewrite(this.astRewrite);
				node.getBody().accept(findNextVariableAstVisitor);
				if (findNextVariableAstVisitor.isTransformable()
						&& iteratorDefinitionAstVisior.getList() != null) {

					SimpleName iterationVariable = findNextVariableAstVisitor.getVariableName();
					Type iterationType = findNextVariableAstVisitor.getIteratorVariableType();
					// wenn der typ == null ist muss der typ wieder außerhab
					// gesucht werden

					SingleVariableDeclaration iterationVariableDefinition = NodeBuilder.newSingleVariableDeclaration(
							node.getAST(), (SimpleName) astRewrite.createMoveTarget(iterationVariable),
							(Type) astRewrite.createMoveTarget(iterationType));

					Expression iterationList = (Expression) astRewrite.createMoveTarget(iteratorDefinitionAstVisior.getList());
					
					EnhancedForStatement newFor = NodeBuilder.newEnhandesForStatement(node.getAST(),
							(Statement) astRewrite.createMoveTarget(node.getBody()), iterationList, iterationVariableDefinition);
					astRewrite.remove(findNextVariableAstVisitor.getRemoveWithTransformation(), null);
					astRewrite.replace(node, newFor, null);
				}
			}
			if (StringUtils.equals("size", methodInvocation.getName().getFullyQualifiedName()) //$NON-NLS-1$
					&& methodInvocation.getExpression() instanceof SimpleName) {
				SimpleName listName = (SimpleName)methodInvocation.getExpression();
				if (listName != null && !isContentofRegistertITypes(listName.resolveTypeBinding())) {
					//Type is not an Iterator
					return false;
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
