package at.splendit.simonykees.core.visitor.tryWithResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class TryWithResourceASTVisitor extends ASTVisitor {

	private ASTRewrite astRewrite;
	private List<VariableDeclarationExpression> listVDE = new ArrayList<>();
	private List<IType> itypes;

	public TryWithResourceASTVisitor(ASTRewrite astRewrite, List<IType> itypes) {
		this(astRewrite);
		this.itypes = new ArrayList<>();
		this.itypes.addAll(itypes);
	}

	public TryWithResourceASTVisitor(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
	}

	@Override
	public boolean visit(TryStatement node) {
		return true;
	}

	public void endVisit(TryStatement node) {
		if (!listVDE.isEmpty()) {
			TryStatement replacementNode = (TryStatement) ASTNode.copySubtree(node.getAST(), node);
			replacementNode.resources().addAll(listVDE);
			astRewrite.replace(node, replacementNode, null);
		}
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ITypeBinding typeBind = node.getType().resolveBinding();
		if (isSubInterface(typeBind)) {
			System.out.println(typeBind.getName() + " inherits from :" + itypes.toString());
			boolean changed = false;
			Collection<Object> removeList = new HashSet();
			for (Object iterator : node.fragments()) {
				if (iterator instanceof VariableDeclarationFragment) {
					VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) iterator;
					VariableDeclarationExpression variableDeclarationExpression = node.getAST()
							.newVariableDeclarationExpression((VariableDeclarationFragment) ASTNode
									.copySubtree(variableDeclarationFragment.getAST(), variableDeclarationFragment));
					removeList.add(iterator);
					variableDeclarationExpression.setType((Type) ASTNode.copySubtree(node.getAST(), node.getType()));
					listVDE.add(variableDeclarationExpression);
					changed = true;
				}
			}
			if (changed) {
				removeList.removeAll(node.fragments());
				if (removeList.isEmpty()) {
					astRewrite.remove(node, null);
				} else {
					VariableDeclarationStatement replacementNode = (VariableDeclarationStatement) ASTNode
							.copySubtree(node.getAST(), node);
					astRewrite.replace(node, replacementNode, null);
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
