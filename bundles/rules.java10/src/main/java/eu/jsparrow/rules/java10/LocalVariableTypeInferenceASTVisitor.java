package eu.jsparrow.rules.java10;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class LocalVariableTypeInferenceASTVisitor extends AbstractASTRewriteASTVisitor {

	CompilationUnit astRoot = null;

	public boolean visit(CompilationUnit node) {
		this.astRoot = node;
		return true;
	}

	public void endVisit(CompilationUnit node) {
		this.astRoot = null;
	}
	
	public boolean visit(VariableDeclarationStatement node) {
		return true;
	}

	public boolean visit(VariableDeclarationFragment node) {
		IBinding binding = node.getName().resolveBinding();
		if (!(binding instanceof IVariableBinding)) {
			return false;
		}
		IVariableBinding varBinding = (IVariableBinding) binding;
		if (varBinding.isField() || varBinding.isParameter()) {
			return false;
		}

		ITypeBinding typeBinding = varBinding.getType();
		if (typeBinding == null || typeBinding.isAnonymous() || typeBinding.isIntersectionType()
				|| typeBinding.isWildcardType()) {
			return false;
		}
		
		
		Type type = null;
		ASTNode parent = node.getParent();
		if (parent instanceof VariableDeclarationStatement) {
			type = ((VariableDeclarationStatement) parent).getType();
		} else if (parent instanceof VariableDeclarationExpression) {
			type = ((VariableDeclarationExpression) parent).getType();
		}
		
		if (type == null || type.isVar()) {
			return false;
		}
		
		SimpleName var = node.getAST().newSimpleName("var");
		Type varType = node.getAST().newSimpleType(var);
		astRewrite.replace(type, varType, null);
		
		return true;
	}
}
