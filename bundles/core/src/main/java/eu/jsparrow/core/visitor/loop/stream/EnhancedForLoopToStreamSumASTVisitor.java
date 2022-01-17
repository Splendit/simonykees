package eu.jsparrow.core.visitor.loop.stream;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;

/**
 * Analyzes the occurrences of {@link EnhancedForStatement}s and checks whether
 * they are used only for summing up the elements of a collection. For example,
 * the following code:
 * 
 * <pre>
 * <code>{@code
 * List<Integer> numbers = generateIntList(input);}
 * int sum = 0;
 * for(int n : numbers) {
 * 	sum += n;
 * }
 * </code>
 * </pre>
 * 
 * will be converted to:
 * 
 * <pre>
 * <code>{@code
 * List<Integer> numbers = generateIntList(input);
 * int sum = numbers.stream().mapToInt(Integer::intValue).sum();
 * }</code>
 * </pre>
 * 
 * Considers only the cases where the loop variable is either an {@code int},
 * {@code double}, {@code long} or the corresponding boxed type.
 * 
 * @author Ardit Ymeri
 * @since 2.1.1
 *
 */
public class EnhancedForLoopToStreamSumASTVisitor extends AbstractEnhancedForLoopToStreamASTVisitor {

	private static final String JAVA_LANG_DOUBLE = java.lang.Double.class.getName();
	private static final String JAVA_LANG_INTEGER = java.lang.Integer.class.getName();
	private static final String JAVA_LANG_LONG = java.lang.Long.class.getName();
	private static final String JAVA_LANG_SHORT = java.lang.Short.class.getName();
	private static final String JAVA_LANG_FLOAT = java.lang.Float.class.getName();
	private static final String JAVA_LANG_BYTE = java.lang.Byte.class.getName();
	private static final String DOUBLE_VALUE = "doubleValue"; //$NON-NLS-1$
	private static final String INT_VALUE = "intValue"; //$NON-NLS-1$
	private static final String LONG_VALUE = "longValue"; //$NON-NLS-1$
	private static final String SUM = "sum"; //$NON-NLS-1$
	private static final String ZERO_TOKEN = "0"; //$NON-NLS-1$
	private static final String ZERO_LONG_TOKEN = "0L"; //$NON-NLS-1$
	private static final String ZERO_DOUBLE_TOKEN = "0D"; //$NON-NLS-1$

	@Override
	public boolean visit(EnhancedForStatement loopNode) {

		/*
		 * The loop expression bust be a collection
		 */
		Expression expression = loopNode.getExpression();
		if (!isCollection(expression) || isConditionalExpression(expression)) {
			return true;
		}
		
		SingleVariableDeclaration loopParameter = loopNode.getParameter();
		if(isGeneratedNode(loopParameter.getType())) {
			return true;
		}

		/*
		 * The body of the loop must consist of a single statement
		 */
		ExpressionStatement expressionStatement = getSingleBodyStatement(loopNode).orElse(null);
		if (expressionStatement == null) {
			return true;
		}

		/*
		 * The expression statement must be an addition operation of the loop
		 * variable and a variable for keeping the result.
		 */
		SimpleName sumVariableName = findResultVariableName(loopParameter, expressionStatement).orElse(null);
		if (sumVariableName == null) {
			return true;
		}

		/*
		 * Generate the mapping method invocation i.e. either mapToInt,
		 * mapToDouble or mapToLong
		 */
		MethodInvocation mapToStreamInvocation = findCorrespondingNumberStream(expression, sumVariableName)
			.orElse(null);
		if (mapToStreamInvocation == null) {
			return true;
		}

		/*
		 * Find the declaration of the sum variable, check if it is initialized
		 * to zero and if it is not referenced between its declaration and the
		 * loop occurrence
		 */
		VariableDeclarationFragment sumDeclarationFragment = findSumVariableDeclaration(sumVariableName, loopNode)
			.orElse(null);
		if (sumDeclarationFragment == null) {
			return true;
		}

		VariableDeclarationStatement sumDeclStatement;
		ASTNode fragmentParent = sumDeclarationFragment.getParent();
		if (ASTNode.VARIABLE_DECLARATION_STATEMENT == fragmentParent.getNodeType()) {
			sumDeclStatement = (VariableDeclarationStatement) fragmentParent;
		} else {
			return true;
		}

		/*
		 * Create the stream expression for computing the sum
		 */
		MethodInvocation streamSumInvocation = createStreamSumInvocation(expression, mapToStreamInvocation);

		/*
		 * Replace the loop with the new stream expression
		 */
		VariableDeclarationStatement newSumVariableDeclaration = createNewSumDeclaration(sumDeclStatement,
				sumDeclarationFragment, streamSumInvocation);

		astRewrite.replace(loopNode, newSumVariableDeclaration, null);
		getCommentRewriter().saveRelatedComments(loopNode);
		addMarkerEvent(loopNode);
		onRewrite();

		removeOldSumDeclaration(sumDeclStatement, sumDeclarationFragment);

		return true;
	}

	/**
	 * Creates a variable declaration statement having only one fragment
	 * declaring the same variable as the one declared in the given fragment,
	 * but using the given method invocation as an initializer.
	 * 
	 * @param fragment
	 *            the original fragment declaring the original variable
	 * @param streamSumInvocation
	 *            the new initializer to be used on the new fragment
	 * @return a new node representing the new
	 *         {@link VariableDeclarationStatement}
	 */
	private VariableDeclarationStatement createNewSumDeclaration(VariableDeclarationStatement oldDeclStatement,
			VariableDeclarationFragment fragment, MethodInvocation streamSumInvocation) {

		Type oldType = oldDeclStatement.getType();
		SimpleName oldName = fragment.getName();

		AST ast = fragment.getAST();
		VariableDeclarationFragment newFragment = ast.newVariableDeclarationFragment();
		newFragment.setName(ast.newSimpleName(oldName.getIdentifier()));
		newFragment.setInitializer(streamSumInvocation);

		VariableDeclarationStatement newDeclStatement = ast.newVariableDeclarationStatement(newFragment);
		newDeclStatement.setType((Type) astRewrite.createCopyTarget(oldType));

		return newDeclStatement;
	}

	/**
	 * Creates a method invocation of the form:
	 * 
	 * <pre>
	 * <code>
	 * 		[expression].stream().[mapToStreamInvocation].sum()
	 * </code>
	 * </pre>
	 * 
	 * @param expression
	 *            a node representing a collection to create the stream from
	 * @param mapToStreamInvocation
	 *            a new node representing a method invocation to be placed right
	 *            next to stream() invocation.
	 * @return a new node representing the constructed method invocation.
	 */
	private MethodInvocation createStreamSumInvocation(Expression expression, MethodInvocation mapToStreamInvocation) {
		AST ast = expression.getAST();
		MethodInvocation stream = ast.newMethodInvocation();
		stream.setName(ast.newSimpleName(STREAM));
		stream.setExpression(createExpressionForStreamMethodInvocation(expression));
		mapToStreamInvocation.setExpression(stream);

		MethodInvocation sum = ast.newMethodInvocation();
		sum.setName(ast.newSimpleName(SUM));
		sum.setExpression(mapToStreamInvocation);

		return sum;
	}

	/**
	 * Makes use of {@link SumVariableDeclarationVisitor} for finding the
	 * declaration fragment of the variable with the given name. Furthermore,
	 * checks whether the variable is initialized to zero and is NOT referenced
	 * between its declaration and the loop occurrence.
	 * 
	 * @param variableName
	 *            name of the variable to check for
	 * @param loopNode
	 *            loop computing the the sum value
	 * @return an optional of the declaration fragment if one is found which
	 *         fulfills the described conditions, or an empty optional
	 *         otherwise.
	 */
	private Optional<VariableDeclarationFragment> findSumVariableDeclaration(SimpleName variableName,
			EnhancedForStatement loopNode) {
		Block block = ASTNodeUtil.getSpecificAncestor(loopNode, Block.class);
		if (block == null) {
			return Optional.empty();
		}

		SumVariableDeclarationVisitor visitor = new SumVariableDeclarationVisitor(variableName, loopNode, block);
		block.accept(visitor);

		return visitor.getSumVariableDeclaration();
	}

	/**
	 * Creates a method invocation for mapping a stream to a either of the
	 * predefined streams: {@link IntStream}, {@link DoubleStream} or
	 * {@link LongStream}. Uses an {@link ExpressionMethodReference} as the only
	 * parameter of the method.
	 * <p>
	 * The name of the {@link MethodInvocation} method and the name of the
	 * {@link ExpressionMethodReference} depend on the type of the sum variable
	 * (representing the variable storing the sum), whereas the expression of
	 * the {@link ExpressionMethodReference} depends on the type of the
	 * expression (representing a collection).
	 * 
	 * @param expression
	 *            a node representing a collection
	 * @param sumVarName
	 *            a node representing the variable storing the sum
	 * @return an optional o the method invocation if the collection can be
	 *         converted to any of the aforementioned streams, or an empty
	 *         collection otherwise.
	 */
	private Optional<MethodInvocation> findCorrespondingNumberStream(Expression expression, SimpleName sumVarName) {
		ITypeBinding expressionType = expression.resolveTypeBinding();
		if (expressionType.isParameterizedType()) {
			ITypeBinding[] typeArguments = expressionType.getTypeArguments();
			if (typeArguments.length == 1) {
				ITypeBinding argumentType = typeArguments[0];

				String argumentTypeName = argumentType.getQualifiedName();

				String mapMethodname;
				String methodRefName;
				String boxedType = ClassRelationUtil.findBoxedTypeOfPrimitive(sumVarName.resolveTypeBinding());

				switch (boxedType) {
				case "Integer": //$NON-NLS-1$
					mapMethodname = MAP_TO_INT;
					methodRefName = INT_VALUE;
					break;
				case "Double": //$NON-NLS-1$
					mapMethodname = MAP_TO_DOUBLE;
					methodRefName = DOUBLE_VALUE;
					break;
				case "Long": //$NON-NLS-1$
					mapMethodname = MAP_TO_LONG;
					methodRefName = LONG_VALUE;
					break;
				default:
					return Optional.empty();
				}

				String methodRefExpression;
				if (JAVA_LANG_DOUBLE.equals(argumentTypeName)) {
					methodRefExpression = Double.class.getSimpleName();
				} else if (JAVA_LANG_INTEGER.equals(argumentTypeName)) {
					methodRefExpression = Integer.class.getSimpleName();
				} else if (JAVA_LANG_LONG.equals(argumentTypeName)) {
					methodRefExpression = Long.class.getSimpleName();
				} else if (JAVA_LANG_SHORT.equals(argumentTypeName)) {
					methodRefExpression = Short.class.getSimpleName();
				} else if (JAVA_LANG_FLOAT.equals(argumentTypeName)) {
					methodRefExpression = Float.class.getSimpleName();
				} else if (JAVA_LANG_BYTE.equals(argumentTypeName)) {
					methodRefExpression = Byte.class.getSimpleName();
				} else {
					return Optional.empty();
				}

				return Optional.of(createNumberStreamMapInvocation(mapMethodname, methodRefExpression, methodRefName));
			}
		}

		return Optional.empty();
	}

	/**
	 * Creates a new {@link MethodInvocation} having one parameter represented
	 * as an {@link ExpressionMethodReference}.
	 * 
	 * @param mapMethodName
	 *            name of the method
	 * @param methodReferenceExpression
	 *            expression of the method reference
	 * @param methodReferenceName
	 *            name of the method reference
	 * @return the constructed method invocation
	 */
	private MethodInvocation createNumberStreamMapInvocation(String mapMethodName, String methodReferenceExpression,
			String methodReferenceName) {
		AST ast = astRewrite.getAST();
		MethodInvocation methodInvocation = ast.newMethodInvocation();
		methodInvocation.setName(ast.newSimpleName(mapMethodName));

		ExpressionMethodReference methodReference = ast.newExpressionMethodReference();
		methodReference.setName(ast.newSimpleName(methodReferenceName));
		methodReference.setExpression(ast.newSimpleName(methodReferenceExpression));

		ListRewrite listRewrite = astRewrite.getListRewrite(methodInvocation, MethodInvocation.ARGUMENTS_PROPERTY);
		listRewrite.insertFirst(methodReference, null);

		return methodInvocation;
	}

	/**
	 * Checks whether the given expression represents a {@link Collection}
	 * 
	 * @param expression
	 *            expression to be checked
	 * @return {@code true} if the expression is a collection or {@code false}
	 *         otherwise.
	 */
	private boolean isCollection(Expression expression) {
		ITypeBinding expressionBinding = expression.resolveTypeBinding();
		List<String> expressionBindingList = Collections.singletonList(java.util.Collection.class.getName());

		return expressionBinding != null
				&& (ClassRelationUtil.isInheritingContentOfTypes(expressionBinding, expressionBindingList)
						|| ClassRelationUtil.isContentOfTypes(expressionBinding, expressionBindingList));
	}

	/**
	 * A helper visitor for finding the declaration fragment of the sum variable
	 * and checking if it is initialized to zero and not updated before the loop
	 * occurrence.
	 * 
	 * @author Ardit Ymeri
	 * @since 2.1.1
	 *
	 */
	private class SumVariableDeclarationVisitor extends ASTVisitor {

		private SimpleName variableName;
		private EnhancedForStatement loopNode;
		private VariableDeclarationFragment declarationFragment;
		private Block block;

		private boolean beforeLoop = true;

		private boolean missingZeroInitialization = false;
		private boolean referencedBeforeLoop = false;

		public SumVariableDeclarationVisitor(SimpleName variableName, EnhancedForStatement loopNode, Block block) {
			this.variableName = variableName;
			this.loopNode = loopNode;
			this.block = block;
		}

		@Override
		public boolean preVisit2(ASTNode node) {
			return beforeLoop && !missingZeroInitialization && !referencedBeforeLoop;
		}

		@Override
		public boolean visit(VariableDeclarationFragment fragment) {
			if (fragment.getName()
				.getIdentifier()
				.equals(variableName.getIdentifier())) {
				Expression initializer = fragment.getInitializer();
				if (initializer != null) {
					if (ASTNode.NUMBER_LITERAL == initializer.getNodeType()) {
						NumberLiteral numberLiteral = (NumberLiteral) initializer;

						String token = numberLiteral.getToken();

						if (isZero(token, fragment.getName())) {
							this.declarationFragment = fragment;
						} else {
							missingZeroInitialization = true;
						}
					} else {
						missingZeroInitialization = true;
					}
				} else {
					missingZeroInitialization = true;
				}

			}
			return true;
		}

		private boolean isZero(String token, SimpleName name) {
			if (ZERO_TOKEN.equals(token) || ZERO_LONG_TOKEN.equals(token) || ZERO_DOUBLE_TOKEN.equals(token)) {
				return true;
			}

			ITypeBinding nameTypeBinding = name.resolveTypeBinding();
			if (JAVA_LANG_INTEGER.equals(nameTypeBinding.getQualifiedName())) {
				int intValue = Integer.parseInt(token);
				return intValue == 0;
			} else if (JAVA_LANG_DOUBLE.equals(nameTypeBinding.getQualifiedName())) {
				Double doubleValue = Double.parseDouble(token);
				return doubleValue == 0;
			} else if (JAVA_LANG_LONG.equals(nameTypeBinding.getQualifiedName())) {
				Long doubleValue = Long.parseLong(token);
				return doubleValue == 0;
			}
			return false;
		}

		@Override
		public boolean visit(SimpleName simpleName) {
			if (simpleName.getIdentifier()
				.equals(this.variableName.getIdentifier())
					&& VariableDeclarationFragment.NAME_PROPERTY != simpleName.getLocationInParent()) {
				IBinding binding = simpleName.resolveBinding();
				StructuralPropertyDescriptor propertyDescriptor = simpleName.getLocationInParent();
				if (IBinding.VARIABLE == binding.getKind() && FieldAccess.NAME_PROPERTY != propertyDescriptor
						&& QualifiedName.NAME_PROPERTY != propertyDescriptor) {
					clearParameters();
				}
			}

			return true;
		}

		private void clearParameters() {
			this.declarationFragment = null;
			this.referencedBeforeLoop = true;
		}

		@Override
		public boolean visit(Block block) {
			if (this.block == block) {
				return true;
			}
			return !beforeLoop;
		}

		@Override
		public boolean visit(EnhancedForStatement loopNode) {
			if (this.loopNode == loopNode) {
				this.beforeLoop = false;
			}
			return false;
		}

		public Optional<VariableDeclarationFragment> getSumVariableDeclaration() {
			return Optional.ofNullable(declarationFragment);
		}
	}
}
