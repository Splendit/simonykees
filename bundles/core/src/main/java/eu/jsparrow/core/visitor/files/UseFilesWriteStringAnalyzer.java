package eu.jsparrow.core.visitor.files;

import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
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

	SimpleName writerVariableName;

	VariableDeclarationFragment fragmentDeclaringBufferedWriter;

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
		ITypeBinding typeBinding = writerVariableName.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(typeBinding, java.io.BufferedWriter.class.getName())) {
			return false;
		}

		if (methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return false;
		}
		ExpressionStatement writeInvocationStatement = (ExpressionStatement) methodInvocation.getParent();

		if (writeInvocationStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return false;
		}
		blockOfInvocationStatement = (Block) writeInvocationStatement.getParent();

		ASTNode declaringNode = compilationUnit.findDeclaringNode(writerVariableName.resolveBinding());
		if (declaringNode == null || declaringNode.getNodeType() != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return false;
		}

		fragmentDeclaringBufferedWriter = (VariableDeclarationFragment) declaringNode;
		if (fragmentDeclaringBufferedWriter.getLocationInParent() == VariableDeclarationExpression.FRAGMENTS_PROPERTY
				&& fragmentDeclaringBufferedWriter.getParent()
					.getLocationInParent() == TryStatement.RESOURCES2_PROPERTY
				&& fragmentDeclaringBufferedWriter.getParent()
					.getParent() == blockOfInvocationStatement.getParent()) {
			VariableDeclarationExpression parentVariableDeclarationExpression = (VariableDeclarationExpression) fragmentDeclaringBufferedWriter
				.getParent();
			if (parentVariableDeclarationExpression.fragments()
				.size() != 1) {
				return false;
			}
			tryStatement = (TryStatement) parentVariableDeclarationExpression.getParent();
		} else {
			return false;
		}

		if (fragmentDeclaringBufferedWriter.getInitializer() != null) {
			Expression initializer = fragmentDeclaringBufferedWriter.getInitializer();
			if (ClassRelationUtil.isNewInstanceCreationOf(initializer, java.io.BufferedWriter.class.getName())) {
				bufferedWriterInstanceCreation = (ClassInstanceCreation) initializer;
			}
		} else {
			return false;
		}

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

		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(
				writerVariableName);
		blockOfInvocationStatement.accept(visitor);
		return visitor.getUsages()
			.size() == 1;
	}

	private Optional<SimpleName> findInvocationExpressionAsSimpleName(MethodInvocation methodInvocation) {
		Expression methodInvocationExpression = methodInvocation.getExpression();
		if (methodInvocationExpression == null || methodInvocationExpression.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Optional.empty();
		}
		return Optional.of((SimpleName) methodInvocationExpression);
	}
}
