package eu.jsparrow.core.visitor.files;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import eu.jsparrow.core.visitor.sub.SignatureData;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * Helper class to determine whether a method invocation is an invocation of
 * <br>
 * {@link java.io.Writer#write(String)} <br>
 * which can be transformed by the <br>
 * {@link eu.jsparrow.core.visitor.files.UseFilesWriteStringASTVisitor}.
 *
 * @since 3.24.0
 */
class WriteMethodInvocationAnalyzer {

	private final SignatureData write = new SignatureData(java.io.Writer.class, "write", java.lang.String.class); //$NON-NLS-1$
	private SimpleName writerVariableSimpleName;
	private ExpressionStatement writeInvocationStatementToReplace;
	private Expression charSequenceArgument;
	private Block blockOfInvocationStatement;

	boolean analyze(MethodInvocation methodInvocation) {
		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression == null || methodInvocationExpression
			.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}
		writerVariableSimpleName = (SimpleName) methodInvocationExpression;

		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return false;
		}
		writeInvocationStatementToReplace = (ExpressionStatement) methodInvocation.getParent();

		if (!write.isEquivalentTo(methodInvocation.resolveMethodBinding())) {
			return false;
		}
		charSequenceArgument = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.get(0);

		if (writeInvocationStatementToReplace.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return false;
		}
		blockOfInvocationStatement = (Block) writeInvocationStatementToReplace.getParent();
		return true;
	}

	SimpleName getWriterVariableSimpleName() {
		return writerVariableSimpleName;
	}

	ExpressionStatement getWriteInvocationStatementToReplace() {
		return writeInvocationStatementToReplace;
	}

	Expression getCharSequenceArgument() {
		return charSequenceArgument;
	}

	Block getBlockOfInvocationStatement() {
		return blockOfInvocationStatement;
	}
}
