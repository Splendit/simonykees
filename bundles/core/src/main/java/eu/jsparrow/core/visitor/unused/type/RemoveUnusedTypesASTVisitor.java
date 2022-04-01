package eu.jsparrow.core.visitor.unused.type;

import java.util.List;

import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveUnusedTypesASTVisitor extends AbstractASTRewriteASTVisitor {

	private List<UnusedTypeWrapper> unusedTypes;

	public RemoveUnusedTypesASTVisitor(List<UnusedTypeWrapper> unusedTypes) {
		this.unusedTypes = unusedTypes;
	}
	
	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		return true;
	}

}
