package eu.jsparrow.core.visitor.junit;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class ReplaceExpectedExceptionByAssertThrowsASTVisitor extends AbstractAddImportASTVisitor {
	
	
	public boolean visit(MethodDeclaration methodDeclaration) {
		// check for @Test
		// check for expectedException.expect()
		// make sure the transformation is feasible: nod duplicated expect, no statement after expect, etc..
		// generate transformation 
		
		return false;
	}

}
