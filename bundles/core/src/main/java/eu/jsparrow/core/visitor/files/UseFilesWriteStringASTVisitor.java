package eu.jsparrow.core.visitor.files;

import java.util.Optional;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.compiler.ast.TryStatement;

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
		
		Expression stringArgument = extractStringArgument(methodInvocation).orElse(null);
		if(stringArgument == null || methodInvocation.getLocationInParent() != ExpressionStatement.EXPRESSION_PROPERTY) {
			return true;
		}
		ExpressionStatement expressionStatement = (ExpressionStatement)methodInvocation.getParent();
		if(expressionStatement.getLocationInParent() != Block.STATEMENTS_PROPERTY) {
			return true;
		}
		Block block = (Block)expressionStatement.getParent();

		return true;
	}

	private Optional<Expression> extractStringArgument(MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (!write.isEquivalentTo(methodBinding)) {
			return Optional.empty();
		}

		ITypeBinding methodExpressionTypeBinding = methodInvocation.getExpression()
			.resolveTypeBinding();
		if (!ClassRelationUtil.isContentOfType(methodExpressionTypeBinding, java.io.BufferedWriter.class.getName())) {
			return Optional.empty();
		}
		
		Expression stringArgument = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class).get(0);
	
		return Optional.of(stringArgument);
	}

}
