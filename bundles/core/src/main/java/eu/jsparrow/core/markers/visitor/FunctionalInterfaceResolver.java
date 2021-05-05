package eu.jsparrow.core.markers.visitor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;

import eu.jsparrow.core.visitor.functionalinterface.FunctionalInterfaceASTVisitor;

public class FunctionalInterfaceResolver extends FunctionalInterfaceASTVisitor {
	
	private int offset;
	
	public FunctionalInterfaceResolver(int offset) {
		this.offset = offset;
	}
	
	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		ASTNode parent = node.getParent();
		int startPosition = parent.getStartPosition();
		int endPosition = startPosition + parent.getLength();
		if(startPosition <= offset && endPosition >= offset) {
			return super.visit(node);
		}
		return false;
	}
}
