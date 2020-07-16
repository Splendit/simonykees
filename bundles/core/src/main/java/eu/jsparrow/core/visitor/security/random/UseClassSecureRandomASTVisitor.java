package eu.jsparrow.core.visitor.security.random;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

public class UseClassSecureRandomASTVisitor extends AbstractAddImportASTVisitor {

	@Override
	public boolean visit(ClassInstanceCreation node) {

		ITypeBinding typeBinding = node.resolveTypeBinding();
		if (!typeBinding.getQualifiedName()
			.equals(java.util.Random.class.getName())) {
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

	Type getSecureRandomType() {
		String secureRandomQualifiedName = java.security.SecureRandom.class.getName();
		Name secureRandomName;
		AST ast = getCompilationUnit().getAST();
		if (isSafeToAddImport(getCompilationUnit(), secureRandomQualifiedName)) {
			this.addImports.add(secureRandomQualifiedName);
			secureRandomName = ast
				.newSimpleName(java.security.SecureRandom.class.getSimpleName());
		} else {
			secureRandomName = ast.newName(secureRandomQualifiedName);
		}
		return ast.newSimpleType(secureRandomName);
	}

	void replaceUnsafeRandomInstanceCreation(ClassInstanceCreation instanceCreation) {
		astRewrite.replace(instanceCreation.getType(), getSecureRandomType(), null);
	}

	void replaceUnsafeRandomVariableType(VariableDeclarationStatement variableDeclarationStatement) {
		astRewrite.replace(variableDeclarationStatement.getType(), getSecureRandomType(), null);
	}
}
