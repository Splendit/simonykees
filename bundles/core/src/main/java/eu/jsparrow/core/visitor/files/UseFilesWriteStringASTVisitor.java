package eu.jsparrow.core.visitor.files;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
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

/**
 * 
 * @since 3.24.0
 *
 */
public class UseFilesWriteStringASTVisitor extends AbstractUseFilesMethodsASTVisitor {

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
						java.io.BufferedWriter.class.getName())
				||
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

		if (block.getLocationInParent() != TryStatement.BODY_PROPERTY) {
			return true;
		}

		if (block.statements()
			.size() != 1) {
			return true;
		}

		TryStatement tryStatement = (TryStatement) block.getParent();

		VariableDeclarationFragment fileIOResource = FilesUtil
			.findVariableDeclarationFragmentAsResource(methodExpressionName, tryStatement)
			.orElse(null);

		if (fileIOResource == null) {
			return true;
		}

		ClassInstanceCreation bufferedWriterInstanceCreation = FilesUtil
			.findClassInstanceCreationAsInitializer(fileIOResource, java.io.BufferedWriter.class.getName())
			.orElse(null);

		if (bufferedWriterInstanceCreation == null) {
			return true;
		}

		Expression bufferedWriterArgument = FilesUtil
			.findBufferedIOArgument(bufferedWriterInstanceCreation, java.io.FileWriter.class.getName())
			.orElse(null);

		TransformationData transformationData = null;
		NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer = new NewBufferedIOArgumentsAnalyzer();
		if (bufferedWriterArgument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION
				&& newBufferedIOArgumentsAnalyzer.analyzeInitializer((ClassInstanceCreation) bufferedWriterArgument)) {
			transformationData = newBufferedIOArgumentsAnalyzer
				.createTransformationData(bufferedWriterInstanceCreation);
		}

		return true;
	}
}
