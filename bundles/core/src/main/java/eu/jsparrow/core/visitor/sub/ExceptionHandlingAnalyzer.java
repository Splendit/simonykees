package eu.jsparrow.core.visitor.sub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.UnionType;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

public class ExceptionHandlingAnalyzer {

	private static final String CHECKED_EXCEPTION_SUPERTYPE = java.lang.Exception.class.getName();
	private static final List<String> CHECKED_EXCEPTION_SUPERTYPE_LIST = Collections
		.singletonList(CHECKED_EXCEPTION_SUPERTYPE);

	private static final String RUNTIME_EXCEPTION = java.lang.RuntimeException.class.getName();
	private static final List<String> RUNTIME_EXCEPTION_LIST = Collections.singletonList(CHECKED_EXCEPTION_SUPERTYPE);

	public static boolean checkThrowStatement(ASTNode excludedAncestor, ThrowStatement throwStatement) {
		ITypeBinding exceptionTypeBinding = throwStatement.getExpression()
			.resolveTypeBinding();
		return analyzeExceptionHandling(excludedAncestor, throwStatement, exceptionTypeBinding);
	}

	private static boolean analyzeExceptionHandling(ASTNode excludedAncestor, ASTNode node,
			ITypeBinding exceptionTypeBinding) {
		if (exceptionTypeBinding == null) {
			return false;
		}

		if (!ClassRelationUtil.isContentOfType(exceptionTypeBinding, CHECKED_EXCEPTION_SUPERTYPE) &&
				!ClassRelationUtil.isInheritingContentOfTypes(exceptionTypeBinding, CHECKED_EXCEPTION_SUPERTYPE_LIST)) {
			return true;
		}

		if (ClassRelationUtil.isContentOfType(exceptionTypeBinding, RUNTIME_EXCEPTION) &&
				ClassRelationUtil.isInheritingContentOfTypes(exceptionTypeBinding, RUNTIME_EXCEPTION_LIST)) {
			return true;
		}

		return analyzeExceptionHandlingRecursively(excludedAncestor, node, exceptionTypeBinding);
	}

	private static boolean analyzeExceptionHandlingRecursively(ASTNode excludedAncestor, ASTNode node,
			ITypeBinding exceptionTypeBinding) {
		ASTNode parent = node.getParent();
		TryStatement tryStatement = null;
		while (parent != null && parent != excludedAncestor) {
			if (parent.getLocationInParent() == TryStatement.BODY_PROPERTY) {
				tryStatement = (TryStatement) parent.getParent();
				break;
			}
			parent = parent.getParent();
		}
		if (tryStatement == null) {
			return false;
		}
		List<String> currentHandledExceptionsTypes = collectHandledExceptionTypes(tryStatement);

		if (ClassRelationUtil.isContentOfTypes(exceptionTypeBinding, currentHandledExceptionsTypes) ||
				ClassRelationUtil.isInheritingContentOfTypes(exceptionTypeBinding, currentHandledExceptionsTypes)) {
			return true;
		}
		return analyzeExceptionHandlingRecursively(excludedAncestor, tryStatement, exceptionTypeBinding);
	}

	private static List<String> collectHandledExceptionTypes(TryStatement tryStatementNode) {
		List<Type> exceptionTypes = new ArrayList<>();
		ASTNodeUtil.convertToTypedList(tryStatementNode.catchClauses(), CatchClause.class)
			.stream()
			.map(CatchClause::getException)
			.map(SingleVariableDeclaration::getType)
			.forEach(exceptionType -> {
				if (exceptionType.getNodeType() == ASTNode.UNION_TYPE) {
					UnionType unionType = (UnionType) exceptionType;
					exceptionTypes.addAll(ASTNodeUtil.convertToTypedList(unionType.types(), Type.class));
				} else {
					exceptionTypes.add(exceptionType);
				}
			});

		return exceptionTypes.stream()
			.map(Type::resolveBinding)
			.filter(Objects::nonNull)
			.map(ITypeBinding::getQualifiedName)
			.collect(Collectors.toList());
	}

	// ???
	static List<String> collectHandledExceptionsForAutomaticClose(ASTNode excludedAncestor,
			TryStatement tryStatementNode) {
		List<TryStatement> tryStatements = new ArrayList<>();
		tryStatements.add(tryStatementNode);
		ASTNode parent = tryStatementNode.getParent();
		while (parent != null && parent != excludedAncestor) {
			if (parent.getLocationInParent() == TryStatement.BODY_PROPERTY) {
				tryStatements.add((TryStatement) parent.getParent());
			}
			parent = parent.getParent();
		}

		return tryStatements.stream()
			.flatMap(tryStatement -> collectHandledExceptionTypes(tryStatementNode).stream())
			.collect(Collectors.toList());
	}

	private ExceptionHandlingAnalyzer() {
		/*
		 * private default constructor to hide implicit public one
		 */
	}

}
