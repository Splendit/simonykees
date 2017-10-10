package eu.jsparrow.core.visitor.helper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.Test;

import eu.jsparrow.core.util.ASTNodeUtil;
import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

public class WildCardTypeASTVisitorTest {
	
	@Test
	public void treeContainsWildCardTypeASTVisitor() throws Exception {
		WildCardTypeASTVisitor wildCardTypeASTVisitor = new WildCardTypeASTVisitor();
		
		ASTNode astNode = ASTNodeBuilder.createBlock("List<? extends String> aList;");  //$NON-NLS-1$
		
		astNode.accept(wildCardTypeASTVisitor);
		
		assertFalse(wildCardTypeASTVisitor.getWildCardTypes().isEmpty());
	}
	
	@Test
	public void treeDontContainsWildCardTypeASTVisitorTest() throws Exception {
		WildCardTypeASTVisitor wildCardTypeASTVisitor = new WildCardTypeASTVisitor();
		
		ASTNode astNode = ASTNodeBuilder.createBlock("List<String> aList;");  //$NON-NLS-1$
		
		astNode.accept(wildCardTypeASTVisitor);
		
		assertTrue(wildCardTypeASTVisitor.getWildCardTypes().isEmpty());
	}
	
	@Test
	public void WildCardTypeASTNodeUtilTest01() throws Exception {
		ASTNode astNode = ASTNodeBuilder.createBlock("List<? extends String> aList;");  //$NON-NLS-1$
		
		assertTrue(ASTNodeUtil.containsWildCards(astNode));
	}
	
	@Test
	public void WildCardTypeASTNodeUtilTest02() throws Exception {
		ASTNode astNode = ASTNodeBuilder.createBlock("List<String> aList;");  //$NON-NLS-1$
		
		assertFalse(ASTNodeUtil.containsWildCards(astNode));
	}
	
	
}
