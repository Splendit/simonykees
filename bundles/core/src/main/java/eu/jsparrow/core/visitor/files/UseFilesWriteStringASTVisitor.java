package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * 
 * @since 3.24.0
 *
 */
public class UseFilesWriteStringASTVisitor extends AbstractAddImportASTVisitor {

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (!continueVisiting) {
			return false;
		}
		verifyImport(compilationUnit, FilesUtil.PATHS_QUALIFIED_NAME);
		verifyImport(compilationUnit, FilesUtil.CHARSET_QUALIFIED_NAME);
		verifyImport(compilationUnit, FilesUtil.FILES_QUALIFIED_NAME);
		return continueVisiting;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		UseFilesWriteStringAnalyzer analyzer = new UseFilesWriteStringAnalyzer();
		analyzer.findAnalysisResult(methodInvocation, getCompilationUnit())
			.ifPresent(result -> {
				transform(methodInvocation, result);
			});
		return true;
	}

	private void transform(MethodInvocation methodInvocation, UseFilesWriteStringAnalysisResult transformationData) {

		MethodInvocation writeStringMethodInvocation = createFilesWriteStringMethodInvocation(transformationData);
		astRewrite.replace(methodInvocation, writeStringMethodInvocation, null);

		Expression bufferedIOInitializer = transformationData.getBufferedIOInitializer();
		ASTNode bufferedIODeclarationFragment = bufferedIOInitializer.getParent();
		ASTNode bufferedIOResourceToRemove = bufferedIODeclarationFragment.getParent();
		astRewrite.remove(bufferedIOResourceToRemove, null);
		transformationData.getFileIOResource()
			.ifPresent(resource -> astRewrite.remove(resource.getParent(), null));
		onRewrite();
	}

	private MethodInvocation createFilesWriteStringMethodInvocation(
			UseFilesWriteStringAnalysisResult transformationData) {
		AST ast = astRewrite.getAST();
		Expression pathArgument = transformationData.getPathExpression()
			.map(p -> (Expression) astRewrite.createCopyTarget(p))
			.orElse(null);
		if (pathArgument == null) {
			Name pathsTypeName = addImport(FilesUtil.PATHS_QUALIFIED_NAME,
					transformationData.getBufferedIOInitializer());
			List<Expression> pathsGetArguments = transformationData.getPathExpressions()
				.stream()
				.map(pathExpression -> (Expression) astRewrite.createCopyTarget(pathExpression))
				.collect(Collectors.toList());
			pathArgument = NodeBuilder.newMethodInvocation(ast, pathsTypeName,
					ast.newSimpleName(FilesUtil.GET), pathsGetArguments);
		}

		Expression writeStringArgumentCopy = (Expression) astRewrite
			.createCopyTarget(transformationData.getWriteStringArgument());

		Expression charset = transformationData.getCharSet()
			.map(exp -> (Expression) astRewrite.createCopyTarget(exp))
			.orElse(null);
		if (charset == null) {
			Name charsetTypeName = addImport(FilesUtil.CHARSET_QUALIFIED_NAME,
					transformationData.getBufferedIOInitializer());
			charset = NodeBuilder.newMethodInvocation(ast, charsetTypeName, FilesUtil.DEFAULT_CHARSET);
		}

		List<Expression> arguments = new ArrayList<>();
		arguments.add(pathArgument);
		arguments.add(writeStringArgumentCopy);
		arguments.add(charset);
		Name filesTypeName = addImport(FilesUtil.FILES_QUALIFIED_NAME,
				transformationData.getBufferedIOInitializer());
		return NodeBuilder.newMethodInvocation(ast, filesTypeName,
				ast.newSimpleName("writeString"), arguments); //$NON-NLS-1$
	}
}
