package at.splendit.simonykees.core.visitor;

import java.util.Collections;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach.AbstractEnhancedForLoopToStreamASTVisitor;

/**
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
public class StringBuildingLoopASTVisitor extends AbstractEnhancedForLoopToStreamASTVisitor {

	private static final String COLLECT = "collect"; //$NON-NLS-1$
	private static final String JOINING = "joining"; //$NON-NLS-1$
	private static final String TO_STRING = "toString"; //$NON-NLS-1$
	private static final String APPEND = "append"; //$NON-NLS-1$
	
	private JavaVersion javaVersion;
	
	public StringBuildingLoopASTVisitor(JavaVersion javaVersion) {
		this.javaVersion = javaVersion;
	}

	@Override
	public boolean visit(EnhancedForStatement loopNode) {

		Expression loopExpression = loopNode.getExpression();
		SingleVariableDeclaration loopParameter = loopNode.getParameter();
		ASTNode loopParent = loopNode.getParent();
		Block parentBlock;
		if(ASTNode.BLOCK == loopParent.getNodeType()) {
			parentBlock = (Block)loopParent;
		} else {
			return true;
		}

		ExpressionStatement singleBodyStatement = getSingleBodyStatement(loopNode).orElse(null);
		if (singleBodyStatement == null) {
			return true;
		}

		SimpleName resultVariable = findSumVariableName(loopParameter, singleBodyStatement).orElse(null);
		if (resultVariable == null) {
			return true;
		}
		
		
		if(this.javaVersion.atLeast(JavaVersion.JAVA_1_8)) {
			// create the collection statement
			MethodInvocation collect = createCollectInvocation();
			MethodInvocation streamExpression;
			if(isCollectionOfStrings(loopExpression)) {
				// expression.stream()
				streamExpression = createStreamFromCollection(loopExpression);
			} else if(isArrayOfStrings(loopExpression)) {
				// Arrays.stream(expression)
				streamExpression = createStreamFromArray(loopExpression);
			} else {
				return true;
			}
			
			collect.setExpression(streamExpression);
			ASTNode newStatement = concatCollectToResult(collect, resultVariable);
			//TODO: write a similar method to assign the collect to result
			//TODO: distinguish between assign and concat depending on where is the result var declared and used *
			
			astRewrite.replace(loopNode, newStatement, null);
			
			
		} else if(this.javaVersion.atLeast(JavaVersion.JAVA_1_5)) {
			// create the stringBuilder 
			String stringBuilderId = generateStringBuilderIdentifier(loopNode, resultVariable.getIdentifier());
			VariableDeclarationStatement sbDeclaration = introduceStringBuilder(stringBuilderId);
			ListRewrite blockRewrite = astRewrite.getListRewrite(parentBlock, Block.STATEMENTS_PROPERTY);
			blockRewrite.insertBefore(sbDeclaration, loopNode, null);
			replaceByStringBuilderAppend(singleBodyStatement, loopParameter.getName(), stringBuilderId);
			ExpressionStatement expressionStatement = assignStringBuilderToResult(stringBuilderId, resultVariable);
			//TODO: write a similar method to assign the collect to result
			//TODO: distinguish between assign and concat depending on where is the result var declared and used *
			
			blockRewrite.insertAfter(expressionStatement, loopNode, null);
		}

		return true;
	}

	private String generateStringBuilderIdentifier(EnhancedForStatement loopNode, String prefix) {
		// TODO: find all declared names in the scope (list of parent blocks untill reaching cu)
		// TODO: check if prefix + sb occurs in the list of declared names
		// 			add a suffix until it doesn't
		return prefix + "Sb";
	}

	private ASTNode concatCollectToResult(MethodInvocation collect, SimpleName resultVariable) {
		AST ast = astRewrite.getAST();
		Assignment assignment = ast.newAssignment();
		assignment.setOperator(Assignment.Operator.PLUS_ASSIGN);
		assignment.setLeftHandSide((SimpleName)astRewrite.createCopyTarget(resultVariable));
		assignment.setRightHandSide(collect);
		return ast.newExpressionStatement(assignment);
	}

	private MethodInvocation createStreamFromArray(Expression loopExpression) {
		AST ast = astRewrite.getAST();
		MethodInvocation stream = ast.newMethodInvocation();
		stream.setName(ast.newSimpleName(STREAM));
		stream.setExpression(ast.newSimpleName(java.util.Arrays.class.getSimpleName()));
		addImports.add(java.util.Arrays.class.getName());
		ListRewrite argRewriter = astRewrite.getListRewrite(stream, MethodInvocation.ARGUMENTS_PROPERTY);
		argRewriter.insertFirst(loopExpression, null);
		return stream;
	}

	private MethodInvocation createStreamFromCollection(Expression loopExpression) {
		Expression collectionExpression = createExpressionForStreamMethodInvocation(loopExpression);
		AST ast = astRewrite.getAST();
		MethodInvocation stream = ast.newMethodInvocation();
		stream.setName(ast.newSimpleName(STREAM));
		stream.setExpression(collectionExpression);
		return stream;
	}

	private MethodInvocation createCollectInvocation() {
		AST ast = astRewrite.getAST();
		MethodInvocation collect = ast.newMethodInvocation();
		collect.setName(ast.newSimpleName(COLLECT));
		
		MethodInvocation collectorsJoining = ast.newMethodInvocation();
		collectorsJoining.setName(ast.newSimpleName(JOINING));
		collectorsJoining.setExpression(ast.newSimpleName(java.util.stream.Collectors.class.getSimpleName()));
		this.addImports.add(java.util.stream.Collectors.class.getName());
		
		ListRewrite argRewriter = astRewrite.getListRewrite(collect, MethodInvocation.ARGUMENTS_PROPERTY);
		argRewriter.insertFirst(collectorsJoining, null);
		
		return collect;
	}

	private ExpressionStatement assignStringBuilderToResult(String sbName, SimpleName resultVariable) {
		AST ast = astRewrite.getAST();
		Assignment assignment = ast.newAssignment();
		assignment.setOperator(Assignment.Operator.PLUS_ASSIGN);
		assignment.setLeftHandSide((SimpleName)astRewrite.createCopyTarget(resultVariable));
		
		MethodInvocation sbToString = ast.newMethodInvocation();
		sbToString.setName(ast.newSimpleName(TO_STRING));
		sbToString.setExpression(ast.newSimpleName(sbName));
		assignment.setRightHandSide(sbToString);
		
		return ast.newExpressionStatement(assignment);
	}

	private void replaceByStringBuilderAppend(ExpressionStatement singleBodyStatement, SimpleName loopParameter, String sbName) {
		AST ast = astRewrite.getAST();
		MethodInvocation append = ast.newMethodInvocation();
		append.setName(ast.newSimpleName(APPEND));
		append.setExpression(ast.newSimpleName(sbName));
		
		ListRewrite argRewriter = astRewrite.getListRewrite(append, MethodInvocation.ARGUMENTS_PROPERTY);
		argRewriter.insertFirst((SimpleName)astRewrite.createCopyTarget(loopParameter), null);
		
		ExpressionStatement expressionStatement = ast.newExpressionStatement(append);
		astRewrite.replace(singleBodyStatement, expressionStatement, null);
	}

	private VariableDeclarationStatement introduceStringBuilder(String identifier) {
		AST ast = astRewrite.getAST();
		
		ClassInstanceCreation initializer = ast.newClassInstanceCreation();
		initializer.setType(ast.newSimpleType(ast.newSimpleName(StringBuilder.class.getSimpleName())));
		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName(identifier));
		fragment.setInitializer(initializer);
		return ast.newVariableDeclarationStatement(fragment);
	}

	private boolean isCollectionOfStrings(Expression loopExpression) {
		ITypeBinding expressionBinding = loopExpression.resolveTypeBinding();
		if(expressionBinding != null && expressionBinding.isParameterizedType()) {
			ITypeBinding[] typeArguments = expressionBinding.getTypeArguments();
			if(typeArguments.length == 1) {
				return ClassRelationUtil.isContentOfTypes(typeArguments[0], Collections.singletonList(String.class.getName()));
			}
		}
		return false;
	}
	
	private boolean isArrayOfStrings(Expression loopExpression) {
		ITypeBinding expressionBinding = loopExpression.resolveTypeBinding();
		if(expressionBinding.isArray()) {
			ITypeBinding componentType = expressionBinding.getComponentType();
			return ClassRelationUtil.isContentOfTypes(componentType, Collections.singletonList(String.class.getName()));
		}
		return false;
	}
}
