package eu.jsparrow.core.visitor.files;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import eu.jsparrow.core.visitor.impl.trycatch.TwrCommentsUtil;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

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
		analyzer.analyze(methodInvocation, getCompilationUnit())
			.ifPresent(result -> {
				transform(methodInvocation, result);
			});
		return true;
	}

	private void transform(MethodInvocation methodInvocation, UseFilesWriteStringAnalysisResult transformationData) {

		MethodInvocation writeStringMethodInvocation = createFilesWriteStringMethodInvocation(transformationData);
		astRewrite.replace(methodInvocation, writeStringMethodInvocation, null);

		/*
		 * TODO: 
		 * 1. use the method createNewTryStatement to completely replace the try statement with a new one. 
		 * This is a JDT bug that cannot handle TWR statements properly. For this reason, the TryStatement 
		 * has to be provided from the transformationData. I tried to track it back but it is very complicated
		 * process. Maybe you can do it faster. 
		 * 2. If you have a transformationData object, that should provide everything 
		 * you need for the transformation. You should not have more logic here that
		 * gets the parents of a node until you reach the node to transform. 
		 */
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

	@SuppressWarnings("unchecked")
	private TryStatement createNewTryStatement(TryStatement node, List<VariableDeclarationExpression> resourceList) {

		TryStatement tryStatement = getASTRewrite().getAST()
			.newTryStatement();
		tryStatement.resources()
			.addAll(resourceList);

		Block newBody = (Block) ASTNode.copySubtree(node.getAST(), node.getBody());
		
		List<Statement> newBodyStatements = ASTNodeUtil.convertToTypedList(newBody.statements(), Statement.class);
		Map<Integer, List<Comment>> comments = TwrCommentsUtil.findBodyComments(node, getCommentRewriter());
		CommentRewriter commentRewriter = getCommentRewriter();
		comments.forEach((key, value) -> {
			int newBodySize = newBodyStatements.size();
			if (newBodySize > key) {
				Statement statement = newBodyStatements.get(key);
				commentRewriter.saveBeforeStatement(statement, value);
			} else if (!newBodyStatements.isEmpty()) {
				Statement statement = newBodyStatements.get(newBodySize - 1);
				commentRewriter.saveAfterStatement(statement, value);
			} else {
				commentRewriter.saveBeforeStatement(node, value);
			}
		});

		List<CatchClause> newCatchClauses = ASTNodeUtil.convertToTypedList(node.catchClauses(), CatchClause.class)
			.stream()
			.map(clause -> (CatchClause) ASTNode.copySubtree(node.getAST(), clause))
			.collect(Collectors.toList());

		tryStatement.setBody(newBody);
		tryStatement.catchClauses()
			.addAll(newCatchClauses);
		tryStatement.setFinally((Block) ASTNode.copySubtree(node.getAST(), node.getFinally()));

		return tryStatement;
	}
	

}
