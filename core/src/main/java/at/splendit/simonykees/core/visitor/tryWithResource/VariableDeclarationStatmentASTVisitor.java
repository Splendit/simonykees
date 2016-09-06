package at.splendit.simonykees.core.visitor.tryWithResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import at.splendit.simonykees.core.visitor.AbstractCompilationUnitAstVisitor;


/**
 * public modifier removed, because an VariableDeclarationStatmentASTVisitor may not be unique
 * @author mgh
 *
 */
class VariableDeclarationStatmentASTVisitor extends AbstractCompilationUnitAstVisitor {

	private List<VariableDeclarationExpression> listVDE = new ArrayList<>();

	public VariableDeclarationStatmentASTVisitor(ASTRewrite astRewrite, List<IType> itypes) {
		super(astRewrite,itypes);
	}

	public VariableDeclarationStatmentASTVisitor(ASTRewrite astRewrite) {
		super(astRewrite);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ITypeBinding typeBind = node.getType().resolveBinding();
		if (isContentofRegistertITypes(typeBind)) {
			Collection<Object> removeList = new HashSet<>();
			for (Object iterator : node.fragments()) {
				if (iterator instanceof VariableDeclarationFragment) {
					VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) iterator;
					VariableDeclarationExpression variableDeclarationExpression = node.getAST()
							.newVariableDeclarationExpression((VariableDeclarationFragment) ASTNode
									.copySubtree(variableDeclarationFragment.getAST(), variableDeclarationFragment));
					removeList.add(iterator);
					variableDeclarationExpression.setType((Type) ASTNode.copySubtree(node.getAST(), node.getType()));
					listVDE.add(variableDeclarationExpression);
				}
			}
			node.delete();
		}
		return false;
	}

	public List<VariableDeclarationExpression> getListVDE() {
		return listVDE;
	}

	public void setListVDE(List<VariableDeclarationExpression> listVDE) {
		this.listVDE = listVDE;
	}
}
