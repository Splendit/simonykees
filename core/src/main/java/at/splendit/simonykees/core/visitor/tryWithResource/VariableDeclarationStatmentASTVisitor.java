package at.splendit.simonykees.core.visitor.tryWithResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;


/**
 * public modifier removed, because an VariableDeclarationStatmentASTVisitor may not be unique
 * @author mgh
 *
 */
class VariableDeclarationStatmentASTVisitor extends ASTVisitor {

	private ASTRewrite astRewrite;
	private List<VariableDeclarationExpression> listVDE = new ArrayList<>();
	private List<IType> itypes;

	public VariableDeclarationStatmentASTVisitor(ASTRewrite astRewrite, List<IType> itypes) {
		this(astRewrite);
		this.itypes = new ArrayList<>();
		this.itypes.addAll(itypes);
	}

	public VariableDeclarationStatmentASTVisitor(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ITypeBinding typeBind = node.getType().resolveBinding();
		if (isSubInterface(typeBind)) {
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
					astRewrite.remove(node, null);
				}
			}
		}
		return false;
	}

	private boolean isSubInterface(ITypeBinding iTypeBinding) {
		boolean result = false;
		if (iTypeBinding == null) {
			return false;
		}
		for (ITypeBinding interfaceBind : iTypeBinding.getInterfaces()) {
			if (itypes.contains(interfaceBind.getJavaElement())) {
				result = result || true;
			}
			result = result || isSubInterface(interfaceBind.getSuperclass());
		}
		return result || isSubInterface(iTypeBinding.getSuperclass());
	}

	public List<VariableDeclarationExpression> getListVDE() {
		return listVDE;
	}

	public void setListVDE(List<VariableDeclarationExpression> listVDE) {
		this.listVDE = listVDE;
	}
}
