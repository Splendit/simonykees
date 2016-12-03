package at.splendit.simonykees.core.visitor;

import java.util.List;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.StringLiteral;

import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * Finds all class instantiations of string over no input parameter or string
 * because its an useless construction. The wrapping of the string is resolved
 * by removing the constructor and replacing it with the parameter string The
 * wrapping of no parameter is resolved by replacing the constructor with an
 * empty string
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class RemoveNewStringConstructorASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static final Integer STRING_KEY = 1;
	private static final String STRING_FULLY_QUALLIFIED_NAME = "java.lang.String"; //$NON-NLS-1$

	public RemoveNewStringConstructorASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(STRING_KEY, generateFullyQuallifiedNameList(STRING_FULLY_QUALLIFIED_NAME));
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (ClassRelationUtil.isContentOfRegistertITypes(node.getType().resolveBinding(),
				this.iTypeMap.get(STRING_KEY))) {
			/**
			 * node.arguments() javadoc shows that it elements are at least
			 * Expression
			 */
			@SuppressWarnings("unchecked")
			List<Expression> arguments = (List<Expression>) node.arguments();
			/**
			 * new String() resolves to ""
			 */
			if (0 == arguments.size()) {
				astRewrite.replace(node, node.getAST().newStringLiteral(), null);
			} else if (1 == arguments.size()) {
				Expression argument = arguments.get(0);
				if (argument instanceof StringLiteral || ClassRelationUtil
						.isContentOfRegistertITypes(argument.resolveTypeBinding(), iTypeMap.get(STRING_KEY))) {
					astRewrite.replace(node, astRewrite.createMoveTarget(argument), null);
				}
			}
		}
		return true;
	}
}
