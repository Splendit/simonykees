package eu.jsparrow.core.visitor.helper;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;
import eu.jsparrow.rules.common.visitor.helper.WildCardTypeASTVisitor;

public class WildCardTypeASTVisitorTest {

	@Test
	public void visitor_wildCardTypeTree_nodeFound() throws Exception {
		WildCardTypeASTVisitor visitor = new WildCardTypeASTVisitor();

		ASTNode astNode = ASTNodeBuilder.createBlockFromString("List<? extends String> aList;"); //$NON-NLS-1$

		astNode.accept(visitor);

		assertFalse(visitor.getWildCardTypes()
			.isEmpty());
	}

	@Test
	public void visitor_noWildCardTypeTree_noNodeFound() throws Exception {
		WildCardTypeASTVisitor visitor = new WildCardTypeASTVisitor();

		ASTNode astNode = ASTNodeBuilder.createBlockFromString("List<String> aList;"); //$NON-NLS-1$

		astNode.accept(visitor);

		assertTrue(visitor.getWildCardTypes()
			.isEmpty());
	}
}
