package at.splendit.simonykees.core.visitor.tryWithResource;

import java.util.ArrayList;
import java.util.List;


import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
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
	
	@Override
	public boolean visit(TryStatement node) {
		return true;
	}
	
	@Override
	public boolean visit(VariableDeclarationStatement node) {
		ITypeBinding typeBind = node.getType().resolveBinding();
		if(isSubInterface(typeBind)){
			System.out.println(typeBind.getName()+" inherits from :"+itypes.toString());
		}
		return true;
	}	
	
	private boolean isSubInterface(ITypeBinding iTypeBinding){
		boolean result = false;
		if (iTypeBinding == null){
			return false;
		}
		for(ITypeBinding interfaceBind : iTypeBinding.getInterfaces()){
			if(itypes.contains(interfaceBind.getJavaElement())){
				result = result || true;
			}
			result = result || isSubInterface(interfaceBind.getSuperclass());
		}
		return result || isSubInterface(iTypeBinding.getSuperclass());
	}
}
