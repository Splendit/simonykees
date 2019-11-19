package eu.jsparrow.core.util;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.Test;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

public class ASTNodeUtilTest {

	@Test
	public void containsWildCards_wildCardTypeTree_nodeExists() throws Exception {
		ASTNode astNode = ASTNodeBuilder.createBlockFromString("List<? extends String> aList;"); //$NON-NLS-1$

		assertTrue(ASTNodeUtil.containsWildCards(astNode));
	}

	@Test
	public void containsWildCards_noWildCardTypeTree_noNodeExists() throws Exception {
		ASTNode astNode = ASTNodeBuilder.createBlockFromString("List<String> aList;"); //$NON-NLS-1$

		assertFalse(ASTNodeUtil.containsWildCards(astNode));
	}
	
}
