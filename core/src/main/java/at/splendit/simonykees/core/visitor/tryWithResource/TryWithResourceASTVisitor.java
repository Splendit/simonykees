package at.splendit.simonykees.core.visitor.tryWithResource;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class TryWithResourceASTVisitor extends ASTVisitor {

	private ASTRewrite astRewrite;
	private List<IType> itypes;

	public TryWithResourceASTVisitor(ASTRewrite astRewrite, List<IType> itypes) {
		this(astRewrite);
		this.itypes = new ArrayList<>();
		this.itypes.addAll(itypes);
	}

	public TryWithResourceASTVisitor(ASTRewrite astRewrite) {
		this.astRewrite = astRewrite;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(TryStatement node) {
		VariableDeclarationStatmentASTVisitor variableDeclarationStatmentASTVisitor = new VariableDeclarationStatmentASTVisitor(
				astRewrite, itypes);
		TryStatement replacementNode = (TryStatement) ASTNode.copySubtree(node.getAST(), node);
		node.accept(variableDeclarationStatmentASTVisitor);
		List<VariableDeclarationExpression> listVDE = variableDeclarationStatmentASTVisitor.getListVDE();
		if (!listVDE.isEmpty()) {
			replacementNode.resources().addAll(listVDE);
			astRewrite.replace(node, replacementNode, null);
		}
		return false;
	}
}
