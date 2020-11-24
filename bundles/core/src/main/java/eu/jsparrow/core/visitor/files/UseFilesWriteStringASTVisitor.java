package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesASTVisitor;

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
		if (!analyzer.analyze(methodInvocation, getCompilationUnit())) {
			return true;
		}

		UseFilesWriteStringAnalysisResult transformationData;
		if (analyzer.bufferedWriterArgument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer = new NewBufferedIOArgumentsAnalyzer();
			ClassInstanceCreation writerInstanceCreation = (ClassInstanceCreation) analyzer.bufferedWriterArgument;
			if (newBufferedIOArgumentsAnalyzer.analyzeInitializer(writerInstanceCreation)) {
				transformationData = createUseFilesWriteStringAnalysisResult(analyzer, newBufferedIOArgumentsAnalyzer,
						analyzer.bufferedWriterInstanceCreation);
				transform(methodInvocation, transformationData);
			}
		} else if (analyzer.bufferedWriterArgument.getNodeType() == ASTNode.SIMPLE_NAME) {
			SimpleName writerVariableName = (SimpleName) analyzer.bufferedWriterArgument;
			transformationData = createTransformationDataUsingFileIOResource(analyzer, writerVariableName,
					java.io.FileWriter.class.getName())
						.orElse(null);
			if (transformationData != null) {
				transform(methodInvocation, transformationData);
			}
		}
		return true;
	}

	private void transform(MethodInvocation methodInvocation, UseFilesWriteStringAnalysisResult transformationData) {

		MethodInvocation writeStringMethodInvocation = createFilesWriteStringMethodInvocation(transformationData);
		astRewrite.replace(methodInvocation, writeStringMethodInvocation, null);

		Expression bufferedIOInitializer = transformationData.getBufferedIOInstanceCreation();
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
		Name pathsTypeName = addImport(FilesUtil.PATHS_QUALIFIED_NAME,
				transformationData.getBufferedIOInstanceCreation());
		List<Expression> pathsGetArguments = transformationData.getPathExpressions()
			.stream()
			.map(pathExpression -> (Expression) astRewrite.createCopyTarget(pathExpression))
			.collect(Collectors.toList());
		MethodInvocation pathsGet = NodeBuilder.newMethodInvocation(ast, pathsTypeName,
				ast.newSimpleName(FilesUtil.GET), pathsGetArguments);

		Expression writeStringArgumentCopy = (Expression) astRewrite
			.createCopyTarget(transformationData.getWriteStringArgument());

		Expression charset = transformationData.getCharSet()
			.map(exp -> (Expression) astRewrite.createCopyTarget(exp))
			.orElse(null);
		if (charset == null) {
			Name charsetTypeName = addImport(FilesUtil.CHARSET_QUALIFIED_NAME,
					transformationData.getBufferedIOInstanceCreation());
			charset = NodeBuilder.newMethodInvocation(ast, charsetTypeName, FilesUtil.DEFAULT_CHARSET);
		}

		List<Expression> arguments = new ArrayList<>();
		arguments.add(pathsGet);
		arguments.add(writeStringArgumentCopy);
		arguments.add(charset);
		Name filesTypeName = addImport(FilesUtil.FILES_QUALIFIED_NAME,
				transformationData.getBufferedIOInstanceCreation());
		return NodeBuilder.newMethodInvocation(ast, filesTypeName,
				ast.newSimpleName("writeString"), arguments); //$NON-NLS-1$
	}

	static Optional<UseFilesWriteStringAnalysisResult> createTransformationDataUsingFileIOResource(
			UseFilesWriteStringAnalyzer analyzer, SimpleName bufferedIOArg,
			String fileIOQualifiedTypeName) {
		VariableDeclarationFragment fileIOResource = FilesUtil.findVariableDeclarationFragmentAsResource(bufferedIOArg,
				analyzer.tryStatement)
			.orElse(null);
		if (fileIOResource == null) {
			return Optional.empty();
		}

		FileIOAnalyzer fileIOAnalyzer = new FileIOAnalyzer(fileIOQualifiedTypeName);
		if (!fileIOAnalyzer.analyzeFileIO((VariableDeclarationExpression) fileIOResource.getParent())) {
			return Optional.empty();
		}

		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(fileIOResource.getName());
		analyzer.tryStatement.accept(visitor);
		List<SimpleName> usages = visitor.getUsages();
		usages.remove(fileIOResource.getName());
		usages.remove(bufferedIOArg);
		if (!usages.isEmpty()) {
			return Optional.empty();
		}

		List<Expression> pathExpressions = fileIOAnalyzer.getPathExpressions();
		UseFilesWriteStringAnalysisResult transformationData = fileIOAnalyzer.getCharset()
			.map(charSet -> new UseFilesWriteStringAnalysisResult(analyzer.bufferedWriterInstanceCreation,
					pathExpressions, analyzer.writeStringArgument, charSet,
					fileIOResource))
			.orElse(new UseFilesWriteStringAnalysisResult(analyzer.bufferedWriterInstanceCreation, pathExpressions,
					analyzer.writeStringArgument,
					fileIOResource));
		return Optional.of(transformationData);
	}

	UseFilesWriteStringAnalysisResult createUseFilesWriteStringAnalysisResult(UseFilesWriteStringAnalyzer analyzer,
			NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer, ClassInstanceCreation newBufferedIO) {
		Expression charsetExpression = newBufferedIOArgumentsAnalyzer.getCharsetExpression()
			.orElse(null);
		if (charsetExpression != null) {
			return new UseFilesWriteStringAnalysisResult(newBufferedIO,
					newBufferedIOArgumentsAnalyzer.getPathExpressions(), analyzer.writeStringArgument,
					charsetExpression);
		}
		return new UseFilesWriteStringAnalysisResult(newBufferedIO,
				newBufferedIOArgumentsAnalyzer.getPathExpressions(), analyzer.writeStringArgument);
	}
}
