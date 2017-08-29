package at.splendit.simonykees.core.visitor;

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Looks for occurrences of equals(..) that refer to an Enumeration. 
 * <p>
 * Those occurrences are then replaced with ==
 * <ul>
 * <li>Enum: since 1.5, ex.: myEnumInstance.equals(MyEnum.ITEM) -> myEnumInstance == MyEnum.ITEM</li>
 * </ul>
 * 
 * @author Hans-Jörg Schrödl
 * @since 2.1.1
 */
public class EnumsWithoutEqualsASTVisitor extends AbstractASTRewriteASTVisitor {

	public boolean visit(MethodInvocation methodInvocation) {
		return false;
	}
	
}
