package at.splendit.simonykees.core.visitor.tryWithResource;

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import at.splendit.simonykees.core.visitor.AbstractCompilationUnitAstVisitor;

public class TryWithResourceASTVisitor extends AbstractCompilationUnitAstVisitor {


	public TryWithResourceASTVisitor(ASTRewrite astRewrite, List<IType> itypes) {
		super(astRewrite, itypes);
	}

	public TryWithResourceASTVisitor(ASTRewrite astRewrite) {
		super(astRewrite);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(TryStatement node) {
		VariableDeclarationStatmentASTVisitor variableDeclarationStatmentASTVisitor = new VariableDeclarationStatmentASTVisitor(
				astRewrite, registeredITypes);
		node.accept(variableDeclarationStatmentASTVisitor);
		List<VariableDeclarationExpression> listVDE = variableDeclarationStatmentASTVisitor.getListVDE();
		if (!listVDE.isEmpty()) {
			TryStatement replacementNode = (TryStatement) ASTNode.copySubtree(node.getAST(), node);
			replacementNode.resources().addAll(listVDE);
			astRewrite.replace(node, replacementNode, null);
		}
		return false;
	}

	@Override
	protected String[] relevantClasses() {
		return new String[] { "java.lang.AutoCloseable", "java.io.Closeable" };
	}

}
