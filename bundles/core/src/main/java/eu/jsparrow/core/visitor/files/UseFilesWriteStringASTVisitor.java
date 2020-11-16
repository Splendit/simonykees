package eu.jsparrow.core.visitor.files;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class UseFilesWriteStringASTVisitor extends AbstractAddImportASTVisitor {
	
	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		return super.visit(compilationUnit);
	}


	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		return super.visit(methodInvocation);
	}

}
