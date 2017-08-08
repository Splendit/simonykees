package at.splendit.simonykees.core.visitor;

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Finds all instantiations of {@link String} with no input parameter (new
 * String()) and all instantiations of {@link String} with a {@link String}
 * parameter (new String("foo")) and replaces those occurrences empty String
 * ("") or a String literal ("foo") respectively.
 * 
 * @author Martin Huter
 * @since 0.9.2
 */
public class UseIsEmptyRuleASTVisitor extends AbstractASTRewriteASTVisitor {

	//private static final String STRING_FULLY_QUALLIFIED_NAME = java.lang.String.class.getName();

	public boolean visit(MethodInvocation node) {

		return true;
	}
}
