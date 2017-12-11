package eu.jsparrow.core.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Modifier;
import org.junit.Test;

import eu.jsparrow.jdtunit.util.ASTNodeBuilder;

public class ASTNodeUtilTest {

	@Test
	public void containsWildCards_wildCardTypeTree_nodeExists() throws Exception {
		ASTNode astNode = ASTNodeBuilder.createBlock("List<? extends String> aList;"); //$NON-NLS-1$

		assertTrue(ASTNodeUtil.containsWildCards(astNode));
	}

	@Test
	public void containsWildCards_noWildCardTypeTree_noNodeExists() throws Exception {
		ASTNode astNode = ASTNodeBuilder.createBlock("List<String> aList;"); //$NON-NLS-1$

		assertFalse(ASTNodeUtil.containsWildCards(astNode));
	}
	
}
