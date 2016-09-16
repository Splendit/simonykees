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
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class StringUtilsASTVisitor extends AbstractCompilationUnitAstVisitor {

	private boolean stringUtilsRequired = false;
	private static final String STRING = "java.lang.String";
			
	private static final String STRING_UTILS = "org.apache.commons.lang3.StringUtils";
	private static final String IS_EMPTY = "isEmpty";
	private static final String TRIM = "trim";
	private static final String EQUALS = "equals";
	private static final String ENDSWITH = "endsWith";
	private static final String INDEXOF = "indexOf";
	private static final String CONTAINS = "contains";
	private static final String SUBSTRING = "substring";
	private static final String SPLIT = "split";
	private static final String REPLACE = "replace";
	private static final String STARTS_WITH = "startsWith";

	public StringUtilsASTVisitor(ASTRewrite astRewrite, List<IType> itypes) {
		super(astRewrite, itypes);
	}

	public StringUtilsASTVisitor(ASTRewrite astRewrite) {
		super(astRewrite);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		Expression optionalExpression = node.getExpression();
		if (optionalExpression == null) {
			return true;
		}
		if (isContentofRegistertITypes(optionalExpression.resolveTypeBinding())) {
			AST currentAST = node.getAST();
			String replacementOperation = null;
			String op = null;
			switch (op = node.getName().getFullyQualifiedName()) {
			case IS_EMPTY:
			case TRIM:
			case EQUALS:
			case ENDSWITH:
			case INDEXOF:
			case CONTAINS:
			case SUBSTRING:
			case SPLIT:
			case REPLACE:
				replacementOperation = op;
				break;
			case "toUpperCase":
				replacementOperation = "upperCase";
				break;
			case "toLowerCase":
				replacementOperation = "lowerCase";
				break;
			case STARTS_WITH:
				if (node.arguments().size() == 1) {
					replacementOperation = STARTS_WITH;
				}
				break;
			default:
				break;
			}
			if (replacementOperation != null) {
				stringUtilsRequired = true;
				astRewrite.set(node, MethodInvocation.EXPRESSION_PROPERTY, currentAST.newSimpleName("StringUtils"),
						null);
				astRewrite.set(node, MethodInvocation.NAME_PROPERTY, node.getAST().newSimpleName(replacementOperation),
						null);
				astRewrite.getListRewrite(node, MethodInvocation.ARGUMENTS_PROPERTY)
						.insertFirst((Expression) ASTNode.copySubtree(currentAST, node.getExpression()), null);
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public void endVisit(CompilationUnit node) {
		if (stringUtilsRequired) {
			node.imports();
			ImportDeclaration stringUtilsImport = node.getAST().newImportDeclaration();
			stringUtilsImport.setName(node.getAST().newName(STRING_UTILS));
			if (node.imports().stream().noneMatch(importDeclaration -> (new ASTMatcher())
					.match((ImportDeclaration) importDeclaration, stringUtilsImport))) {
				// node.imports().add(stringUtilsImport);
				astRewrite.getListRewrite(node, CompilationUnit.IMPORTS_PROPERTY).insertLast(stringUtilsImport, null);
			}
		}
	}

	@Override
	protected String[] relevantClasses() {
		return new String[] { STRING };
	}

}
