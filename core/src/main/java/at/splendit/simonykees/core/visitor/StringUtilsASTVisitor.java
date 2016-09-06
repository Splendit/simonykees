package at.splendit.simonykees.core.visitor;

import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class StringUtilsASTVisitor extends AbstractCompilationUnitAstVisitor {

	private boolean stringUtilsRequired = false;

	public StringUtilsASTVisitor(ASTRewrite astRewrite, List<IType> itypes) {
		super(astRewrite, itypes);
	}

	public StringUtilsASTVisitor(ASTRewrite astRewrite) {
		super(astRewrite);
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean visit(MethodInvocation node) {
		Expression optionalExpression = node.getExpression();
		if (optionalExpression == null) {
			return true;
		}
		if (isContentofRegistertITypes(optionalExpression.resolveTypeBinding())) {
			AST currentAST = node.getAST();
			switch (node.getName().getFullyQualifiedName()) {
			case "isEmpty":
				// TODO implement improvement for isEmpty
				SimpleName stringUtils = currentAST.newSimpleName("StringUtils");
				MethodInvocation replacementMethodInvocation = currentAST.newMethodInvocation();
				replacementMethodInvocation.setExpression(stringUtils);
				replacementMethodInvocation.setName((SimpleName) ASTNode.copySubtree(currentAST, node.getName()));
				replacementMethodInvocation.arguments().add(ASTNode.copySubtree(currentAST, node.getExpression()));
				astRewrite.replace(node, replacementMethodInvocation, null);
				stringUtilsRequired = true;
				break;
			default:
				break;
			}
			return false;
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	public void endVisit(CompilationUnit node) {
		if (stringUtilsRequired) {
			node.imports();
			ImportDeclaration stringUtilsImport = node.getAST().newImportDeclaration();
			stringUtilsImport.setName(node.getAST().newName("org.apache.commons.lang3.StringUtils"));
			if (node.imports().stream().noneMatch(
					importDeclaration -> (new ASTMatcher()).match((ImportDeclaration) importDeclaration, stringUtilsImport))) {
				//node.imports().add(stringUtilsImport);
				astRewrite.getListRewrite(node, CompilationUnit.IMPORTS_PROPERTY).insertLast(stringUtilsImport, null);
			}
		}
	}

	@Override
	protected String[] relevantClasses() {
		return new String[] { "java.lang.String" };
	}

}
