package eu.jsparrow.core.visitor.junit;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class ReplaceExpectedExceptionByAssertThrowsASTVisitor extends AbstractAddImportASTVisitor {
	
	
	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		// check for @Test
		List<MarkerAnnotation> annotations = ASTNodeUtil.convertToTypedList(methodDeclaration.modifiers(), MarkerAnnotation.class);
		if(annotations.size() != 1) {
			return false;
		}
		MarkerAnnotation annotation = annotations.get(0);
		Name typeName = annotation.getTypeName();
		ITypeBinding annotationTypeBinding = typeName.resolveTypeBinding();
		if(!ClassRelationUtil.isContentOfType(annotationTypeBinding, "org.junit.Test")) {
			return true;
		}
		
		Block body = methodDeclaration.getBody();
		ExpectedExceptionVisitor visitor = new ExpectedExceptionVisitor();
		body.accept(visitor);
		
		ITypeBinding exceptionType = null; //TODO find me
		ExpressionsThrowingExceptionVisitor expressionsThrowingExceptionVisitor = new ExpressionsThrowingExceptionVisitor(exceptionType);
		body.accept(expressionsThrowingExceptionVisitor);
		List<ASTNode> throwingException = expressionsThrowingExceptionVisitor.getNodesThrowingExpectedException();
		
		// Make a helper visitor to find expectedException.expect(class)
		// make sure the last statement throws that exception 
		// 

		// check for expectedException.expect()
		// make sure the transformation is feasible: no duplicated expect, no statement after expect, etc..
		// generate transformation 
		
		// verify the positioning. 
		
		return false;
	}

}
