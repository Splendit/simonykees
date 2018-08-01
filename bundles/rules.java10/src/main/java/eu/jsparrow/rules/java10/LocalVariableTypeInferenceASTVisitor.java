package eu.jsparrow.rules.java10;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

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
		Expression initializer = enhancedForStatement.getExpression();

		if (type.isVar()) {
			return true;
		}

		boolean satisfiedPrecondition = verifyLoopPrecondition(initializer, name);
		if (!satisfiedPrecondition) {
			return true;
		}

		replaceWithVarType(type);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		if (VariableDeclarationStatement.FRAGMENTS_PROPERTY == node.getLocationInParent()) {
			VariableDeclarationStatement parent = (VariableDeclarationStatement) node.getParent();
			if (hasMultipleFragments(parent)) {
				return true;
			}
		}
		Expression initializer = node.getInitializer();
		SimpleName name = node.getName();

		boolean satisfiedPrecondition = verifyDeclarationPrecondition(initializer, name);
		if (!satisfiedPrecondition) {
			return true;
		}

		Type type = null;
		ASTNode parent = node.getParent();
		if (parent instanceof VariableDeclarationStatement) {
			type = ((VariableDeclarationStatement) parent).getType();
		} else if (parent instanceof VariableDeclarationExpression) {
			type = ((VariableDeclarationExpression) parent).getType();
		}

		if (type == null || type.isVar()) {
			return false;
		}

		replaceWithVarType(type);
		return true;
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

		ITypeBinding initializerType = initializer.resolveTypeBinding();
		if (initializerType == null || containsWildCard(initializerType)) {
			return false;
		}

		if (initializer.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) initializer;
			if (ASTNodeUtil.containsDiamondOperator(classInstanceCreation)) {
				return false;
			}
		}

		return verifyTypeCompatibility(variableName, initializerType);
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
		
		if(!ClassRelationUtil.compareITypeBinding(typeBinding, initializerType)) {
			return false;
		}

		return areRawCompatible(initializerType, typeBinding);
	}
	
	private boolean verifyLoopPrecondition(Expression initializer, SimpleName variableName) {
		if (initializer == null) {
			return false;
		}

		ITypeBinding expressionType = initializer.resolveTypeBinding();
		
		ITypeBinding initializerType = null;
		if(expressionType.isParameterizedType()) {
			ITypeBinding[] argumentTypes = expressionType.getTypeArguments();
			if(argumentTypes != null && argumentTypes.length == 1) {
				initializerType = argumentTypes[0];
			}
		} else if(expressionType.isArray()) {
			initializerType = expressionType.getComponentType();
		}

		if(initializerType == null) {
			return false;
		}

		if (containsWildCard(initializerType)) {
			return false;
		}

		if (initializer.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) initializer;
			if (ASTNodeUtil.containsDiamondOperator(classInstanceCreation)) {
				return false;
			}
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
