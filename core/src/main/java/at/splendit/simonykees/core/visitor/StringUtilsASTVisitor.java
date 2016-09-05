package at.splendit.simonykees.core.visitor;

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class StringUtilsASTVisitor extends AbstractCompilationUnitAstVisitor {

	public StringUtilsASTVisitor(ASTRewrite astRewrite, List<IType> itypes) {
		super(astRewrite, itypes);
	}

	public StringUtilsASTVisitor(ASTRewrite astRewrite) {
		super(astRewrite);
	}
	
	@Override
	public boolean visit(MethodInvocation node) {
		node.getExpression().resolveTypeBinding();
		isContentofRegistertITypes(node.getExpression().resolveTypeBinding());
		
		return true;
	}
	
	@Override
	public boolean visit(ExpressionStatement node) {
		return true;
	}

	@Override
	protected
	String[] relevantClasses() {
		return new String[] { "java.lang.String" };
	}

}
