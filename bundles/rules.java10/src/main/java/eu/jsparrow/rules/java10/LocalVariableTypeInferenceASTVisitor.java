package eu.jsparrow.rules.java10;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
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

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class LocalVariableTypeInferenceASTVisitor extends AbstractASTRewriteASTVisitor {

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
		
		if(type.isVar()) {
			return true;
		}
		
		boolean preconditionFullfilled = verifyPrecondition(initializer, name);
		if(!preconditionFullfilled) {
			return true;
		}
		

		Type varType = createVarType(type);
		astRewrite.replace(type, varType, null);
		onRewrite();
		
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		Expression initializer = node.getInitializer();
		SimpleName name = node.getName();
		
		boolean satisfiedPrecondition = verifyPrecondition(initializer, name);
		if(!satisfiedPrecondition) {
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
		
		Type varType = createVarType(node);
		astRewrite.replace(type, varType, null);
		onRewrite();
		
		return true;
	}

	private Type createVarType(ASTNode node) {
		AST ast = node.getAST();
		SimpleName var = ast.newSimpleName("var");
		Type varType = ast.newSimpleType(var);
		return varType;
	}

	private boolean verifyPrecondition(Expression initializer, SimpleName name) {
		if(initializer == null) {
			return false;
		}
		
		if(ASTNode.LAMBDA_EXPRESSION == initializer.getNodeType()) {
			return false;
		}
		
		ITypeBinding initializerType = initializer.resolveTypeBinding();
		if(initializerType == null || containsWildCard(initializerType)) {
			return false;
		}
		
		IBinding binding = name.resolveBinding();
		if(binding == null) {
			return false;
		}
		
		if(binding.getKind() != IBinding.VARIABLE) {
			return false;
		}
		
		IVariableBinding varBinding = (IVariableBinding) binding;
		if (varBinding.isField() || varBinding.isParameter()) {
			return false;
		}
		
		ITypeBinding typeBinding = varBinding.getType();
		if (typeBinding == null || typeBinding.isAnonymous() || typeBinding.isIntersectionType()
				|| typeBinding.isWildcardType() || typeBinding.isRawType()) {
			return false;
		}
		
		if(!areRawCompatible(initializerType, typeBinding)) {
			return false;
		}
		
		return true;
	}

	private boolean areRawCompatible(ITypeBinding initializerType, ITypeBinding declarationType) {
		if(initializerType.isRawType() && declarationType.isRawType()) {
			return true;
		}
		if(initializerType.isRawType()) {
			return false;
		}
		
		if(declarationType.isRawType()) {
			return false;
		}
		return true;
	}

	private boolean containsWildCard(ITypeBinding initializerType) {
		if(!initializerType.isParameterizedType()) {
			return false;
		}
		ITypeBinding[] typeArguments = initializerType.getTypeArguments();
		for(ITypeBinding type : typeArguments) {
			if(type.isWildcardType()) {
				return true;
			}
		}
		return false;
	}
}
