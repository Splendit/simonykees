package at.splendit.simonykees.core.visitor.tryWithResource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import at.splendit.simonykees.core.visitor.AbstractCompilationUnitAstVisitor;

public class TryWithResourceASTVisitor extends AbstractCompilationUnitAstVisitor {
	
	private static final String AUTO_CLOSEABLE = "java.lang.AutoCloseable"; //$NON-NLS-1$
	private static final String CLOSEABLE = "java.io.Closeable"; //$NON-NLS-1$
	
	private TryStatement invokingTryStatement = null;
	private List<VariableDeclarationExpression> listVDE = new ArrayList<>();

	public TryWithResourceASTVisitor() {
		super();
	}
	
	private TryWithResourceASTVisitor(List<IType> itypes, TryStatement invokingTryStatement) {
		super(itypes);
		this.invokingTryStatement = invokingTryStatement;
	}

	@Override
	public boolean visit(TryStatement node) {
		if (!node.equals(invokingTryStatement)){
			TryWithResourceASTVisitor tryWithRes = new TryWithResourceASTVisitor(registeredITypes, node);
			tryWithRes.setAstRewrite(astRewrite);
			node.accept(tryWithRes);
			List<VariableDeclarationExpression> listVDE = tryWithRes.getListVDE();
			if (!listVDE.isEmpty()) {
				listVDE.forEach(iteratorNode -> astRewrite.getListRewrite(node, TryStatement.RESOURCES_PROPERTY)
						.insertLast(iteratorNode, null));
			}
			return false;
		}
		else {
			return true;
		}
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
			astRewrite.remove(node, null);
			return false;
		}
		return true;
	}

	@Override
	protected String[] relevantClasses() {
		return new String[] { AUTO_CLOSEABLE, CLOSEABLE };
	}
	
	private List<VariableDeclarationExpression> getListVDE() {
		return listVDE;
	}
}
