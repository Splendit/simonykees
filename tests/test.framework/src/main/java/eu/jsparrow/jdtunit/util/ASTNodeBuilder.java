package eu.jsparrow.jdtunit.util;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

import eu.jsparrow.jdtunit.JdtUnitException;

/**
 * 
 * @author Hans-Jörg Schnedlitz
 * @since 2.5.0
 */
@SuppressWarnings("nls")
public class ASTNodeBuilder {

	private ASTNodeBuilder() {

	}

	public static Block createBlock(String string) throws JdtUnitException {
		@SuppressWarnings("deprecation") // TODO improvement needed, see SIM-878
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		astParser.setSource(string.toCharArray());
		astParser.setKind(ASTParser.K_STATEMENTS);
		ASTNode result = astParser.createAST(null);
		if ((result.getFlags() & ASTNode.MALFORMED) == ASTNode.MALFORMED) {
			throw new JdtUnitException(String.format("Failed to parse '%s'.", string));
		}
		Block block = (Block) result;
		if (block.statements()
			.isEmpty()) {
			throw new JdtUnitException("Can not create an empty block. There might be syntax errors");
		}
		return block;
	}

}
