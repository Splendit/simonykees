package at.splendit.simonykees.core.visitor;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;

import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * Every usage of the function {@link Object#toString()} on an java object is
 * removed, if it is used on an element with the type String
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class RemoveToStringOnStringASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static Integer STRING_KEY = 1;
	private static String STRING_FULLY_QUALLIFIED_NAME = "java.lang.String"; //$NON-NLS-1$

	public RemoveToStringOnStringASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(STRING_KEY, generateFullyQuallifiedNameList(STRING_FULLY_QUALLIFIED_NAME));
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (StringUtils.equals("toString", node.getName().getFullyQualifiedName()) //$NON-NLS-1$
				&& node.typeArguments().size() == 0
				&& (node.getExpression() != null && ClassRelationUtil
						.isContentOfRegistertITypes(node.getExpression().resolveTypeBinding(), iTypeMap.get(STRING_KEY))
						|| node.getExpression() instanceof StringLiteral)) {
			astRewrite.replace(node, (Expression) astRewrite.createMoveTarget(node.getExpression()), null);
		}
		return true;
	}
}
