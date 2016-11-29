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
 * String.format() where /n is used is replaced by %n to use the
 * {@link lookuplinesep}
 * 
 * @author Martin Huter
 * @since 0.9.2
 *
 */
public class StringFormatLineSeperatorASTVisitor extends AbstractCompilationUnitASTVisitor {

	private static Integer STRING_KEY = 1;
	private static String STRING_FULLY_QUALLIFIED_NAME = "java.lang.String"; //$NON-NLS-1$

	private static Integer LOCALE_KEY = 2;
	private static String LOCALE_FULLY_QUALLIFIED_NAME = "java.util.Locale"; //$NON-NLS-1$

	public StringFormatLineSeperatorASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(STRING_KEY, generateFullyQuallifiedNameList(STRING_FULLY_QUALLIFIED_NAME));
		this.fullyQuallifiedNameMap.put(LOCALE_KEY, generateFullyQuallifiedNameList(LOCALE_FULLY_QUALLIFIED_NAME));
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (StringUtils.equals("format", node.getName().getFullyQualifiedName()) //$NON-NLS-1$
				&& node.getExpression() instanceof SimpleName && ClassRelationUtil.isContentOfRegistertITypes(
						node.getExpression().resolveTypeBinding(), iTypeMap.get(STRING_KEY))) {

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
					&& ClassRelationUtil.isContentOfRegistertITypes(
							((QualifiedName) arguments.get(0)).resolveTypeBinding(), iTypeMap.get(LOCALE_KEY))
					&& arguments.get(1) instanceof StringLiteral) {
				formatString = (StringLiteral) arguments.get(1);
			}
			// StringLiteral for refactoring found
			if (formatString != null) {
				//replace complete windows strings
				String formatedString = StringUtils.replace(formatString.getEscapedValue(), "\\r\\n", "%n"); //$NON-NLS-1$//$NON-NLS-2$
				//replace complete unix strings
				//FIXME are there possible side effects?
				formatedString = StringUtils.replace(formatedString, "\\n", "%n"); //$NON-NLS-1$//$NON-NLS-2$
				StringLiteral newFormatString = NodeBuilder.newStringLiteral(node.getAST(), formatedString);
				astRewrite.replace(formatString, newFormatString, null);
			}
		}
		return true;
	}
}
