package eu.jsparrow.core.visitor.security.random;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

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

	@Override
	public void endVisit(CompilationUnit node) {
		super.endVisit(node);
		isSafeToAddImportMap.clear();
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
	}

	void replaceUnsafeRandomVariableType(VariableDeclarationStatement variableDeclarationStatement) {
		astRewrite.replace(variableDeclarationStatement.getType(), getSecureRandomType(), null);
	}
}
