package eu.jsparrow.core.visitor.files;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.visitor.sub.SignatureData;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class UseFilesWriteStringASTVisitor extends AbstractAddImportASTVisitor {

	private final SignatureData write = new SignatureData(java.io.Writer.class, "write", java.lang.String.class); //$NON-NLS-1$

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (!write.isEquivalentTo(methodInvocation.resolveMethodBinding())) {
			return true;
		}
		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression == null ||
				!ClassRelationUtil.isContentOfType(methodInvocationExpression.resolveTypeBinding(),
				java.io.BufferedWriter.class.getName()) ||
				methodInvocationExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return true;
		}
		SimpleName methodExpressionName = (SimpleName) methodInvocationExpression;

		Expression writeStringArgument = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.get(0);

		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return true;
		}
		ExpressionStatement expressionStatement = (ExpressionStatement) methodInvocation.getParent();
		if (expressionStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return true;
		}
		Block block = (Block) expressionStatement.getParent();

		TryStatement tryStatement = null;
		if (block.getLocationInParent() == TryStatement.BODY_PROPERTY) {
			tryStatement = (TryStatement) block.getParent();
			VariableDeclarationFragment fileIOResource = FilesUtils
				.findVariableDeclarationFragmentAsResource(methodExpressionName,
						tryStatement)
				.orElse(null);
			if (fileIOResource == null) {
				return true;
			}
		}
		return true;
	}

}
