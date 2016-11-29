package at.splendit.simonykees.core.visitor;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import at.splendit.simonykees.core.Activator;
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
	private static String STRING_FULLY_QUALLIFIED_NAME = "java.util.String"; //$NON-NLS-1$

	private ASTMatcher astMatcher = new ASTMatcher();

	public StringFormatLineSeperatorASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(STRING_KEY, generateFullyQuallifiedNameList(STRING_FULLY_QUALLIFIED_NAME));
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (StringUtils.equals("format", node.getName().getFullyQualifiedName()) //$NON-NLS-1$
				&& node.getExpression() instanceof SimpleName && ClassRelationUtil.isContentOfRegistertITypes(
						node.getExpression().resolveTypeBinding(), iTypeMap.get(STRING_KEY))) {

			@SuppressWarnings("unchecked")
			List<Expression> arguments = (List<Expression>) node.arguments();
			if (arguments.size() == 1 && arguments.get(0) instanceof StringLiteral) {
				Activator.log("insert /n to %n replacement");
			}
		}
		return true;
	}
}
