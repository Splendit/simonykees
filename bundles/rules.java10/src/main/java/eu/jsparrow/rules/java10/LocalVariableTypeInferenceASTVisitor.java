package eu.jsparrow.rules.java10;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;
import eu.jsparrow.rules.common.visitor.helper.VariableAssignmentVisitor;

/**
 * A visitor for replacing the types of local variable declarations with
 * {@value #VAR_KEY_WORD}. Covers also the declarations of the parameters on
 * {@link EnhancedForStatement}s. For example, the following lines:
 * <p/>
 * 
 * <pre>
 * <code>
 * Order order = new Order("product");
 * for (User user : userList) {
 * 	consume(user);
 * }
 * </code>
 * </pre>
 * 
 * will be transformed to:
 * 
 * <pre>
 * <code>
 * var order = new Order("product");
 * for (var user : userList) {
 * 	consume(user);
 * }
 * </code>
 * </pre>
 * 
 * @since 2.6.0
 */
public class LocalVariableTypeInferenceASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String VAR_KEY_WORD = "var"; //$NON-NLS-1$

	@Override
	public boolean visit(EnhancedForStatement enhancedForStatement) {
		SingleVariableDeclaration parameter = enhancedForStatement.getParameter();
		Type type = parameter.getType();
		SimpleName name = parameter.getName();
		Expression loopExpression = enhancedForStatement.getExpression();

		if(isGeneratedNode(parameter.getType())) {			
			return true;
		}

		if (type.isVar()) {
			return true;
		}

		boolean satisfiedPrecondition = verifyLoopPrecondition(loopExpression, name);
		if (!satisfiedPrecondition) {
			return true;
		}

		replaceWithVarType(type);
		List<Dimension> dimensions = ASTNodeUtil.convertToTypedList(parameter.extraDimensions(), Dimension.class);
		removeArrayDimensions(dimensions);
		getCommentRewriter().saveCommentsInParentStatement(type);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		Expression initializer = node.getInitializer();
		SimpleName name = node.getName();

		Type type = findType(node);
		if (type == null || type.isVar()) {
			return false;
		}
		
		if(isGeneratedNode(type)) {
			return true;
		}

		boolean satisfiedPrecondition = verifyDeclarationPrecondition(initializer, name);
		if (!satisfiedPrecondition) {
			return true;
		}

		removeArrayDimensions(node, type);
		checkSpaces(type, name);
		replaceWithVarType(type);
		getCommentRewriter().saveCommentsInParentStatement(type);
		return true;
	}

	private Type findType(VariableDeclarationFragment node) {
		Type type = null;
		ASTNode parent = node.getParent();
		if (parent.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			VariableDeclarationStatement statement = (VariableDeclarationStatement) parent;
			if (statement.fragments()
				.size() == 1) {
				type = statement.getType();
			}
		}

		if (parent.getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION) {
			VariableDeclarationExpression declarationExpression = ((VariableDeclarationExpression) parent);
			if (declarationExpression.fragments()
				.size() == 1) {
				type = declarationExpression.getType();
			}
		}
		return type;
	}

	private void checkSpaces(Type type, SimpleName name) {
		int typeEndingPosition = type.getStartPosition() + type.getLength();
		int nameStartingPosition = name.getStartPosition();
		if (nameStartingPosition != typeEndingPosition) {
			return;
		}

		/*
		 * There is no space between the type declaration and the variable name.
		 * E.g. List<String>valuses = ...
		 */
		ASTNode parent = type.getParent();
		if (parent.getNodeType() != ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			return;
		}

		/*
		 * A work around to avoid converting List<String>values=... to
		 * varvalues=...
		 */
		VariableDeclarationStatement statement = (VariableDeclarationStatement) parent;
		VariableDeclarationStatement newStatement = (VariableDeclarationStatement) ASTNode.copySubtree(type.getAST(),
				statement);
		AST ast = newStatement.getAST();
		newStatement.setType(ast.newSimpleType(ast.newSimpleName(VAR_KEY_WORD)));
		astRewrite.replace(parent, newStatement, null);
	}

	private boolean verifyDeclarationPrecondition(Expression initializer, SimpleName variableName) {
		if (initializer == null) {
			return false;
		}

		int initializerNodeType = initializer.getNodeType();
		if (ASTNode.LAMBDA_EXPRESSION == initializerNodeType || ASTNode.ARRAY_INITIALIZER == initializerNodeType
				|| initializer instanceof MethodReference) {
			return false;
		}

		if (ASTNode.CONDITIONAL_EXPRESSION == initializer.getNodeType()) {
			ConditionalExpression conditionalExpression = (ConditionalExpression) initializer;
			Expression thenExpression = conditionalExpression.getThenExpression();
			Expression elseExpression = conditionalExpression.getElseExpression();
			return verifyDeclarationPrecondition(thenExpression, variableName)
					&& verifyDeclarationPrecondition(elseExpression, variableName);
		}

		ITypeBinding initializerType = initializer.resolveTypeBinding();
		if (initializerType == null || containsWildCard(initializerType)
				|| ASTNodeUtil.isGenericMethodInvocation(initializer) || initializerType.isNullType()) {
			return false;
		}

		if (initializer.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) initializer;
			if (ASTNodeUtil.containsDiamondOperator(classInstanceCreation)) {
				return false;
			}

			if (classInstanceCreation.getAnonymousClassDeclaration() != null) {
				return false;
			}
		}

		return verifyTypeCompatibility(variableName, initializerType);
	}

	private boolean verifyLoopPrecondition(Expression loopExpression, SimpleName variableName) {
		ITypeBinding expressionType = loopExpression.resolveTypeBinding();
		if (expressionType == null) {
			return false;
		}

		ITypeBinding initializerType = null;
		if (expressionType.isParameterizedType()) {
			ITypeBinding[] argumentTypes = expressionType.getTypeArguments();
			if (argumentTypes != null && argumentTypes.length == 1) {
				initializerType = argumentTypes[0];
			}
		} else if (expressionType.isArray()) {
			initializerType = expressionType.getComponentType();
		}

		if (initializerType == null) {
			return false;
		}

		if (containsWildCard(initializerType)) {
			return false;
		}

		return verifyTypeCompatibility(variableName, initializerType);
	}

	/**
	 * Checks whether it is possible to replace the type of a local variable
	 * with the type of its initializer. Some reasons why this could lead to
	 * problems include:
	 * <ul>
	 * <li>the variable is re-assigned with subtypes which are incompatible with
	 * the initializer type</li>
	 * <li>the variable is used as a parameter in overloaded methods</li>
	 * <li>raw types are used in initializer or declaration</li>
	 * <li>the declaration type contains undefined types like wildcards,
	 * intersection types, etc</li>
	 * <ul>
	 * 
	 * @param variableName
	 *            name of the variable to be checked
	 * @param initializerType
	 *            type of the variable initialzier
	 * @return {@code true} if it is safe to replace the declaration type with
	 *         the initializer type, or {@code false} otherwise.
	 */
	private boolean verifyTypeCompatibility(SimpleName variableName, ITypeBinding initializerType) {
		IBinding binding = variableName.resolveBinding();
		if (binding == null) {
			return false;
		}

		if (binding.getKind() != IBinding.VARIABLE) {
			return false;
		}

		IVariableBinding varBinding = (IVariableBinding) binding;
		if (varBinding.isField() || varBinding.isParameter()) {
			return false;
		}

		ITypeBinding typeBinding = varBinding.getType();
		if (typeBinding == null || typeBinding.isAnonymous() || typeBinding.isIntersectionType()
				|| typeBinding.isWildcardType() || typeBinding.isPrimitive()) {
			return false;
		}

		if (typeBinding.isArray() && typeBinding.getElementType()
			.isPrimitive()) {
			return false;
		}

		if (!isReassignedWithDifferentSubtypes(variableName, typeBinding, initializerType)) {
			return false;
		}

		if (!ClassRelationUtil.compareITypeBinding(typeBinding, initializerType)
				&& isUsedInOverloadedMethod(variableName)) {
			return false;
		}

		return areRawCompatible(initializerType, typeBinding);
	}

	/**
	 * Verifies if a variable is re-assigned to a value which is incompatible
	 * with the type of the initializer. E.g.
	 * 
	 * <pre>
	 * {
	 * 	&#64;code
	 * 	List<User> list = new ArrayList<>();
	 * 	list = new LinkedList<>();
	 * }
	 * </pre>
	 * 
	 * Makes use of {@link VariableAssignmentVisitor} for finding all
	 * assignments of the local variable.
	 * 
	 * @param variableName
	 *            name of the variable to be checked.
	 * @param variableTypeBinding
	 *            the type binding of the variable to be checked.
	 * @param initializerType
	 *            the type of the variable initializer
	 * @return {@code true} if it is safe to change the type of the variable to
	 *         the type of its initializer, or {@code false} otherwise.
	 */
	private boolean isReassignedWithDifferentSubtypes(SimpleName variableName, ITypeBinding variableTypeBinding,
			ITypeBinding initializerType) {

		if (ClassRelationUtil.compareITypeBinding(variableTypeBinding, initializerType)) {
			return true;
		}

		ITypeBinding typeBindingErasure = variableTypeBinding.getErasure();
		if (!ClassRelationUtil.isInheritingContentOfTypes(initializerType,
				Collections.singletonList(typeBindingErasure.getQualifiedName()))) {
			return false;
		}

		Block block = ASTNodeUtil.getSpecificAncestor(variableName, Block.class);
		if (block == null) {
			return false;
		}

		VariableAssignmentVisitor visitor = new VariableAssignmentVisitor(variableName);
		block.accept(visitor);
		List<Assignment> assignments = visitor.getAssignments();
		for (Assignment assignment : assignments) {
			Expression rightHandSide = assignment.getRightHandSide();
			ITypeBinding rightHandSideTypeBinding = rightHandSide.resolveTypeBinding();

			if (rightHandSideTypeBinding == null || !rightHandSideTypeBinding.isAssignmentCompatible(initializerType)) {
				return false;
			}
		}

		return true;
	}

	private boolean isUsedInOverloadedMethod(SimpleName variableName) {
		LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(variableName);
		Block block = ASTNodeUtil.getSpecificAncestor(variableName, Block.class);
		block.accept(visitor);
		List<SimpleName> usages = visitor.getUsages();
		return usages.stream()
			.filter(name -> name.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY)
			.anyMatch(name -> this.isOverloadedInParameter(MethodInvocation.class.cast(name.getParent()), name));
	}

	private boolean isOverloadedInParameter(MethodInvocation methodInvocation, SimpleName parameter) {
		List<IMethodBinding> overloads = ClassRelationUtil.findOverloadedMethods(methodInvocation);
		if(overloads.isEmpty()) {
			return false;
		}

		int position = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class)
			.indexOf(parameter);
		if(position < 0) {
			return false;
		}
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}

		return overloads.stream()
			.anyMatch(method -> ClassRelationUtil.isOverloadedOnParameter(methodBinding, method, position));
	}

	private boolean areRawCompatible(ITypeBinding initializerType, ITypeBinding declarationType) {
		if (initializerType.isRawType() && declarationType.isRawType()) {
			return true;
		}
		if (initializerType.isRawType()) {
			return false;
		}

		return !declarationType.isRawType();
	}

	private boolean containsWildCard(ITypeBinding initializerType) {
		if (!initializerType.isParameterizedType()) {
			return false;
		}
		ITypeBinding[] typeArguments = initializerType.getTypeArguments();
		for (ITypeBinding type : typeArguments) {
			if (type.isWildcardType()) {
				return true;
			}
		}
		return false;
	}

	private void removeArrayDimensions(VariableDeclarationFragment node, Type type) {
		if (type.isArrayType()) {
			ArrayType arrayType = (ArrayType) type;
			ASTNodeUtil.convertToTypedList(arrayType.dimensions(), Dimension.class)
				.forEach(dimension -> astRewrite.remove(dimension, null));
		}
		removeArrayDimensions(ASTNodeUtil.convertToTypedList(node.extraDimensions(), Dimension.class));
	}

	private void removeAndSaveComments(ASTNode node) {
		astRewrite.remove(node, null);
		getCommentRewriter().saveCommentsInParentStatement(node);
	}

	private void removeArrayDimensions(List<Dimension> dimensions) {
		dimensions.forEach(this::removeAndSaveComments);
	}

	private void replaceWithVarType(Type type) {
		Type varType = createVarType(type);
		astRewrite.replace(type, varType, null);
		onRewrite();
	}

	private Type createVarType(ASTNode node) {
		AST ast = node.getAST();
		SimpleName var = ast.newSimpleName(VAR_KEY_WORD);
		return ast.newSimpleType(var);
	}
}
