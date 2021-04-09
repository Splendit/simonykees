package eu.jsparrow.core.visitor.junit;

import java.util.Optional;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;

import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * 
 * @since 3.29.0
 *
 */
public class ReplaceJUnitAssertThatWithHamcrestASTVisitor extends AbstractAddImportASTVisitor {

	private static final String ORG_HAMCREST_MATCHER_ASSERT_ASSERT_THAT = "org.hamcrest.MatcherAssert.assertThat"; //$NON-NLS-1$
	private static final String ORG_HAMCREST_MATCHER_ASSERT = "org.hamcrest.MatcherAssert"; //$NON-NLS-1$
	
	private boolean updatedAssertThatStaticImport = false;

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		super.visit(compilationUnit);
		verifyImport(compilationUnit, ORG_HAMCREST_MATCHER_ASSERT);
		return true;
	}

	@Override
	public boolean visit(ImportDeclaration importDeclaration) {
		if (!importDeclaration.isStatic() || importDeclaration.isOnDemand()) {
			return false;
		}

		IBinding binding = importDeclaration.resolveBinding();
		boolean isMethodBinding = binding.getKind() == IBinding.METHOD;
		if (!isMethodBinding) {
			return false;
		}

		if (!isJUnitAssertThat((IMethodBinding) binding)) {
			return false;
		}

		AST ast = importDeclaration.getAST();
		ImportDeclaration newImportDeclaration = ast.newImportDeclaration();
		newImportDeclaration.setStatic(true);
		newImportDeclaration.setName(ast.newName(ORG_HAMCREST_MATCHER_ASSERT_ASSERT_THAT));
		astRewrite.replace(importDeclaration, newImportDeclaration, null);
		this.updatedAssertThatStaticImport = true;
		return true;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
		if (methodBinding == null) {
			return false;
		}
		boolean isJunitAssertThat = isJUnitAssertThat(methodBinding);
		if (!isJunitAssertThat) {
			return true;
		}

		Expression expression = methodInvocation.getExpression();

		if(expression == null) {
			if(!this.updatedAssertThatStaticImport) {
				verifyStaticMethodImport(getCompilationUnit(), ORG_HAMCREST_MATCHER_ASSERT_ASSERT_THAT);
				Optional<Name> newExpression = addImportForStaticMethod(ORG_HAMCREST_MATCHER_ASSERT_ASSERT_THAT, expression);
				newExpression.ifPresent(name -> astRewrite.set(methodInvocation, MethodInvocation.EXPRESSION_PROPERTY, name, null));
			}
		} else if (expression.getNodeType() == ASTNode.SIMPLE_NAME || expression.getNodeType() == ASTNode.QUALIFIED_NAME) {
			Name newExpression = addImport(ORG_HAMCREST_MATCHER_ASSERT, expression);
			astRewrite.replace(expression, newExpression, null);
		}
		onRewrite();
		return true;
	}

	private boolean isJUnitAssertThat(IMethodBinding methodBinding) {
		String methodIdentifier = methodBinding.getName();
		if (!"assertThat".equals(methodIdentifier)) { //$NON-NLS-1$
			return false;
		}
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		return ClassRelationUtil.isContentOfType(declaringClass, "org.junit.Assert");
	}
}
