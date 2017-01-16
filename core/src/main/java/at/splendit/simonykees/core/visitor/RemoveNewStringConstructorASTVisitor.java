package at.splendit.simonykees.core.visitor;

import java.util.List;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.StringLiteral;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * TODO: Martin please check if this description is correct.
 * 
 * Finds all instantiations of {@link String} with no input parameter (new
 * String()) and all instantiations of {@link String} with a {@link String}
 * parameter (new String("foo")) and replaces those occurrences empty String
 * ("") or a String literal ("foo") respectively.
 * 
 * @author Martin Huter
 * @since 0.9.2
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
			 * node.arguments() javadoc shows that its elements are at least
			 * Expression
			 */
			@SuppressWarnings("unchecked")
			List<Expression> arguments = (List<Expression>) node.arguments();

			
			switch (arguments.size()) {
			
			case 0:
				/**
				 * new String() resolves to ""
				 */
				astRewrite.replace(node, node.getAST().newStringLiteral(), null);
				break;
				
			case 1:
				/**
				 * new String("string" || StringLiteral) resolves to "string" ||
				 * StringLiteral
				 */
				Expression argument = arguments.get(0);
				if (argument instanceof StringLiteral || ClassRelationUtil
						.isContentOfRegistertITypes(argument.resolveTypeBinding(), iTypeMap.get(STRING_KEY))) {
					if (argument instanceof ParenthesizedExpression) {
						argument = ASTNodeUtil.unwrapParenthesizedExpression(argument);
					}
					astRewrite.replace(node, astRewrite.createMoveTarget(argument), null);
				}
				break;

			default:
				break;
			}
		}
		return true;
	}
}
