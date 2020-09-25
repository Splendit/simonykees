package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
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

import eu.jsparrow.core.visitor.loop.stream.AbstractEnhancedForLoopToStreamASTVisitor;
import eu.jsparrow.core.visitor.sub.VariableDeclarationsVisitor;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Analyzes the occurrences of the {@link EnhancedForStatement}s and checks
 * whether they are only used for concatenating the strings of a collection or
 * array. It considers two cases:
 * 
 * <ul>
 * <li>If the compliance level of the java project is set
 * to @{@link JavaVersion#JAVA_1_8} or later, then the whole loop is replaced
 * with an invocation of {@link Stream#collect(Collector)} for joining the
 * strings. For example, the following code:
 * 
 * <pre>
 * <code>
 *  	List<String> collectionOfStrings = generateStringList(input);
 *		String result = "";
 *		for(String val : collectionOfStrings) {
 *			result = result + val;
 *		}
 *  </code>
 * 
 * is replaced with:
 * 
 * <pre>
 * <code>
		List<String> collectionOfStrings = generateStringList(input);
		String result = collectionOfStrings.stream().collect(Collectors.joining());
 *  </code>
 * 
 * A collection, is converted into a stream by invoking the
 * {@link Collection#stream()} whereas, an array is converted into a stream by
 * invoking {@link Arrays#stream(Object[])}.</li>
 * <li>Otherwise, if the compliance level is set to {@link JavaVersion#JAVA_1_5}
 * or later, then a {@link StringBuilder} is used for the concatenation, thus
 * avoiding the direct string concatenations inside the loop. For example, the
 * following code:
 * 
 * <pre>
 * <code>
 *  	List<String> collectionOfStrings = generateStringList(input);
 *		String result = "";
 *		for(String val : collectionOfStrings) {
 *			result = result + val;
 *		}
 *  </code>
 * 
 * is converted to:
 * 
 * <pre>
 * <code>
 *  	List<String> collectionOfStrings = generateStringList(input);
 *		StringBuilder resultSb = new StringBuilder();
 *		for(String val : collectionOfStrings) {
 *			resultSb.append(val);
 *		}
 *		String result = resultSb.toString();
 *  </code>
 * 
 * A new {@link StringBuilder} is introduced just before the occurrence of the
 * loop, and each element of the collection/array is appended to it. Afterwards,
 * the result of the StringBuilder is assigned to the original variable.</li>
 * </ul>
 * 
 * Only the collections/arrays of {@link String}s or {@link Number}s are
 * supported, in both cases.
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
public class StringBuildingLoopASTVisitor extends AbstractEnhancedForLoopToStreamASTVisitor {

	private static final String COLLECTORS_QUALIFIED_NAME = java.util.stream.Collectors.class.getName();
	private static final String ARRAYS_QUALIFIED_NAME = java.util.Arrays.class.getName();
	private static final String COLLECT = "collect"; //$NON-NLS-1$
	private static final String JOINING = "joining"; //$NON-NLS-1$
	private static final String TO_STRING = "toString"; //$NON-NLS-1$
	private static final String APPEND = "append"; //$NON-NLS-1$
	private static final String STRING_BUILDER_CORE_IDENTIFIER = "Sb"; //$NON-NLS-1$

	/**
	 * Stores the identifiers of the {@link StringBuilder}s generated inside one
	 * method.
	 */
	private List<String> generatedIdsPerMethod = new ArrayList<>();

	private String javaVersion;

	public StringBuildingLoopASTVisitor(String javaVersion) {
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
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (continueVisiting) {
			verifyImport(compilationUnit, ARRAYS_QUALIFIED_NAME);
			verifyImport(compilationUnit, COLLECTORS_QUALIFIED_NAME);
		}
		return continueVisiting;
	}

	@Override
	public boolean visit(EnhancedForStatement loopNode) {

		Expression loopExpression = loopNode.getExpression();
		SingleVariableDeclaration loopParameter = loopNode.getParameter();

		if (isGeneratedNode(loopParameter.getType())) {
			return true;
		}

		if (isConditionalExpression(loopExpression)) {
			return true;
		}

		ExpressionStatement singleBodyStatement = getSingleBodyStatement(loopNode).orElse(null);
		if (singleBodyStatement == null) {
			return true;
		}

		SimpleName resultVariable = findResultVariableName(loopParameter, singleBodyStatement).orElse(null);
		if (resultVariable == null) {
			return true;
		}

		if (!ClassRelationUtil.isContentOfTypes(resultVariable.resolveTypeBinding(),
				Collections.singletonList(java.lang.String.class.getName()))) {
			// the result variable has to be a string
			return true;
		}

		if (JavaCore.compareJavaVersions(javaVersion, JavaCore.VERSION_1_8) >= 0) {
			// create the collection statement

			MethodInvocation streamExpression;
			ITypeBinding loopExpressionTypeBinding = loopExpression.resolveTypeBinding();
			if (isCollectionOfStrings(loopExpressionTypeBinding)) {
				// expression.stream()
				streamExpression = createStreamFromStringsCollection(loopExpression);
				concatUsingCollectorsJoining(loopNode, resultVariable, streamExpression);
			} else if (isArrayOfStrings(loopExpressionTypeBinding)) {
				// Arrays.stream(expression)
				streamExpression = createStreamFromArray(loopExpression);
				concatUsingCollectorsJoining(loopNode, resultVariable, streamExpression);
			} else if (isArrayOfNumbers(loopExpressionTypeBinding)) {
				// Arrays.stream(expression)).map(Object::toString)
				streamExpression = createStreamFromNumnbersArray(loopExpression);
				concatUsingCollectorsJoining(loopNode, resultVariable, streamExpression);
			} else if (isCollectionOfNumbers(loopExpressionTypeBinding)) {
				// expression.stream().map(Object::toString)
				streamExpression = createStreamFromNumbersCollection(loopExpression);
				concatUsingCollectorsJoining(loopNode, resultVariable, streamExpression);
			} else {
				/*
				 * the loop expression cannot be converted to a stream, but
				 * using a StringBuilder may still be possible.
				 */
				concatUsingStringBuilder(loopNode, loopParameter, singleBodyStatement, resultVariable);
			}

		} else if (JavaCore.compareJavaVersions(javaVersion, JavaCore.VERSION_1_5) >= 0) {
			concatUsingStringBuilder(loopNode, loopParameter, singleBodyStatement, resultVariable);
		}

		return false;
	}

	/**
	 * Replaces the loop with a stream expression which computes the
	 * concatenation result by invoking {@code collect(Collectors.joining())}.
	 * 
	 * @param loopNode
	 *            the original loop
	 * @param resultVariable
	 *            the variable for storing the result
	 * @param streamExpression
	 *            the expression providing the stream for
	 *            {@code collect(Collectors.joining())}.
	 */
	private void concatUsingCollectorsJoining(EnhancedForStatement loopNode, SimpleName resultVariable,
			MethodInvocation streamExpression) {
		MethodInvocation collect = createCollectInvocation();
		collect.setExpression(streamExpression);
		ASTNode newStatement;
		Optional<VariableDeclarationFragment> optFragment = isReassignable(resultVariable, loopNode);
		if (ASTNode.BLOCK == loopNode.getParent()
			.getNodeType() && optFragment.isPresent()) {
			VariableDeclarationFragment fragment = optFragment.get();
			VariableDeclarationStatement oldDeclStatement = (VariableDeclarationStatement) fragment.getParent();
			newStatement = assignCollectToResult(collect, resultVariable, oldDeclStatement);
			removeOldSumDeclaration(oldDeclStatement, fragment);
		} else {
			newStatement = concatCollectToResult(collect, resultVariable);
		}

		astRewrite.replace(loopNode, newStatement, null);
		getCommentRewriter().saveRelatedComments(loopNode);
		onRewrite();
	}

	/**
	 * Introduces a {@link StringBuilder} to be used for storing the value of
	 * the loop variable instead of concatenation expression.
	 * 
	 * @param loopNode
	 *            the original loop
	 * @param loopParameter
	 *            the loop variable
	 * @param singleBodyStatement
	 *            the single statement in the loop
	 * @param resultVariable
	 *            the variable for storing the result of the concatenation
	 */
	private void concatUsingStringBuilder(EnhancedForStatement loopNode, SingleVariableDeclaration loopParameter,
			ExpressionStatement singleBodyStatement, SimpleName resultVariable) {
		// create the stringBuilder
		ASTNode loopParent = loopNode.getParent();
		Block parentBlock;
		if (ASTNode.BLOCK == loopParent.getNodeType()) {
			/*
			 * If the parent is not a block, there is no room for creating the
			 * StringBuilder.
			 */
			parentBlock = (Block) loopParent;
			String stringBuilderId = generateStringBuilderIdentifier(loopNode, resultVariable.getIdentifier());
			VariableDeclarationStatement sbDeclaration = introduceStringBuilder(stringBuilderId);
			ListRewrite blockRewrite = astRewrite.getListRewrite(parentBlock, Block.STATEMENTS_PROPERTY);
			blockRewrite.insertBefore(sbDeclaration, loopNode, null);
			replaceByStringBuilderAppend(singleBodyStatement, loopParameter.getName(), stringBuilderId);
			Statement expressionStatement;
			Optional<VariableDeclarationFragment> optFragment = isReassignable(resultVariable, loopNode);
			if (optFragment.isPresent()) {
				VariableDeclarationFragment fragment = optFragment.get();
				VariableDeclarationStatement oldDeclStatement = (VariableDeclarationStatement) fragment.getParent();
				expressionStatement = assignStringBuilderToResult(stringBuilderId, resultVariable, oldDeclStatement);
				removeOldSumDeclaration(oldDeclStatement, fragment);
			} else {
				expressionStatement = concatStringBuilderToResult(stringBuilderId, resultVariable);
			}

			blockRewrite.insertAfter(expressionStatement, loopNode, null);
			onRewrite();
		}
	}

	/**
	 * Creates a new variable declaration statement and initializes the declared
	 * variable with the content of the {@link StringBuilder} with the given
	 * identifier. Keeps the modifiers of the old declaration statement.
	 * 
	 * @param stringBuilderId
	 *            the identifier of the {@link StringBuilder}
	 * @param resultVariable
	 *            the name of the variable to create a new declaration for.
	 * @param oldDeclaration
	 *            the old declaration of the variable.
	 * @return the created declaration statement.
	 */
	private VariableDeclarationStatement assignStringBuilderToResult(String stringBuilderId, SimpleName resultVariable,
			VariableDeclarationStatement oldDeclaration) {
		AST ast = astRewrite.getAST();

		MethodInvocation sbToString = ast.newMethodInvocation();
		sbToString.setName(ast.newSimpleName(TO_STRING));
		sbToString.setExpression(ast.newSimpleName(stringBuilderId));

		VariableDeclarationFragment newDeclFragment = ast.newVariableDeclarationFragment();
		newDeclFragment.setName((SimpleName) astRewrite.createCopyTarget(resultVariable));
		newDeclFragment.setInitializer(sbToString);

		VariableDeclarationStatement newDecl = ast.newVariableDeclarationStatement(newDeclFragment);
		ITypeBinding resultType = resultVariable.resolveTypeBinding();
		SimpleType type = ast.newSimpleType(ast.newSimpleName(resultType.getName()));
		newDecl.setType(type);

		/*
		 * Save the modifiers
		 */
		copyModifiers(oldDeclaration, newDecl);

		return newDecl;
	}

	/**
	 * Copies the modifiers and the annotations from the old declaration to the
	 * new declaration statement.
	 * 
	 * @param oldDeclaration
	 *            a declaration statement representing an existing declaration
	 * @param newDecl
	 *            the target declaration statement to put the copied modifiers
	 *            to.
	 */
	private void copyModifiers(VariableDeclarationStatement oldDeclaration, VariableDeclarationStatement newDecl) {
		ListRewrite modifiersRewriter = astRewrite.getListRewrite(newDecl,
				VariableDeclarationStatement.MODIFIERS2_PROPERTY);
		List<Modifier> modifiers = ASTNodeUtil.convertToTypedList(oldDeclaration.modifiers(), Modifier.class);
		modifiers
			.forEach(modifier -> modifiersRewriter.insertLast((Modifier) astRewrite.createCopyTarget(modifier), null));
		List<Annotation> annotations = ASTNodeUtil.convertToTypedList(oldDeclaration.modifiers(), Annotation.class);
		annotations.forEach(
				annotation -> modifiersRewriter.insertLast((Annotation) astRewrite.createCopyTarget(annotation), null));
	}

	/**
	 * Creates a new variable declaration statement and initializes the declared
	 * variable with the given method invocation. Keeps the modifiers of the old
	 * declaration statement.
	 * 
	 * @param collect2
	 *            method invocation to be used as initializer
	 * @param resultVariable
	 *            the name of the variable to create a new declaration for.
	 * @param oldDeclaration
	 *            the old declaration of the variable.
	 * @return the created declaration statement.
	 */
	private VariableDeclarationStatement assignCollectToResult(MethodInvocation collect2, SimpleName resultVariable,
			VariableDeclarationStatement oldDeclaration) {
		AST ast = astRewrite.getAST();

		VariableDeclarationFragment newDeclFragment = ast.newVariableDeclarationFragment();
		newDeclFragment.setName((SimpleName) astRewrite.createCopyTarget(resultVariable));
		newDeclFragment.setInitializer(collect2);

		VariableDeclarationStatement newDecl = ast.newVariableDeclarationStatement(newDeclFragment);
		ITypeBinding resultType = resultVariable.resolveTypeBinding();
		SimpleType type = ast.newSimpleType(ast.newSimpleName(resultType.getName()));
		newDecl.setType(type);

		copyModifiers(oldDeclaration, newDecl);

		return newDecl;
	}

	/**
	 * Checks whether the variable storing the result of the computation
	 * performed by the loop, is declared in the same block with the loop, is
	 * initialized to empty string and is not referenced between its declaration
	 * and the loop occurrence.
	 * 
	 * @param resultVariable
	 *            the name of the variable storing the result of the computation
	 *            of the loop.
	 * @param loopNode
	 *            the loop performing the computation.
	 * @return an optional of the declaration fragment of the variable
	 */
	private Optional<VariableDeclarationFragment> isReassignable(SimpleName resultVariable,
			EnhancedForStatement loopNode) {
		Block block = ASTNodeUtil.getSpecificAncestor(loopNode, Block.class);
		ReassignableResultVisitor analyzer = new ReassignableResultVisitor(block, loopNode, resultVariable);
		block.accept(analyzer);
		return Optional.ofNullable(analyzer.getDeclarationFragment());
	}

	/**
	 * Generates the identifier for the {@link StringBuilder} to be introduced.
	 * Checks for clashing names with the existing variables visible in the
	 * scope and the recently introduced identifiers.
	 * 
	 * @param loopNode
	 *            the node where the string builder will be used.
	 * @param prefix
	 *            the prefix of the string builder identifier.
	 * @return the generated identifier.
	 */
	private String generateStringBuilderIdentifier(EnhancedForStatement loopNode, String prefix) {
		ASTNode scope = ASTNodeUtil.findScope(loopNode);
		VariableDeclarationsVisitor declVisitor = new VariableDeclarationsVisitor();
		scope.accept(declVisitor);
		List<String> declaredIds = declVisitor.getVariableDeclarationNames()
			.stream()
			.map(SimpleName::getIdentifier)
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

	/**
	 * Creates an expression statement which concatenates the result of the
	 * given method invocation with the variable represented by the given name.
	 * 
	 * @param collect
	 *            a method invocation computing the value to be concatenated
	 * @param resultVariable
	 *            the name of the variable to be concatenated
	 * @return an {@link ExpressionStatement} representing the concatenation.
	 */
	private ASTNode concatCollectToResult(MethodInvocation collect, SimpleName resultVariable) {
		AST ast = astRewrite.getAST();
		Assignment assignment = ast.newAssignment();
		assignment.setOperator(Assignment.Operator.PLUS_ASSIGN);
		assignment.setLeftHandSide((SimpleName) astRewrite.createCopyTarget(resultVariable));
		assignment.setRightHandSide(collect);
		return ast.newExpressionStatement(assignment);
	}

	/**
	 * Creates a node representing an invocation of
	 * {@link Arrays#stream(Object[])} and feeds the given expression as a
	 * parameter.
	 * 
	 * @param loopExpression
	 *            a node representing an array
	 * @return an expression of the form {@code Arrays.stream(loopExpression)}.
	 */
	private MethodInvocation createStreamFromArray(Expression loopExpression) {
		AST ast = astRewrite.getAST();
		MethodInvocation stream = ast.newMethodInvocation();
		stream.setName(ast.newSimpleName(STREAM));
		Name arraysTypeName = addImport(ARRAYS_QUALIFIED_NAME);
		stream.setExpression(arraysTypeName);
		ListRewrite argRewriter = astRewrite.getListRewrite(stream, MethodInvocation.ARGUMENTS_PROPERTY);
		argRewriter.insertFirst(loopExpression, null);
		return stream;
	}

	private MethodInvocation createStreamFromNumnbersArray(Expression loopExpression) {
		AST ast = astRewrite.getAST();

		MethodInvocation stream = createStreamFromArray(loopExpression);
		MethodInvocation mapToString = ast.newMethodInvocation();
		mapToString.setName(ast.newSimpleName(MAP));
		mapToString.setExpression(stream);

		ExpressionMethodReference methodReference = ast.newExpressionMethodReference();
		methodReference.setExpression(ast.newSimpleName(Object.class.getSimpleName()));
		methodReference.setName(ast.newSimpleName(TO_STRING));

		ListRewrite argRewriter = astRewrite.getListRewrite(mapToString, MethodInvocation.ARGUMENTS_PROPERTY);
		argRewriter.insertFirst(methodReference, null);

		return mapToString;
	}

	/**
	 * Creates a node representing an invocation of {@link Collection#stream()}
	 * and plugs the given expression to the expression of the method
	 * invocation.
	 * 
	 * @param loopExpression
	 *            a node representing a collection
	 * @return an expression of the form {@code loopExpression.stream()}.
	 */
	private MethodInvocation createStreamFromStringsCollection(Expression loopExpression) {
		Expression collectionExpression = createExpressionForStreamMethodInvocation(loopExpression);
		AST ast = astRewrite.getAST();
		MethodInvocation stream = ast.newMethodInvocation();
		stream.setName(ast.newSimpleName(STREAM));
		stream.setExpression(collectionExpression);
		return stream;
	}

	/**
	 * 
	 * @return a node representing the invocation of
	 *         {@link Collectors#joining()}.
	 */
	private MethodInvocation createCollectInvocation() {
		AST ast = astRewrite.getAST();
		MethodInvocation collect = ast.newMethodInvocation();
		collect.setName(ast.newSimpleName(COLLECT));

		MethodInvocation collectorsJoining = ast.newMethodInvocation();
		collectorsJoining.setName(ast.newSimpleName(JOINING));
		Name colllectorsTypeName = addImport(COLLECTORS_QUALIFIED_NAME);
		collectorsJoining.setExpression(colllectorsTypeName);

		ListRewrite argRewriter = astRewrite.getListRewrite(collect, MethodInvocation.ARGUMENTS_PROPERTY);
		argRewriter.insertFirst(collectorsJoining, null);

		return collect;
	}

	/**
	 * Creates an expression statement which concatenates the contents of the
	 * string builder represented by the given identifier of the with the
	 * variable represented by the given name.
	 * 
	 * @param sbName
	 *            the identifier of the string builder whose content will be
	 *            concatenated
	 * @param resultVariable
	 *            the name of the variable to be concatenated
	 * @return an {@link ExpressionStatement} representing the concatenation.
	 */
	private ExpressionStatement concatStringBuilderToResult(String sbName, SimpleName resultVariable) {
		AST ast = astRewrite.getAST();
		Assignment assignment = ast.newAssignment();
		assignment.setOperator(Assignment.Operator.PLUS_ASSIGN);
		assignment.setLeftHandSide((SimpleName) astRewrite.createCopyTarget(resultVariable));

		MethodInvocation sbToString = ast.newMethodInvocation();
		sbToString.setName(ast.newSimpleName(TO_STRING));
		sbToString.setExpression(ast.newSimpleName(sbName));
		assignment.setRightHandSide(sbToString);

		return ast.newExpressionStatement(assignment);
	}

	/**
	 * Replaces the given expression statement with a new statement of the form
	 * {@code sbName.append(loopParameter)}, which appends the value of the
	 * variable represented by the given name, to the {@link StringBuilder} with
	 * the given identifier.
	 * 
	 * @param singleBodyStatement
	 *            statement to be replaced
	 * @param loopParameter
	 *            the name of the variable to be appended to the
	 *            {@link StringBuilder}
	 * @param sbName
	 *            the identifier of the {@link StringBuilder}
	 */
	private void replaceByStringBuilderAppend(ExpressionStatement singleBodyStatement, SimpleName loopParameter,
			String sbName) {
		AST ast = astRewrite.getAST();
		MethodInvocation append = ast.newMethodInvocation();
		append.setName(ast.newSimpleName(APPEND));
		append.setExpression(ast.newSimpleName(sbName));

		ListRewrite argRewriter = astRewrite.getListRewrite(append, MethodInvocation.ARGUMENTS_PROPERTY);
		argRewriter.insertFirst((SimpleName) astRewrite.createCopyTarget(loopParameter), null);

		ExpressionStatement expressionStatement = ast.newExpressionStatement(append);
		astRewrite.replace(singleBodyStatement, expressionStatement, null);
		getCommentRewriter().saveRelatedComments(singleBodyStatement);
	}

	/**
	 * Creates a {@link VariableDeclarationStatement} for declaring a new
	 * {@link StringBuilder}.
	 * 
	 * @param identifier
	 *            identifier of the {@link StringBuilder} to be introduced.
	 * @return a declaration statement for the new {@link StringBuilder}
	 */
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

	/**
	 * 
	 * @param loopExpression
	 *            a node representing a collection
	 * @return an expression of the form
	 *         {@code [loopExpression].stream().map(Object::toString)}
	 */
	private MethodInvocation createStreamFromNumbersCollection(Expression loopExpression) {
		AST ast = astRewrite.getAST();
		MethodInvocation stream = createStreamFromStringsCollection(loopExpression);
		MethodInvocation mapToString = ast.newMethodInvocation();
		mapToString.setName(ast.newSimpleName(MAP));
		mapToString.setExpression(stream);

		ExpressionMethodReference methodReference = ast.newExpressionMethodReference();
		methodReference.setExpression(ast.newSimpleName(Object.class.getSimpleName()));
		methodReference.setName(ast.newSimpleName(TO_STRING));

		ListRewrite argRewriter = astRewrite.getListRewrite(mapToString, MethodInvocation.ARGUMENTS_PROPERTY);
		argRewriter.insertFirst(methodReference, null);

		return mapToString;
	}

	/**
	 * Checks whether the given {@link ITypeBinding} represents a collection of
	 * {@link Number}s.
	 * 
	 * @param expressionBinding
	 *            type binding to be checked
	 * 
	 * @return {@code true} if the type binding is a collection of
	 *         {@code Number}s or {@code false} otherwise.
	 */
	private boolean isCollectionOfNumbers(ITypeBinding expressionBinding) {
		if (expressionBinding != null && expressionBinding.isParameterizedType()) {
			ITypeBinding[] typeArguments = expressionBinding.getTypeArguments();
			if (typeArguments.length == 1) {
				return ClassRelationUtil.isInheritingContentOfTypes(typeArguments[0],
						Collections.singletonList(Number.class.getName()));
			}
		}
		return false;
	}

	/**
	 * Checks whether the given {@link ITypeBinding} represents a collection of
	 * {@link String}s.
	 * 
	 * @param expressionBinding
	 *            type binding to be checked
	 * 
	 * @return {@code true} if the type binding is a collection of
	 *         {@code String}s or {@code false} otherwise.
	 */
	private boolean isCollectionOfStrings(ITypeBinding expressionBinding) {
		if (expressionBinding != null && expressionBinding.isParameterizedType()) {
			ITypeBinding[] typeArguments = expressionBinding.getTypeArguments();
			if (typeArguments.length == 1) {
				return ClassRelationUtil.isContentOfTypes(typeArguments[0],
						Collections.singletonList(String.class.getName()));
			}
		}
		return false;
	}

	/**
	 * Checks whether the given {@link ITypeBinding} represents an array of
	 * {@link String}s.
	 * 
	 * @param expressionBinding
	 *            type binding to be checked
	 * 
	 * @return {@code true} if the type binding is an array of {@code String}s
	 *         or {@code false} otherwise.
	 */
	private boolean isArrayOfStrings(ITypeBinding expressionBinding) {
		if (expressionBinding != null && expressionBinding.isArray()) {
			ITypeBinding componentType = expressionBinding.getComponentType();
			return ClassRelationUtil.isContentOfTypes(componentType, Collections.singletonList(String.class.getName()));
		}
		return false;
	}

	/**
	 * Checks whether the given {@link ITypeBinding} represents an array of
	 * {@link Number}s.
	 * 
	 * @param expressionBinding
	 *            type binding to be checked
	 * 
	 * @return {@code true} if the type binding is an array of {@code Number}s
	 *         or {@code false} otherwise.
	 */
	private boolean isArrayOfNumbers(ITypeBinding expressionBinding) {
		if (expressionBinding != null && expressionBinding.isArray()) {
			ITypeBinding componentType = expressionBinding.getComponentType();
			return ClassRelationUtil.isInheritingContentOfTypes(componentType,
					Collections.singletonList(Number.class.getName()));
		}
		return false;
	}

	/**
	 * A visitor for checking whether a variable storing the result of the
	 * concatenation is declared in the same block as the loop, is initialized
	 * to empty string and is not referenced between its declaration and the
	 * loop occurrence.
	 * 
	 * @author Ardit Ymeri
	 * @since 2.1.1
	 *
	 */
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
			if (simpleName.getIdentifier()
				.equals(this.resultName.getIdentifier())
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
			if (fragmentName.getIdentifier()
				.equals(resultName.getIdentifier())) {
				Expression initializer = fragment.getInitializer();
				if (ASTNode.STRING_LITERAL == initializer.getNodeType()) {
					StringLiteral stringLiteral = (StringLiteral) initializer;
					if (StringUtils.isEmpty(stringLiteral.getLiteralValue())) {
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
			if (this.loop == loop) {
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
