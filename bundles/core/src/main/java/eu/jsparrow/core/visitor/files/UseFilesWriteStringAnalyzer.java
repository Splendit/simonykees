package eu.jsparrow.core.visitor.files;

import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import eu.jsparrow.core.visitor.sub.SignatureData;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesASTVisitor;

public class UseFilesWriteStringAnalyzer {

	private final SignatureData write = new SignatureData(java.io.Writer.class, "write", java.lang.String.class); //$NON-NLS-1$

	Expression writeStringArgument;

	Expression bufferedIOInitializer;

	Expression bufferedWriterInstanceCreationArgument;

	List<Expression> filesNewBufferedWriterInvocationArguments;

	TryStatement tryStatement;

	boolean analyze(MethodInvocation methodInvocation, CompilationUnit compilationUnit) {
		if (!write.isEquivalentTo(methodInvocation.resolveMethodBinding())) {
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
		Block blockOfInvocationStatement = (Block) writeInvocationStatement.getParent();

		SimpleName writerVariableName = findWriterVariableNameUsedOnce(methodInvocation, blockOfInvocationStatement)
			.orElse(null);
		if (writerVariableName == null) {
			return false;
		}
		ASTNode declaringNode = compilationUnit.findDeclaringNode(writerVariableName.resolveBinding());
		if (declaringNode == null || declaringNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return false;
		}
		VariableDeclarationFragment fragmentDeclaringBufferedWriter = (VariableDeclarationFragment) declaringNode;
		tryStatement = findSurroundingTryStatement(fragmentDeclaringBufferedWriter, blockOfInvocationStatement)
			.orElse(null);
		if (tryStatement == null) {
			return false;
		}

		bufferedIOInitializer = fragmentDeclaringBufferedWriter.getInitializer();
		if (bufferedIOInitializer == null) {
			return false;
		}
		if (ClassRelationUtil.isNewInstanceCreationOf(bufferedIOInitializer, java.io.BufferedWriter.class.getName())) {
			ClassInstanceCreation bufferedWriterInstanceCreation = (ClassInstanceCreation) bufferedIOInitializer;
			bufferedWriterInstanceCreationArgument = FilesUtil
				.findBufferedIOArgument(bufferedWriterInstanceCreation, java.io.FileWriter.class.getName())
				.orElse(null);
			if (bufferedWriterInstanceCreationArgument == null) {
				return false;
			}
		} else if (isFilesNewBufferedWriterInvocation(bufferedIOInitializer)) {
			MethodInvocation filesNewBufferedWriterInvocation = (MethodInvocation) bufferedIOInitializer;
			filesNewBufferedWriterInvocationArguments = ASTNodeUtil
				.convertToTypedList(filesNewBufferedWriterInvocation.arguments(), Expression.class);
			if (filesNewBufferedWriterInvocationArguments.isEmpty()
					|| filesNewBufferedWriterInvocationArguments.size() > 2) {
				return false;
			}

			ITypeBinding firstArgumentTypeBinding = filesNewBufferedWriterInvocationArguments.get(0)
				.resolveTypeBinding();
			if (!ClassRelationUtil.isContentOfType(firstArgumentTypeBinding, java.nio.file.Path.class.getName())) {
				return false;
			}
			if (filesNewBufferedWriterInvocationArguments.size() == 2) {
				ITypeBinding secondArgumentTypeBinding = filesNewBufferedWriterInvocationArguments.get(1)
					.resolveTypeBinding();
				if (!ClassRelationUtil.isContentOfType(secondArgumentTypeBinding,
						java.nio.charset.Charset.class.getName())) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean isFilesNewBufferedWriterInvocation(Expression initializer) {
		if (initializer.getNodeType() != ASTNode.METHOD_INVOCATION) {
			return false;
		}
		MethodInvocation invocation = (MethodInvocation) initializer;
		Expression invocationExpression = invocation.getExpression();
		if (invocationExpression == null) {
			return false;
		}
		IMethodBinding methodBinding = invocation.resolveMethodBinding();
		if (!ClassRelationUtil.isContentOfType(methodBinding
			.getDeclaringClass(), java.nio.file.Files.class.getName())) {
			return false;
		}
		if (!Modifier.isStatic(methodBinding.getModifiers())) {
			return false;
		}
		return methodBinding.getName()
			.equals("newBufferedWriter"); //$NON-NLS-1$
	}

	private Optional<SimpleName> findWriterVariableNameUsedOnce(MethodInvocation methodInvocation,
			Block blockOfInvocationStatement) {
		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression == null || methodInvocationExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Optional.empty();
		}
		SimpleName writerVariableName = (SimpleName) methodInvocationExpression;
		ITypeBinding typeBinding = writerVariableName.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(typeBinding, java.io.BufferedWriter.class.getName())) {
			return Optional.empty();
		}
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(
				writerVariableName);
		blockOfInvocationStatement.accept(visitor);
		if (visitor.getUsages()
			.size() != 1) {
			return Optional.empty();
		}
		return Optional.of(writerVariableName);
	}

	private Optional<TryStatement> findSurroundingTryStatement(
			VariableDeclarationFragment fragmentDeclaringBufferedWriter, Block blockOfInvocationStatement) {
		if (fragmentDeclaringBufferedWriter.getLocationInParent() == VariableDeclarationExpression.FRAGMENTS_PROPERTY
				&& fragmentDeclaringBufferedWriter.getParent()
					.getLocationInParent() == TryStatement.RESOURCES2_PROPERTY
				&& fragmentDeclaringBufferedWriter.getParent()
					.getParent() == blockOfInvocationStatement.getParent()) {
			VariableDeclarationExpression parentVariableDeclarationExpression = (VariableDeclarationExpression) fragmentDeclaringBufferedWriter
				.getParent();
			if (parentVariableDeclarationExpression.fragments()
				.size() != 1) {
				return Optional.empty();
			}
			return Optional.of((TryStatement) parentVariableDeclarationExpression.getParent());
		} else {
			return Optional.empty();
		}
	}
}
