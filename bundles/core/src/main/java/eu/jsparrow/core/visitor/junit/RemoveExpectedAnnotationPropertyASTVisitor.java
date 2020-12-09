package eu.jsparrow.core.visitor.junit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class RemoveExpectedAnnotationPropertyASTVisitor extends AbstractAddImportASTVisitor {
	
	private static final String EXCEPTION_TYPE_NAME = java.lang.Exception.class.getName();
	private static final String ORG_JUNIT_TEST = "org.junit.Test"; //$NON-NLS-1$
	private static final String ASSERT_THROWS = "assertThrows"; //$NON-NLS-1$
	
	private String assertThrowsQualifiedName;
	
	public RemoveExpectedAnnotationPropertyASTVisitor(String assertThrowsQualifiedName) {
		this.assertThrowsQualifiedName = assertThrowsQualifiedName;
	}
	
	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (continueVisiting) {
			verifyStaticMethodImport(compilationUnit, assertThrowsQualifiedName);
		}
		return continueVisiting;
	}
	
	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		NormalAnnotation annotation = isTestAnnotatedMethod(methodDeclaration);
		if(annotation == null) {
			return false;
		}
		
		MemberValuePair expectedValuePair = findExpectedValuePair(annotation);
		if(expectedValuePair == null) {
			return false;
		}
		
		Expression expectedExpressionExpression = expectedValuePair.getValue();
		ITypeBinding exceptionType = findExceptionTypeArgument(expectedExpressionExpression).orElse(null);
		if(exceptionType == null) {
			return false;
		}
		
		ExpressionsThrowingExceptionVisitor throwingExceptionsVisitor = new ExpressionsThrowingExceptionVisitor(
				exceptionType);
		if (!hasSingleNodeThrowingException(methodDeclaration, throwingExceptionsVisitor)) {
			return false;
		}
		
		ASTNode nodeThrowingException = throwingExceptionsVisitor.getNodesThrowingExpectedException()
				.get(0);
		boolean isLastStatement = verifyPosition(methodDeclaration, nodeThrowingException);
		if (!isLastStatement) {
			return false;
		}
		
		refactor(methodDeclaration, exceptionType, nodeThrowingException, expectedExpressionExpression);
		
		return true;
	}
	
	private MemberValuePair findExpectedValuePair(NormalAnnotation annotation) {
		List<MemberValuePair> values = ASTNodeUtil.convertToTypedList(annotation.values(), MemberValuePair.class);
		for(MemberValuePair memberValuePair : values) {
			SimpleName name = memberValuePair.getName();
			String identifier = name.getIdentifier();
			if("expected".equals(identifier)) {
				return memberValuePair;
			}
		}
		return null;
	}

	private NormalAnnotation isTestAnnotatedMethod(MethodDeclaration methodDeclaration) {
		List<NormalAnnotation> annotations = ASTNodeUtil.convertToTypedList(methodDeclaration.modifiers(),
				NormalAnnotation.class);

		for(NormalAnnotation annotation : annotations) {
			Name typeName = annotation.getTypeName();
			ITypeBinding annotationTypeBinding = typeName.resolveTypeBinding();
			boolean isTest = ClassRelationUtil.isContentOfTypes(annotationTypeBinding,
					Arrays.asList(ORG_JUNIT_TEST));
			if(isTest) {
				return annotation;
			}
		}
		return null;
	}
	
	private boolean hasSingleNodeThrowingException(MethodDeclaration methodDeclaration,
			ExpressionsThrowingExceptionVisitor throwingExceptionsVisitor) {
		Block body = methodDeclaration.getBody();
		body.accept(throwingExceptionsVisitor);
		List<ASTNode> nodesThrowingException = throwingExceptionsVisitor.getNodesThrowingExpectedException();
		if (nodesThrowingException.size() != 1) {
			return false;
		}
		ASTNode nodeThrowingException = nodesThrowingException.get(0);
		return nodeThrowingException.getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY
				|| nodeThrowingException.getLocationInParent() == ThrowStatement.EXPRESSION_PROPERTY;
	}
	
	// PULL up
	private Optional<ITypeBinding> findExceptionTypeArgument(Expression excpetionClass) {
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
	
	// pull up
	private boolean verifyPosition(MethodDeclaration methodDeclaration, ASTNode nodeThrowingException) {
		Block testBody = methodDeclaration.getBody();
		List<Statement> statements = ASTNodeUtil.convertToTypedList(testBody.statements(), Statement.class);
		Statement lastStatement = statements.get(statements.size() - 1);
		return lastStatement == nodeThrowingException.getParent();
	}
	
	@SuppressWarnings("unchecked")
	private void refactor(MethodDeclaration methodDeclaration,
			ITypeBinding exceptionType, ASTNode nodeThrowingException, Expression expectedException) {


		AST ast = methodDeclaration.getAST();
		Optional<Name> qualifiedPrefix = addImportForStaticMethod(assertThrowsQualifiedName, methodDeclaration);
		MethodInvocation assertThrows = ast.newMethodInvocation();
		assertThrows.setName(ast.newSimpleName(ASSERT_THROWS));
		qualifiedPrefix.ifPresent(assertThrows::setExpression);
		Expression firstArg = (Expression) astRewrite.createCopyTarget(expectedException);
		LambdaExpression lambdaExpression = ast.newLambdaExpression();
		ASTNode lambdaBody = createThrowRunnable(nodeThrowingException);
		lambdaExpression.setBody(lambdaBody);
		List<Expression> assertionArguments = assertThrows.arguments();
		assertionArguments.add(firstArg);
		assertionArguments.add(lambdaExpression);

		removeThrowsDeclarations(methodDeclaration, exceptionType);
		
		ExpressionStatement assertionStatement = ast.newExpressionStatement(assertThrows);
		astRewrite.replace(nodeThrowingException.getParent(), assertionStatement, null);
		astRewrite.remove(expectedException.getParent(), null);
		onRewrite();
		
	}

	private void removeThrowsDeclarations(MethodDeclaration methodDeclaration, ITypeBinding exceptionType) {
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

	@SuppressWarnings("unchecked")
	private ASTNode createThrowRunnable(ASTNode nodeThrowingException) {
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


}
