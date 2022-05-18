package eu.jsparrow.core.visitor.spring;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class ReplaceRequestMappingAnnotationASTVisitor extends AbstractAddImportASTVisitor {
	
	

	@Override
	public boolean visit(ImportDeclaration node) {
		IBinding binding = node.resolveBinding();
		return true;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		IAnnotationBinding annotationBinding = node.resolveAnnotationBinding();
		return true;
	}

}
