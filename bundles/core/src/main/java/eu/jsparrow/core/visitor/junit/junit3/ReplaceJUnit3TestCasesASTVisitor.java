package eu.jsparrow.core.visitor.junit.junit3;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class ReplaceJUnit3TestCasesASTVisitor extends AbstractAddImportASTVisitor {
	private Junit3MigrationConfiguration configuration;

	public ReplaceJUnit3TestCasesASTVisitor(Junit3MigrationConfiguration configuration) {
		this.configuration = configuration;
	}
	
	

	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding methodBinding = node.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}
		;
		String qualifiedName = methodBinding.getDeclaringClass()
			.getQualifiedName();
		return true;
	}

}