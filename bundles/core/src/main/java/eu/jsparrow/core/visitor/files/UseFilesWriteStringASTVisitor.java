package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

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
		if (!analyzer.analyze(methodInvocation, getCompilationUnit())) {
			return true;
		}

		TransformationData transformationData;
		if (analyzer.bufferedWriterArgument.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			NewBufferedIOArgumentsAnalyzer newBufferedIOArgumentsAnalyzer = new NewBufferedIOArgumentsAnalyzer();
			ClassInstanceCreation writerInstanceCreation = (ClassInstanceCreation) analyzer.bufferedWriterArgument;
			if (newBufferedIOArgumentsAnalyzer.analyzeInitializer(writerInstanceCreation)) {
				transformationData = newBufferedIOArgumentsAnalyzer
					.createTransformationData(analyzer.bufferedWriterInstanceCreation);
				transform(methodInvocation, analyzer.writeStringArgument, analyzer.fragmentDeclaringBufferedWriter,
						transformationData);
			}
		} else if (analyzer.bufferedWriterArgument.getNodeType() == ASTNode.SIMPLE_NAME) {

		}
		return true;
	}

	private void transform(MethodInvocation methodInvocation, Expression writeStringArgument,
			VariableDeclarationFragment fragmentDeclaringBufferedWriter, TransformationData transformationData) {

		MethodInvocation writeStringMethodInvocation = createFilesWriteStringMethodInvocation(transformationData,
				writeStringArgument);
		astRewrite.replace(methodInvocation, writeStringMethodInvocation, null);
		removeFragmentDeclaringBufferedWriter(fragmentDeclaringBufferedWriter);
		onRewrite();
	}

	private void removeFragmentDeclaringBufferedWriter(VariableDeclarationFragment fragmentDeclaringBufferedWriter) {
		if (fragmentDeclaringBufferedWriter.getLocationInParent() == VariableDeclarationExpression.FRAGMENTS_PROPERTY &&
				fragmentDeclaringBufferedWriter.getParent()
					.getLocationInParent() == TryStatement.RESOURCES2_PROPERTY) {
			VariableDeclarationExpression parentVariableDeclarationExpression = (VariableDeclarationExpression) fragmentDeclaringBufferedWriter
				.getParent();
			TryStatement tryStatement = (TryStatement) parentVariableDeclarationExpression.getParent();
			if (parentVariableDeclarationExpression.fragments()
				.size() == 1) {
				ListRewrite resourceRewriter = astRewrite.getListRewrite(tryStatement,
						TryStatement.RESOURCES2_PROPERTY);
				resourceRewriter.remove(parentVariableDeclarationExpression, null);
			} else {
				ListRewrite fragmentsRewriter = astRewrite.getListRewrite(parentVariableDeclarationExpression,
						VariableDeclarationExpression.FRAGMENTS_PROPERTY);
				fragmentsRewriter.remove(fragmentDeclaringBufferedWriter, null);
			}
		} else if (fragmentDeclaringBufferedWriter
			.getLocationInParent() == VariableDeclarationStatement.FRAGMENTS_PROPERTY
				&& fragmentDeclaringBufferedWriter.getParent()
					.getLocationInParent() == Block.STATEMENTS_PROPERTY) {
			VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) fragmentDeclaringBufferedWriter
				.getParent();
			Block block = (Block) variableDeclarationStatement.getParent();
			if (variableDeclarationStatement.fragments()
				.size() == 1) {
				ListRewrite statementsRewriter = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
				statementsRewriter.remove(variableDeclarationStatement, null);
			} else {
				ListRewrite fragmentsRewriter = astRewrite.getListRewrite(variableDeclarationStatement,
						VariableDeclarationExpression.FRAGMENTS_PROPERTY);
				fragmentsRewriter.remove(fragmentDeclaringBufferedWriter, null);
			}
		}
	}

	private MethodInvocation createFilesWriteStringMethodInvocation(TransformationData transformationData,
			Expression writeStringArgument) {
		AST ast = astRewrite.getAST();
		Name pathsTypeName = addImport(FilesUtil.PATHS_QUALIFIED_NAME,
				transformationData.getBufferedIOInstanceCreation());
		List<Expression> pathsGetArguments = transformationData.getPathExpressions()
			.stream()
			.map(pathExpression -> (Expression) astRewrite.createCopyTarget(pathExpression))
			.collect(Collectors.toList());
		MethodInvocation pathsGet = NodeBuilder.newMethodInvocation(ast, pathsTypeName,
				ast.newSimpleName(FilesUtil.GET), pathsGetArguments);

		Expression writeStringArgumentCopy = (Expression) astRewrite.createCopyTarget(writeStringArgument);

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
}
