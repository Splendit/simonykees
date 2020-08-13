package eu.jsparrow.core.visitor.impl;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.visitor.sub.SignatureData;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class UseOffsetBasedStringMethodsASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String SUBSTRING = "substring"; //$NON-NLS-1$
	private static final String STARTS_WITH = "startsWith"; //$NON-NLS-1$
	private static final String LAST_INDEX_OF = "lastIndexOf"; //$NON-NLS-1$
	private static final String INDEX_OF = "indexOf"; //$NON-NLS-1$
	private static final SignatureData INDEX_OF_INT = new SignatureData(java.lang.String.class, INDEX_OF, int.class);
	private static final SignatureData INDEX_OF_STRING = new SignatureData(java.lang.String.class, INDEX_OF,
			java.lang.String.class);
	private static final SignatureData LAST_INDEX_OF_INT = new SignatureData(java.lang.String.class, LAST_INDEX_OF,
			int.class);
	private static final SignatureData LAST_INDEX_OF_STRING = new SignatureData(java.lang.String.class, LAST_INDEX_OF,
			java.lang.String.class);
	private static final SignatureData STARTS_WITH_STRING = new SignatureData(java.lang.String.class, STARTS_WITH,
			java.lang.String.class);
	private static final SignatureData SUBSTRING_AT_BEGIN_INDEX = new SignatureData(java.lang.String.class, SUBSTRING,
			int.class);

	private boolean checkSignature(IMethodBinding methodBinding) {
		return INDEX_OF_INT.isEquivalentTo(methodBinding) ||
				INDEX_OF_STRING.isEquivalentTo(methodBinding) ||
				LAST_INDEX_OF_INT.isEquivalentTo(methodBinding) ||
				LAST_INDEX_OF_STRING.isEquivalentTo(methodBinding) ||
				STARTS_WITH_STRING.isEquivalentTo(methodBinding);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (!checkSignature(node.resolveMethodBinding())) {
			return true;
		}

		Expression expression = node.getExpression();
		if (expression.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return true;
		}
		MethodInvocation substringInvocation = (MethodInvocation) expression;
		if (!SUBSTRING_AT_BEGIN_INDEX.isEquivalentTo(substringInvocation.resolveMethodBinding())) {
			return true;
		}

		Expression stringExpression = substringInvocation.getExpression();
		Expression offsetArgument = ASTNodeUtil.convertToTypedList(substringInvocation.arguments(), Expression.class)
			.get(0);

		// begin transforming...
		ASTNode offsetArgumentCopy = astRewrite.createCopyTarget(offsetArgument);
		astRewrite.getListRewrite(node, MethodInvocation.ARGUMENTS_PROPERTY)
			.insertLast(offsetArgumentCopy, null);

		ASTNode stringExpressionCopy = astRewrite.createCopyTarget(stringExpression);
		astRewrite.replace(node.getExpression(), stringExpressionCopy, null);
		onRewrite();
		return true;
	}
}
