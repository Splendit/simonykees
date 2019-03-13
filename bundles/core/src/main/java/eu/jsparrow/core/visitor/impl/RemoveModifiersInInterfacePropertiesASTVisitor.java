package eu.jsparrow.core.visitor.impl;

import java.util.List;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveModifiersInInterfacePropertiesASTVisitor extends AbstractASTRewriteASTVisitor {
	
	@Override
	public boolean visit(TypeDeclaration type) {
		
		if(!type.isInterface()) {
			return true;
		}
		
		FieldDeclaration[] fields = type.getFields();
		
		for(FieldDeclaration field : fields) {
			List<Modifier> modifiers = ASTNodeUtil.convertToTypedList(field.modifiers(), Modifier.class);
			for(Modifier modifier : modifiers) {
				if(modifier.isPublic() || modifier.isStatic() || modifier.isFinal()) {
					astRewrite.remove(modifier, null);
					onRewrite();
				}
			}
		}
		
		
		MethodDeclaration[] methods = type.getMethods();
		for(MethodDeclaration method : methods) {
			List<Modifier> modifiers = ASTNodeUtil.convertToTypedList(method.modifiers(), Modifier.class);
			for(Modifier modifier : modifiers) {
				if(modifier.isPublic()) {
					astRewrite.remove(modifier, null);
					onRewrite();
				}
			}
		}
		
		
		return true;
	}

}
