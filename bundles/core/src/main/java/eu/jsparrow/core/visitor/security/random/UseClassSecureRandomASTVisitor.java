package eu.jsparrow.core.visitor.security.random;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.Type;

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

		ITypeBinding typeBinding = node.getType()
			.resolveBinding();
		if (!typeBinding.getQualifiedName()
			.equals(java.util.Random.class.getName())) {
			return true;
		}

		Expression expression = node;
		while (expression.getLocationInParent() == ParenthesizedExpression.EXPRESSION_PROPERTY) {
			expression = (Expression) expression.getParent();
		}
		if (expression.getLocationInParent() == ClassInstanceCreation.ARGUMENTS_PROPERTY
				|| expression.getLocationInParent() == MethodInvocation.ARGUMENTS_PROPERTY) {
			return true;
		}

		if (node.arguments()
			.isEmpty()) {
			astRewrite.replace(node.getType(), getSecureRandomType(), null);
			onRewrite();
			return true;
		}
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

}
