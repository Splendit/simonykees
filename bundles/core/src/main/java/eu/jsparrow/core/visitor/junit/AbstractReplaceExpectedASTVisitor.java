package eu.jsparrow.core.visitor.junit;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * Contains some common functionalities for replacing expected exceptions with
 * {@code assertThrows}.
 * 
 * @since 3.24.0
 *
 */
abstract class AbstractReplaceExpectedASTVisitor extends AbstractAddImportASTVisitor {

	protected static final String EXCEPTION_TYPE_NAME = java.lang.Exception.class.getName();

	protected boolean verifyPosition(MethodDeclaration methodDeclaration, ASTNode nodeThrowingException) {
		Block testBody = methodDeclaration.getBody();
		List<Statement> statements = ASTNodeUtil.convertToTypedList(testBody.statements(), Statement.class);
		Statement lastStatement = statements.get(statements.size() - 1);
		return lastStatement == nodeThrowingException.getParent();
	}

	protected Optional<ITypeBinding> findExceptionTypeArgument(Expression excpetionClass) {
		ITypeBinding argumentType = excpetionClass.resolveTypeBinding();
		boolean isClass = ClassRelationUtil.isContentOfType(argumentType, java.lang.Class.class.getName());
		if (isClass && argumentType.isParameterizedType()) {
			ITypeBinding[] typeArguments = argumentType.getTypeArguments();
			if (typeArguments.length == 1) {
				ITypeBinding typeArgument = typeArguments[0];
				boolean isException = ClassRelationUtil.isContentOfType(typeArgument, EXCEPTION_TYPE_NAME)
						|| ClassRelationUtil.isInheritingContentOfTypes(typeArgument,
								Collections.singletonList(EXCEPTION_TYPE_NAME));
				if (isException) {
					return Optional.of(typeArgument);
				}
			}
		}

		return Optional.empty();
	}

	@SuppressWarnings("unchecked")
	protected ASTNode createThrowRunnable(ASTNode nodeThrowingException) {
		if (nodeThrowingException.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY) {
			return astRewrite.createCopyTarget(nodeThrowingException);
		} else {
			AST ast = nodeThrowingException.getAST();
			Block body = ast.newBlock();
			List<Statement> statements = body.statements();
			statements.add((Statement) astRewrite.createCopyTarget(nodeThrowingException.getParent()));
			return body;
		}
	}

	protected void removeThrowsDeclarations(MethodDeclaration methodDeclaration, ITypeBinding exceptionType) {
		List<Type> exceptionTypes = ASTNodeUtil.convertToTypedList(methodDeclaration.thrownExceptionTypes(),
				Type.class);
		ListRewrite listRewrite = astRewrite.getListRewrite(methodDeclaration,
				MethodDeclaration.THROWN_EXCEPTION_TYPES_PROPERTY);
		for (Type type : exceptionTypes) {
			ITypeBinding typeBinding = type.resolveBinding();
			if (ClassRelationUtil.compareITypeBinding(typeBinding, exceptionType)) {
				listRewrite.remove(type, null);
			}
		}
	}

}
