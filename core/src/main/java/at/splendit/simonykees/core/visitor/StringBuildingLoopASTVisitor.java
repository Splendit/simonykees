package at.splendit.simonykees.core.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import at.splendit.simonykees.core.util.ASTNodeUtil;
import at.splendit.simonykees.core.util.ClassRelationUtil;
import at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach.AbstractEnhancedForLoopToStreamASTVisitor;
import at.splendit.simonykees.core.visitor.sub.VariableDeclarationsVisitor;

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
	private static final String STRING_BUILDER_CORE_IDENTIFIER = "Sb"; //$NON-NLS-1$
	
	private List<String> generatedIdsPerMethod = new ArrayList<>();
	
	private JavaVersion javaVersion;
	
	public StringBuildingLoopASTVisitor(JavaVersion javaVersion) {
		this.javaVersion = javaVersion;
	}
	
	@Override
	public void endVisit(MethodDeclaration methodDeclaration) {
		generatedIdsPerMethod.clear();
	}
	
	@Override
	public void endVisit(Initializer initializer) {
		generatedIdsPerMethod.clear();
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
			ASTNode newStatement;
			Optional<VariableDeclarationFragment> optFragment = isReassignable(resultVariable, loopNode);
			if(optFragment.isPresent()) {
				newStatement = assignCollectToResult(collect, resultVariable);
				VariableDeclarationFragment fragment = optFragment.get();
				VariableDeclarationStatement oldDeclStatement = (VariableDeclarationStatement)fragment.getParent();
				removeOldSumDeclaration(oldDeclStatement, fragment);
			} else {
				newStatement = concatCollectToResult(collect, resultVariable);
			}
			
			astRewrite.replace(loopNode, newStatement, null);
			
			
		} else if(this.javaVersion.atLeast(JavaVersion.JAVA_1_5)) {
			// create the stringBuilder 
			String stringBuilderId = generateStringBuilderIdentifier(loopNode, resultVariable.getIdentifier());
			VariableDeclarationStatement sbDeclaration = introduceStringBuilder(stringBuilderId);
			ListRewrite blockRewrite = astRewrite.getListRewrite(parentBlock, Block.STATEMENTS_PROPERTY);
			blockRewrite.insertBefore(sbDeclaration, loopNode, null);
			replaceByStringBuilderAppend(singleBodyStatement, loopParameter.getName(), stringBuilderId);
			Statement expressionStatement;
			Optional<VariableDeclarationFragment> optFragment = isReassignable(resultVariable, loopNode);
			if(optFragment.isPresent()) {
				VariableDeclarationFragment fragment = optFragment.get();
				expressionStatement = assignStringBuilderToResult(stringBuilderId, resultVariable);
				VariableDeclarationStatement oldDeclStatement = (VariableDeclarationStatement)fragment.getParent();
				removeOldSumDeclaration(oldDeclStatement, fragment);
				
			} else {
				expressionStatement = concatStringBuilderToResult(stringBuilderId, resultVariable);
			}

			
			blockRewrite.insertAfter(expressionStatement, loopNode, null);
		}

		return true;
	}

	private VariableDeclarationStatement assignStringBuilderToResult(String stringBuilderId, SimpleName resultVariable) {
		AST ast = astRewrite.getAST();
		
		MethodInvocation sbToString = ast.newMethodInvocation();
		sbToString.setName(ast.newSimpleName(TO_STRING));
		sbToString.setExpression(ast.newSimpleName(stringBuilderId));
		
		VariableDeclarationFragment newDeclFragment = ast.newVariableDeclarationFragment();
		newDeclFragment.setName((SimpleName)astRewrite.createCopyTarget(resultVariable));
		newDeclFragment.setInitializer(sbToString);
		
		VariableDeclarationStatement newDecl = ast.newVariableDeclarationStatement(newDeclFragment);
		ITypeBinding resultType = resultVariable.resolveTypeBinding();
		SimpleType type = ast.newSimpleType(ast.newSimpleName(resultType.getName()));
		newDecl.setType(type);
		
		return newDecl;
	}

	private VariableDeclarationStatement assignCollectToResult(MethodInvocation collect2, SimpleName resultVariable) {
		AST ast = astRewrite.getAST();

		VariableDeclarationFragment newDeclFragment = ast.newVariableDeclarationFragment();
		newDeclFragment.setName((SimpleName)astRewrite.createCopyTarget(resultVariable));
		newDeclFragment.setInitializer(collect2);
		
		VariableDeclarationStatement newDecl = ast.newVariableDeclarationStatement(newDeclFragment);
		ITypeBinding resultType = resultVariable.resolveTypeBinding();
		SimpleType type = ast.newSimpleType(ast.newSimpleName(resultType.getName()));
		newDecl.setType(type);
		
		return newDecl;
	}

	private Optional<VariableDeclarationFragment> isReassignable(SimpleName resultVariable, EnhancedForStatement loopNode) {
		Block block = ASTNodeUtil.getSpecificAncestor(loopNode, Block.class);
		ReassignableResultVisitor analyzer = new ReassignableResultVisitor(block, loopNode, resultVariable);
		block.accept(analyzer);
		return Optional.ofNullable(analyzer.getDeclarationFragment());
	}

	private String generateStringBuilderIdentifier(EnhancedForStatement loopNode, String prefix) {
		ASTNode scope = ASTNodeUtil.findScope(loopNode);
		VariableDeclarationsVisitor declVisitor = new VariableDeclarationsVisitor();
		scope.accept(declVisitor);
		List<String> declaredIds = declVisitor.getVariableDeclarationNames().stream().map(SimpleName::getIdentifier)
				.collect(Collectors.toList());
		int count = 0;
		String defaultIdentifier = prefix + STRING_BUILDER_CORE_IDENTIFIER;
		String sbIdentifier = defaultIdentifier;

		while (declaredIds.contains(sbIdentifier) || generatedIdsPerMethod.contains(sbIdentifier)) {
			count++;
			sbIdentifier = defaultIdentifier + count;
		}
		
		generatedIdsPerMethod.add(sbIdentifier);

		return sbIdentifier;
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

	private ExpressionStatement concatStringBuilderToResult(String sbName, SimpleName resultVariable) {
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
		VariableDeclarationStatement varDeclStatement = ast.newVariableDeclarationStatement(fragment);
		varDeclStatement.setType(ast.newSimpleType(ast.newSimpleName(StringBuilder.class.getSimpleName())));
		return varDeclStatement;
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
	
	private class ReassignableResultVisitor extends ASTVisitor {
		private Block block;
		private EnhancedForStatement loop;
		private SimpleName resultName;
		
		private boolean beforeDeclaration = true;
		private boolean beforeLoop = true;
		private boolean keepSearching = true;
		private VariableDeclarationFragment fragment;
		
		public ReassignableResultVisitor(Block block, EnhancedForStatement loop, SimpleName resultName) {
			this.block = block;
			this.loop = loop;
			this.resultName = resultName;
		}
		
		@Override
		public boolean preVisit2(ASTNode node) {
			return keepSearching;
		}
		
		@Override
		public boolean visit(SimpleName simpleName) {
			if (simpleName.getIdentifier().equals(this.resultName.getIdentifier())
					&& VariableDeclarationFragment.NAME_PROPERTY != simpleName.getLocationInParent()) {
				IBinding binding = simpleName.resolveBinding();
				StructuralPropertyDescriptor propertyDescriptor = simpleName.getLocationInParent();
				if (IBinding.VARIABLE == binding.getKind() && FieldAccess.NAME_PROPERTY != propertyDescriptor
						&& QualifiedName.NAME_PROPERTY != propertyDescriptor) {
					/*
					 * a reference of the variable is found
					 */
					keepSearching = false;
					fragment = null;
				}
			}
			return true;
		}
		
		@Override
		public boolean visit(VariableDeclarationFragment fragment) {
			SimpleName fragmentName = fragment.getName();
			if(fragmentName.getIdentifier().equals(resultName.getIdentifier())) {
				Expression initializer = fragment.getInitializer();
				if(ASTNode.STRING_LITERAL == initializer.getNodeType()) {
					StringLiteral stringLiteral = (StringLiteral)initializer;
					if(stringLiteral.getLiteralValue().isEmpty()) {
						this.fragment = fragment;
						beforeDeclaration = false;
					} else {
						keepSearching = false;
					}
				}
			}
			return true;
		}

		@Override
		public boolean visit(EnhancedForStatement loop) {
			if(this.loop == loop) {
				beforeLoop = false;
				keepSearching = false;
			}
			return beforeLoop;
		}
		
		@Override
		public boolean visit(Block block) {
			return this.block == block || !beforeDeclaration;
		}
		
		public VariableDeclarationFragment getDeclarationFragment() {
			return this.fragment;
		}
	}
}
