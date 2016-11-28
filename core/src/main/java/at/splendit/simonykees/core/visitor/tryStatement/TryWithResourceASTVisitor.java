package at.splendit.simonykees.core.visitor.tryStatement;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.AbstractCompilationUnitASTVisitor;

/**
 * The {@link TryWithResourceASTVisitor} is used to find resources in an
 * Try-Block and moves it to the resource-head of try. A resource is a source
 * that implements {@link Closeable} or {@link AutoCloseable}
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */

public class TryWithResourceASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static final Integer AUTO_CLOSEABLE_KEY = 1;
	private static final String AUTO_CLOSEABLE_FULLY_QUALLIFIED_NAME = "java.lang.AutoCloseable"; //$NON-NLS-1$
	private static final String CLOSEABLE_FULLY_QUALLIFIED_NAME = "java.io.Closeable"; //$NON-NLS-1$

	private TryStatement invokingTryStatement = null;
	private List<VariableDeclarationExpression> listVDE = new ArrayList<>();

	public TryWithResourceASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(AUTO_CLOSEABLE_KEY,
				generateFullyQuallifiedNameList(AUTO_CLOSEABLE_FULLY_QUALLIFIED_NAME, CLOSEABLE_FULLY_QUALLIFIED_NAME));
	}

	private TryWithResourceASTVisitor(Map<Integer, List<IType>> iTypeMap, TryStatement invokingTryStatement) {
		this();
		this.iTypeMap = iTypeMap;
		this.invokingTryStatement = invokingTryStatement;
	}

	@Override
	public boolean visit(TryStatement node) {
		if (!node.equals(invokingTryStatement)) {
			TryWithResourceASTVisitor tryWithRes = new TryWithResourceASTVisitor(iTypeMap, node);
			tryWithRes.setAstRewrite(astRewrite);
			node.accept(tryWithRes);
			List<VariableDeclarationExpression> listVDE = tryWithRes.getListVDE();
			if (!listVDE.isEmpty()) {
				listVDE.forEach(iteratorNode -> astRewrite.getListRewrite(node, TryStatement.RESOURCES_PROPERTY)
						.insertLast(iteratorNode, null));
			}
			return false;
		} else {
			return true;
		}
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ITypeBinding typeBind = null;
		typeBind = node.getType().resolveBinding();
		if (ClassRelationUtil.isInheritingContentOfRegistertITypes(typeBind, iTypeMap.get(AUTO_CLOSEABLE_KEY))) {
			for (Object iterator : node.fragments()) {
				if (iterator instanceof VariableDeclarationFragment) {
					VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) iterator;
					VariableDeclarationExpression variableDeclarationExpression = node.getAST()
							.newVariableDeclarationExpression((VariableDeclarationFragment) ASTNode
									.copySubtree(variableDeclarationFragment.getAST(), variableDeclarationFragment));
					variableDeclarationExpression.setType((Type) ASTNode.copySubtree(node.getAST(), node.getType()));
					listVDE.add(variableDeclarationExpression);
				}
			}
			astRewrite.remove(node, null);
			return false;
		}
		return true;
	}

	private List<VariableDeclarationExpression> getListVDE() {
		return listVDE;
	}
}
