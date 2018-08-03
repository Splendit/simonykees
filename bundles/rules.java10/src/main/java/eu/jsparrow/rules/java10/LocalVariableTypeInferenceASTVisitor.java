package eu.jsparrow.rules.java10;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.VariableAssignmentVisitor;

public class LocalVariableTypeInferenceASTVisitor extends AbstractASTRewriteASTVisitor {

	private static final String VAR_KEY_WORD = "var"; //$NON-NLS-1$
	CompilationUnit astRoot = null;

	@Override
	public boolean visit(CompilationUnit node) {
		super.visit(node);
		this.astRoot = node;
		return true;
	}

	@Override
	public void endVisit(CompilationUnit node) {
		super.visit(node);
		this.astRoot = null;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement enhancedForStatement) {
		SingleVariableDeclaration parameter = enhancedForStatement.getParameter();
		Type type = parameter.getType();
		SimpleName name = parameter.getName();
		Expression loopExpression = enhancedForStatement.getExpression();

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
		onRewrite();
		getCommentRewriter().saveCommentsInParentStatement(type);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		Expression initializer = node.getInitializer();
		SimpleName name = node.getName();

		Type type = null;
		ASTNode parent = node.getParent();
		if (parent.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
			VariableDeclarationStatement statement = (VariableDeclarationStatement) parent;
			if (hasMultipleFragments(statement)) {
				return true;
			}
			type = statement.getType();
		} else if (parent.getNodeType() == ASTNode.VARIABLE_DECLARATION_EXPRESSION) {
			type = ((VariableDeclarationExpression) parent).getType();
		}

		if (type == null || type.isVar()) {
			return false;
		}

		boolean satisfiedPrecondition = verifyDeclarationPrecondition(initializer, name);
		if (!satisfiedPrecondition) {
			return true;
		}

		removeArrayDimensions(node, type);
		replaceWithVarType(type);
		onRewrite();
		getCommentRewriter().saveCommentsInParentStatement(type);
		return true;
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

	private boolean hasMultipleFragments(VariableDeclarationStatement declarationStatement) {
		List<VariableDeclarationFragment> fragments = ASTNodeUtil.convertToTypedList(declarationStatement.fragments(),
				VariableDeclarationFragment.class);
		return fragments.size() != 1;
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

	private boolean verifyDeclarationPrecondition(Expression initializer, SimpleName variableName) {
		if (initializer == null) {
			return false;
		}

		if (ASTNode.LAMBDA_EXPRESSION == initializer.getNodeType()) {
			return false;
		}

		if (ASTNode.ARRAY_INITIALIZER == initializer.getNodeType()) {
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
		if (initializerType == null || containsWildCard(initializerType) || isGenericMethod(initializer)) {
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

	private boolean isGenericMethod(Expression initializer) {

		if (initializer.getNodeType() == ASTNode.METHOD_INVOCATION) {
			MethodInvocation initializerMethod = (MethodInvocation) initializer;
			IMethodBinding methodBinding = initializerMethod.resolveMethodBinding();
			if (methodBinding == null) {
				return true;
			}
			IMethodBinding methodDeclaration = methodBinding.getMethodDeclaration();
			if (methodDeclaration == null || methodDeclaration.isGenericMethod()) {
				return true;
			}
		}
		return false;
	}

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

		if (!isConvertibleToType(variableName, typeBinding, initializerType)) {
			return false;
		}

		if (isUsedInOverloadedMethod(variableName)) {
			return false;
		}

		return areRawCompatible(initializerType, typeBinding);
	}

	private boolean isUsedInOverloadedMethod(SimpleName variableName) {
		LocalVariableUsagesASTVisitor visitor = new LocalVariableUsagesASTVisitor(variableName);
		Block block = ASTNodeUtil.getSpecificAncestor(variableName, Block.class);
		block.accept(visitor);
		List<SimpleName> usages = visitor.getUsages();
		return usages.stream()
			.filter(name -> name.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY)
			.map(parameter -> MethodInvocation.class.cast(parameter.getParent()))
			.anyMatch(methodInvocation -> isOverloaded(methodInvocation));
	}

	private boolean isOverloaded(MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}

		ITypeBinding delcaringClass = methodBinding.getDeclaringClass();
		List<IMethodBinding> inheritedMethods = ClassRelationUtil.findInheretedMethods(delcaringClass);
		IMethodBinding[] declaredMethods = delcaringClass.getDeclaredMethods();
		List<IMethodBinding> allmethods = new ArrayList<>();
		allmethods.addAll(inheritedMethods);
		allmethods.addAll(Arrays.asList(declaredMethods));
		String identifier = methodInvocation.getName()
			.getIdentifier();

		return allmethods.stream()
			.map(IMethodBinding::getName)
			.anyMatch(identifier::equals);
	}

	private boolean isConvertibleToType(SimpleName variableName, ITypeBinding typeBinding,
			ITypeBinding initializerType) {

		if (ClassRelationUtil.compareITypeBinding(typeBinding, initializerType)) {
			return true;
		}

		ITypeBinding typeBindingErasure = typeBinding.getErasure();
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
}
