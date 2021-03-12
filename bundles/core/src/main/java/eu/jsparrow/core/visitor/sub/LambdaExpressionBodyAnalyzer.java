package eu.jsparrow.core.visitor.sub;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;

/**
 * A helper class for analyzing the block body of a lambda expression.
 * 
 * @author Ardit Ymeri
 * @since 1.2
 *
 */
public class LambdaExpressionBodyAnalyzer {

	private static final String MAP = "map"; //$NON-NLS-1$
	private static final String MAP_TO_INT = "mapToInt"; //$NON-NLS-1$
	private static final String MAP_TO_LONG = "mapToLong"; //$NON-NLS-1$
	private static final String MAP_TO_DOUBLE = "mapToDouble"; //$NON-NLS-1$

	private ASTNode extractableBlock;
	private ASTNode remainingBlock;
	private List<SimpleName> declaredNames = new ArrayList<>();
	private List<Statement> extractableStatements = new ArrayList<>();
	private List<Statement> remainingStatements = new ArrayList<>();
	private Expression mapExpression = null;
	private SimpleName newForEachVarName = null;
	private Type parameterType = null;
	private Modifier modifier;
	private boolean primitiveTarget = false;
	private String mappingMethodName = MAP;
	private VariableDeclarationStatement mapVariableDeclaration;
	private ExpressionStatement replacedRemainingStatement;

	public LambdaExpressionBodyAnalyzer(SimpleName parameter, Block block, ASTRewrite astRewrite) {
		List<Statement> statements = ASTNodeUtil.returnTypedList(block.statements(), Statement.class);
		boolean mapVariableFound = false;

		for (Statement statement : statements) {

			if (!mapVariableFound) {

				// search for a map variable
				if (ASTNode.VARIABLE_DECLARATION_STATEMENT == statement.getNodeType()) {
					/*
					 * only variable declaration statements can introduce a map
					 * variable
					 */
					VariableDeclarationStatement declStatement = (VariableDeclarationStatement) statement;

					List<VariableDeclarationFragment> fragments = ASTNodeUtil
						.convertToTypedList(declStatement.fragments(), VariableDeclarationFragment.class);
					Type type = declStatement.getType();

					if (ASTNodeUtil.convertToTypedList(declStatement.modifiers(), Annotation.class)
						.isEmpty() && !involvesUndefinedTypes(type.resolveBinding())
							&& !declStatement.getType()
								.isArrayType()
							&& referencesName(declStatement, parameter) && fragments.size() == 1) {
						/*
						 * a map variable is found. store its name and its
						 * initializer
						 */
						VariableDeclarationFragment fragment = fragments.get(0);
						SimpleName fragmentName = fragment.getName();
						Expression initializer = fragment.getInitializer();
						/*
						 * FIXME: SIM-767
						 */
						if (!isDerivableInitializerType(initializer)) {
							storeMethodNameForPrimitiveType(type);
							mapVariableFound = true;
							newForEachVarName = fragmentName;
							parameterType = declStatement.getType();
							mapExpression = initializer;
							this.mapVariableDeclaration = declStatement;
							storeModifier(declStatement);
						} else {
							storeDeclaredName(statement, fragments);
						}
					} else {
						/*
						 * store the declared name. It will be checked for
						 * references after the map variable is found.
						 */
						storeDeclaredName(statement, fragments);
					}
				} else {
					ReturnStatementVisitor visitor = new ReturnStatementVisitor();
					statement.accept(visitor);
					if (visitor.hasReturnStatement()) {
						clearParameters();
						return;
					}
					extractableStatements.add(statement);
				}
			} else {
				if (referencesName(statement, parameter) || referencesNames(statement, declaredNames)) {
					clearParameters();
					return;
				}
				remainingStatements.add((Statement) statement);
			}
		}

		prepareRemainingBlock(astRewrite);
		prepareExtractableBlock(astRewrite);
	}

	/**
	 * Checks if the given type is a type variable or involves a type variable
	 * as a parameter.
	 * 
	 * @param type
	 *            a type to be checked
	 * 
	 * @return {@code true} if the type involves a type variable, or
	 *         {@code false} otherwise.
	 */
	private boolean involvesUndefinedTypes(ITypeBinding type) {

		if (type.isParameterizedType()) {
			ITypeBinding[] arguments = type.getTypeArguments();
			for (ITypeBinding argument : arguments) {
				if (argument.isParameterizedType()) {
					// recursive call
					return involvesUndefinedTypes(argument);
				}

				ITypeBinding typeDeclaration = argument.getTypeDeclaration();
				if (typeDeclaration.isTypeVariable()) {
					return true;
				}

				if (argument.isRawType() || argument.isWildcardType() || argument.isCapture()) {
					return true;
				}
			}
		} else if (type.isTypeVariable() || type.isRawType() || type.isWildcardType() || type.isCapture()) {
			return true;
		}

		return false;
	}

	private void storeMethodNameForPrimitiveType(Type type) {
		ITypeBinding mappingVarBinding = type.resolveBinding();
		if (mappingVarBinding != null && mappingVarBinding.isPrimitive()) {
			this.primitiveTarget = true;
			this.mappingMethodName = calcMappingMethodName(mappingVarBinding);

		}
	}

	private void storeModifier(VariableDeclarationStatement declStatement) {
		List<Modifier> modifiers = ASTNodeUtil.convertToTypedList(declStatement.modifiers(), Modifier.class);
		if (modifiers.size() == 1) {
			this.modifier = modifiers.get(0);
		}
	}

	/**
	 * Store the statement and the name of the declaration fragment so that the
	 * it is possible to check for references in the rest of the statements of
	 * the body.
	 * 
	 * @param statement
	 *            the whole statement representing a variable declaration.
	 * @param fragments
	 *            fragments of the declaration statement.
	 */
	private void storeDeclaredName(Statement statement, List<VariableDeclarationFragment> fragments) {
		extractableStatements.add(statement);
		fragments.stream()
			.map(VariableDeclarationFragment::getName)
			.forEach(declaredNames::add);
	}

	/**
	 * Checks if the given type binding corresponds to either of the primitives:
	 * {@code int}, {@code long} or {@code double}, and if yes returns the
	 * corresponding method name which returns the respective stream type.
	 * 
	 * @param initializerBinding
	 *            type binding of the resulting stream type.
	 * 
	 * @return {@value #MAP} if the given type is not any of the aforementioned
	 *         types, or any of the following: {@value #MAP_TO_INT},
	 *         {@value #MAP_TO_DOUBLE} or {@value #MAP_TO_LONG} respectively for
	 *         {@code int}, {@code double} or {@code long} primitives.
	 */
	private String calcMappingMethodName(ITypeBinding initializerBinding) {
		if (initializerBinding.isPrimitive()) {
			String typeName = initializerBinding.getQualifiedName();
			switch (typeName) {
			case "int": //$NON-NLS-1$
				return MAP_TO_INT;
			case "double": //$NON-NLS-1$
				return MAP_TO_DOUBLE;
			case "long": //$NON-NLS-1$
				return MAP_TO_LONG;
			default:
				return MAP;
			}
		}
		return MAP;
	}

	/**
	 * Checks if the expression is a generic method invocation without type
	 * arguments.
	 * 
	 * @param expression
	 *            expression to be checked
	 * @return {@code true} if expression is a method invocation without type
	 *         arguments, {@code false} otherwise.
	 */
	private boolean isDerivableInitializerType(Expression expression) {
		if (expression.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation methodInvocation = (MethodInvocation) expression;
			IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
			if (methodBinding == null) {
				return false;
			}
			if (methodBinding.isParameterizedMethod() && methodInvocation.typeArguments()
				.isEmpty()) {
				return true;
			}
		}

		return false;
	}

	public Modifier getNewForEachParameterModifier() {
		return this.modifier;
	}

	private void clearParameters() {
		extractableBlock = null;
		remainingBlock = null;
		newForEachVarName = null;
	}

	public SimpleName getNewForEachParameterName() {
		return newForEachVarName;
	}

	public Type getNewForEachParameterType() {
		return this.parameterType;
	}

	/**
	 * An indicator for showing whether all parameters are found for extracting
	 * a part of the {@code Stream::forEach} to a {@code Stream::map}.
	 * 
	 * @return
	 */
	public boolean foundExtractableMapStatement() {
		return extractableBlock != null && remainingBlock != null && newForEachVarName != null
				&& mapExpression != null;
	}

	/**
	 * Creates the body of the lambda expression to be used in the introduced
	 * {@code Stream::map}
	 * 
	 * @param ast
	 *            either a {@link Block} or a {@link Expression}
	 */
	private void prepareExtractableBlock(ASTRewrite astRewrite) {
		if (!this.extractableStatements.isEmpty() && mapExpression != null) {
			AST ast = astRewrite.getAST();
			Block block = ast.newBlock();
			ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);

			this.extractableStatements.forEach(
					statement -> listRewrite.insertLast((Statement) astRewrite.createCopyTarget(statement), null));

			ReturnStatement returnStatement = ast.newReturnStatement();
			returnStatement.setExpression((Expression) astRewrite.createCopyTarget(mapExpression));
			listRewrite.insertLast(returnStatement, null);

			this.extractableBlock = block;
		} else if (mapExpression != null) {
			this.extractableBlock = astRewrite.createCopyTarget(mapExpression);
		}
	}

	/**
	 * Creates a the body of the lambda expression in the forEach after
	 * extracting the part to be placed in {@code Stream::map}.
	 * 
	 * @param ast
	 *            either a {@link Block} or a single {@link Expression} if the
	 *            remaining block has only one expression.
	 */
	private void prepareRemainingBlock(ASTRewrite astRewrite) {
		ASTNode block;
		if (this.remainingStatements.size() == 1 && ASTNode.EXPRESSION_STATEMENT == remainingStatements.get(0)
			.getNodeType()) {
			ExpressionStatement remainingStm = (ExpressionStatement) remainingStatements.get(0);
			Expression expression = remainingStm.getExpression();
			block = astRewrite.createCopyTarget(expression);
			replacedRemainingStatement = remainingStm;

		} else {
			block = astRewrite.getAST()
				.newBlock();
			ListRewrite listRewrite = astRewrite.getListRewrite(block, Block.STATEMENTS_PROPERTY);
			this.remainingStatements
				.forEach(statement -> listRewrite.insertLast(astRewrite.createCopyTarget(statement), null));
		}

		this.remainingBlock = block;
	}

	public Expression getMapExpression() {
		return this.mapExpression;
	}

	public VariableDeclarationStatement getMapVariableDeclaration() {
		return this.mapVariableDeclaration;
	}

	public List<Statement> getRemainingStatements() {
		return this.remainingStatements;
	}

	public ASTNode getExtractableBlock() {
		return this.extractableBlock;
	}

	public ASTNode getRemainingBlock() {
		return this.remainingBlock;
	}

	public ExpressionStatement getReplacedRemainingStatement() {
		return this.replacedRemainingStatement;
	}

	private boolean referencesNames(Statement statement, List<SimpleName> declaredNames2) {
		return declaredNames2.stream()
			.anyMatch(simpleName -> referencesName(statement, simpleName));
	}

	/**
	 * Checks whether there is a reference of the variable with the given simple
	 * name in the code represented by the given ast node.
	 * 
	 * @param node
	 *            node to look for
	 * @param simpleName
	 *            name of the variable to look for
	 * 
	 * @return {@code true} if there is a reference of the given simple name in
	 *         the node, and {@code false otherwise}
	 */
	private boolean referencesName(ASTNode node, SimpleName simpleName) {
		LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(simpleName);
		node.accept(visitor);
		return !visitor.getUsages()
			.isEmpty();
	}

	public boolean isPrimitiveTarget() {
		return this.primitiveTarget;
	}

	public String getMappingMethodName() {
		return mappingMethodName;
	}

	/**
	 * A visitor searching for return statements.
	 *
	 */
	class ReturnStatementVisitor extends ASTVisitor {
		ReturnStatement returnStatement = null;

		@Override
		public boolean visit(ReturnStatement returnStatement) {
			this.returnStatement = returnStatement;
			return true;
		}

		@Override
		public boolean preVisit2(ASTNode node) {
			// stop searching if a return statement is already found
			return returnStatement == null;
		}

		public boolean hasReturnStatement() {
			return this.returnStatement != null;
		}
	}
}