package eu.jsparrow.core.visitor.security.random;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class UseClassSecureRandomASTVisitor extends AbstractAddImportASTVisitor {

	private static final String SECURE_RANDOM_QUALIFIED_NAME = java.security.SecureRandom.class.getName();
	private final Map<CompilationUnit, Boolean> isSafeToAddImportMap = new HashMap<>();

	@Override
	public boolean visit(CompilationUnit node) {
		super.visit(node);
		boolean flagSafeImport = isSafeToAddImport(node, SECURE_RANDOM_QUALIFIED_NAME);
		isSafeToAddImportMap.put(node, Boolean.valueOf(flagSafeImport));
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (!analyzeInstanceCreation(node)) {
			return false;
		}
		replaceUnsafeRandomInstanceCreation(node);
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {

		ITypeBinding typeBinding = node.getType()
			.resolveBinding();
		if (!typeBinding.getQualifiedName()
			.equals(java.util.Random.class.getName())) {
			return false;
		}

		replaceUnsafeRandomVariableType(node);
		return true;
	}

	@Override
	public void endVisit(CompilationUnit node) {
		super.endVisit(node);
		isSafeToAddImportMap.clear();
	}

	private boolean analyzeInstanceCreation(ClassInstanceCreation classInstanceCreation) {

		ITypeBinding typeBinding = classInstanceCreation.getType()
			.resolveBinding();
		if (!typeBinding.getQualifiedName()
			.equals(java.util.Random.class.getName())) {
			return false;
		}

		if (!classInstanceCreation.arguments()
			.isEmpty()) {
			return false;
		}

		ASTNode astNode = classInstanceCreation;
		while (astNode != null) {
			StructuralPropertyDescriptor locationInParent = astNode.getLocationInParent();
			if (locationInParent == MethodInvocation.ARGUMENTS_PROPERTY) {
				return false;
			}

			if (locationInParent == MethodInvocation.EXPRESSION_PROPERTY) {
				MethodInvocation methodInvocation = (MethodInvocation) astNode.getParent();
				ITypeBinding declaringClass = methodInvocation.resolveMethodBinding()
					.getDeclaringClass();
				if (!ClassRelationUtil.isContentOfType(declaringClass, java.util.Random.class.getName())) {
					return false;
				}
				String methodName = methodInvocation.getName()
					.getIdentifier();
				return methodName.startsWith("next") //$NON-NLS-1$
						|| methodName.equals("doubles") //$NON-NLS-1$
						|| methodName.equals("ints") //$NON-NLS-1$
						|| methodName.equals("longs"); //$NON-NLS-1$
			}

			if (locationInParent == VariableDeclarationFragment.INITIALIZER_PROPERTY) {
				return true;
			}

			if (locationInParent == ExpressionStatement.EXPRESSION_PROPERTY) {
				return true;
			}

			boolean continueLoop = locationInParent == Assignment.RIGHT_HAND_SIDE_PROPERTY
					|| locationInParent == ParenthesizedExpression.EXPRESSION_PROPERTY;

			if (!continueLoop) {
				break;
			}

			astNode = astNode.getParent();

		}
		return false;
	}

	private Type getSecureRandomType() {
		CompilationUnit compilationUnit = getCompilationUnit();
		AST ast = compilationUnit.getAST();
		Name secureRandomName;
		boolean flagSafeImport = isSafeToAddImportMap.get(compilationUnit)
			.booleanValue();
		if (flagSafeImport) {
			this.addImports.add(SECURE_RANDOM_QUALIFIED_NAME);
			secureRandomName = ast
				.newSimpleName(java.security.SecureRandom.class.getSimpleName());
		} else {
			secureRandomName = ast.newName(SECURE_RANDOM_QUALIFIED_NAME);
		}
		return ast.newSimpleType(secureRandomName);
	}

	void replaceUnsafeRandomInstanceCreation(ClassInstanceCreation instanceCreation) {
		astRewrite.replace(instanceCreation.getType(), getSecureRandomType(), null);
		onRewrite();
	}

	void replaceUnsafeRandomVariableType(VariableDeclarationStatement variableDeclarationStatement) {
		astRewrite.replace(variableDeclarationStatement.getType(), getSecureRandomType(), null);
		onRewrite();
	}
}
