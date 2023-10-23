package eu.jsparrow.core.visitor.impl.extradimensions;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

public class ContainingAnnotationVisitor extends ASTVisitor {
	private boolean annotationFound;

	@Override
	public boolean preVisit2(ASTNode node) {
		return !annotationFound;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		annotationFound = true;
		return false;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		annotationFound = true;
		return false;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		annotationFound = true;
		return false;
	}

	boolean isContainingAnnotation() {
		return annotationFound;
	}
}
