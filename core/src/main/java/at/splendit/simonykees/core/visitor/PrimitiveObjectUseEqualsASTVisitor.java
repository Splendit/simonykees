package at.splendit.simonykees.core.visitor;

import org.eclipse.jdt.core.dom.MethodInvocation;

import at.splendit.simonykees.core.rule.impl.PrimitiveObjectUseEqualsRule;

/**
 * /** Looks for occurrences of ==, != comparing two primitive objects, such as
 * java.lang.Integer or java.lang.Boolean. The full list of primitives can be
 * found <a href=
 * "https://en.wikibooks.org/wiki/Java_Programming/Primitive_Types">here</a>.
 * 
 * Those occurrences should be replaced by equals(). Using == compares object
 * references, which is often not what you want and can lead to bugs. 
 * 
 * Used in PrimitiveObjectUseEqualsRule.
 * 
 * @see PrimitiveObjectUseEqualsRule
 * @author Hans-Jörg Schrödl
 * @since 2.1.1
 */
public class PrimitiveObjectUseEqualsASTVisitor extends AbstractASTRewriteASTVisitor {
	
	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		return true;

	}

}
