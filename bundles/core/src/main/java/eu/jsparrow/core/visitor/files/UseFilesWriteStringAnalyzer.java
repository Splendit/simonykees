package eu.jsparrow.core.visitor.files;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.visitor.sub.SignatureData;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesASTVisitor;

public class UseFilesWriteStringAnalyzer {

	private final SignatureData write = new SignatureData(java.io.Writer.class, "write", java.lang.String.class); //$NON-NLS-1$

	Expression writeStringArgument;

	SimpleName writerVariableName;

	VariableDeclarationFragment fragmentDeclaringBufferedWriter;

	VariableDeclarationExpression parentVariableDeclarationExpression;

	ClassInstanceCreation bufferedWriterInstanceCreation;

	Expression bufferedWriterArgument;

	Block blockOfInvocationStatement;

	TryStatement tryStatement;

	boolean analyze(MethodInvocation methodInvocation, CompilationUnit compilationUnit) {
		if (!write.isEquivalentTo(methodInvocation.resolveMethodBinding())) {
			return false;
		}
		writerVariableName = findInvocationExpressionAsSimpleName(methodInvocation).orElse(null);
		if (writerVariableName == null) {
			return false;
		}

		IBinding methodExpressionBinding = writerVariableName.resolveBinding();
		ASTNode declaringNode = compilationUnit.findDeclaringNode(methodExpressionBinding);
		if (declaringNode == null || declaringNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return false;
		}
		fragmentDeclaringBufferedWriter = (VariableDeclarationFragment) declaringNode;

		bufferedWriterInstanceCreation = FilesUtil
			.findBufferIOInstanceCreationAsInitializer(fragmentDeclaringBufferedWriter,
					java.io.BufferedWriter.class.getName())
			.orElse(null);
		if (bufferedWriterInstanceCreation == null) {
			return false;
		}

		bufferedWriterArgument = FilesUtil
			.findBufferedIOArgument(bufferedWriterInstanceCreation, java.io.FileWriter.class.getName())
			.orElse(null);

		if (bufferedWriterArgument == null) {
			return false;
		}

		writeStringArgument = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.get(0);

		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return false;
		}
		ExpressionStatement writeInvocationStatement = (ExpressionStatement) methodInvocation.getParent();

		if (writeInvocationStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return false;
		}

		blockOfInvocationStatement = (Block) writeInvocationStatement.getParent();
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(writerVariableName);
		blockOfInvocationStatement.accept(visitor);
		List<SimpleName> usages = visitor.getUsages();
		int usagesCount = usages.size();
		if (usagesCount != 1) {
			return false;
		}
		if (blockOfInvocationStatement.getLocationInParent() != TryStatement.BODY_PROPERTY) {
			return false;
		}

		tryStatement = (TryStatement) blockOfInvocationStatement.getParent();

		parentVariableDeclarationExpression = (VariableDeclarationExpression) fragmentDeclaringBufferedWriter
			.getParent();
		
		if(parentVariableDeclarationExpression.fragments().size() != 1) {
			return false;
		}

		return parentVariableDeclarationExpression.getParent() == tryStatement;
	}

	private Optional<SimpleName> findInvocationExpressionAsSimpleName(MethodInvocation methodInvocation) {
		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression == null || methodInvocationExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Optional.empty();
		}
		return Optional.of((SimpleName) methodInvocationExpression);
	}
}
