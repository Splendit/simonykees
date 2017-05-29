package at.splendit.simonykees.core.visitor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import at.splendit.simonykees.core.builder.NodeBuilder;
import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * Every {@link String#format(String, Object...)} or
 * {@link String#format(java.util.Locale, String, Object...)} where the
 * parameter {@link String} is a StringLiteral is transformed so that all
 * occurrences of "\r\n" and "\n" are replaces by "%n" which is matched to
 * {@link System#lineSeparator()} by {@link String#format}
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class StringFormatLineSeparatorASTVisitor extends AbstractASTRewriteASTVisitor {

	private static String STRING_FULLY_QUALLIFIED_NAME = java.lang.String.class.getName();
	private static String LOCALE_FULLY_QUALLIFIED_NAME = java.util.Locale.class.getName();

	/**
	 * checks every String.format invocation for a static format string and
	 * replaces all basic line breaks with the String.format() default %n
	 */
	@Override
	public boolean visit(MethodInvocation node) {
		if (StringUtils.equals("format", node.getName().getFullyQualifiedName()) //$NON-NLS-1$
				&& node.getExpression() instanceof SimpleName && ClassRelationUtil.isContentOfTypes(
						node.getExpression().resolveTypeBinding(), generateFullyQuallifiedNameList(STRING_FULLY_QUALLIFIED_NAME))) {

			@SuppressWarnings("rawtypes")
			List arguments = node.arguments();
			StringLiteral formatString = null;
			if (arguments.size() >= 1 && arguments.get(0) instanceof StringLiteral) {
				formatString = (StringLiteral) arguments.get(0);
			}
			/*
			 * Checks if at least 2 parameters are present; the first needs to
			 * be an LOCALE & the second an StringLiteral
			 */
			else if (arguments.size() >= 2 && arguments.get(0) instanceof QualifiedName
					&& ClassRelationUtil.isContentOfTypes(
							((QualifiedName) arguments.get(0)).resolveTypeBinding(), generateFullyQuallifiedNameList(LOCALE_FULLY_QUALLIFIED_NAME))
					&& arguments.get(1) instanceof StringLiteral) {
				formatString = (StringLiteral) arguments.get(1);
			}
			// StringLiteral for refactoring found
			if (formatString != null) {
				// replace complete windows strings
				String formatedString = StringUtils.replace(formatString.getEscapedValue(), "\\r\\n", "%n"); //$NON-NLS-1$//$NON-NLS-2$
				// replace complete unix strings
				// FIXME are there possible side effects?
				formatedString = StringUtils.replace(formatedString, "\\n", "%n"); //$NON-NLS-1$//$NON-NLS-2$
				/**
				 * only make an astRewrite, if a change happened
				 */
				if(!formatedString.equals(formatString.getEscapedValue())){
					StringLiteral newFormatString = NodeBuilder.newStringLiteral(node.getAST(), formatedString);
					astRewrite.replace(formatString, newFormatString, null);	
				}
			}
		}
		return true;
	}
}
