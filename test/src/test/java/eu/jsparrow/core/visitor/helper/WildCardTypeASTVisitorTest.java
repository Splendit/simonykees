package eu.jsparrow.core.visitor.helper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.Test;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

public class WildCardTypeASTVisitorTest {
	
	@Test
	public void visitor_wildCardTypeTree_nodeFound() throws Exception {
		WildCardTypeASTVisitor visitor = new WildCardTypeASTVisitor();
		
		ASTNode astNode = ASTNodeBuilder.createBlock("List<? extends String> aList;");  //$NON-NLS-1$
		
		astNode.accept(visitor);
		
		assertFalse(visitor.getWildCardTypes().isEmpty());
	}
	
	@Test
	public void visitor_noWildCardTypeTree_noNodeFound() throws Exception {
		WildCardTypeASTVisitor visitor = new WildCardTypeASTVisitor();
		
		ASTNode astNode = ASTNodeBuilder.createBlock("List<String> aList;");  //$NON-NLS-1$
		
		astNode.accept(visitor);
		
		assertTrue(visitor.getWildCardTypes().isEmpty());
	}
}
