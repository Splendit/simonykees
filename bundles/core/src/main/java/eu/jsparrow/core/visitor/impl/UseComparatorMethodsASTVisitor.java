package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**

 * 
 * @since 3.22.0
 *
 */
public class UseComparatorMethodsASTVisitor extends AbstractAddImportASTVisitor {

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		return true;
	}

}
