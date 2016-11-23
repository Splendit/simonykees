package at.splendit.simonykees.core.visitor;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import at.splendit.simonykees.core.util.ClassRelationUtil;

/**
 * This ASTVisitor finds the usage of specified string operation and wraps it in
 * {@link StringUtils} commands.
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class StringUtilsASTVisitor extends AbstractCompilationUnitASTVisitor {

	private boolean stringUtilsRequired = false;

	private static final Integer STRING_KEY = 1;
	private static final String STRING_FULLY_QUALLIFIED_NAME = "java.lang.String"; //$NON-NLS-1$

	private static final String STRING_UTILS_FULLY_QUALLIFIED_NAME = "org.apache.commons.lang3.StringUtils"; //$NON-NLS-1$

	private static final String STRING_UTILS = "StringUtils"; //$NON-NLS-1$
	private static final String IS_EMPTY = "isEmpty"; //$NON-NLS-1$
	private static final String TRIM = "trim"; //$NON-NLS-1$
	// private static final String EQUALS = "equals"; //$NON-NLS-1$ // FIXME:
	// see SIM-86
	private static final String EQUALS_IGNORE_CASE = "equalsIgnoreCase"; //$NON-NLS-1$
	private static final String ENDSWITH = "endsWith"; //$NON-NLS-1$
	private static final String INDEXOF = "indexOf"; //$NON-NLS-1$
	private static final String CONTAINS = "contains"; //$NON-NLS-1$
	private static final String SUBSTRING = "substring"; //$NON-NLS-1$
	// private static final String SPLIT = "split"; //$NON-NLS-1$ // FIXME: see
	// SIM-78
	// private static final String REPLACE = "replace"; //$NON-NLS-1$ // FIXME:
	// see SIM-85
	private static final String STARTS_WITH = "startsWith"; //$NON-NLS-1$

	private static final String TO_UPPER_CASE = "toUpperCase"; //$NON-NLS-1$
	private static final String UPPER_CASE = "upperCase"; //$NON-NLS-1$

	private static final String TO_LOWER_CASE = "toLowerCase"; //$NON-NLS-1$
	private static final String LOWER_CASE = "lowerCase"; //$NON-NLS-1$

	public StringUtilsASTVisitor() {
		super();
		this.fullyQuallifiedNameMap.put(STRING_KEY, generateFullyQuallifiedNameList(STRING_FULLY_QUALLIFIED_NAME));
	}

	@Override
	public boolean visit(MethodInvocation node) {
		Expression optionalExpression = node.getExpression();
		if (optionalExpression == null) {
			return true;
		}

		if (ClassRelationUtil.isContentOfRegistertITypes(optionalExpression.resolveTypeBinding(),
				this.iTypeMap.get(STRING_KEY))) {
			AST currentAST = node.getAST();
			String replacementOperation = null;
			String op = null;
			switch (op = node.getName().getFullyQualifiedName()) {
			case IS_EMPTY:
			case TRIM:
				// case EQUALS: // see SIM-86
			case EQUALS_IGNORE_CASE:
			case ENDSWITH:
			case INDEXOF:
			case CONTAINS:
			case SUBSTRING:
				// case SPLIT: // see SIM-78
				// case REPLACE: // see SIM-85
				replacementOperation = op;
				break;
			case TO_UPPER_CASE:
				replacementOperation = UPPER_CASE;
				break;
			case TO_LOWER_CASE:
				replacementOperation = LOWER_CASE;
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
				astRewrite.set(node, MethodInvocation.EXPRESSION_PROPERTY, currentAST.newSimpleName(STRING_UTILS),
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
			ImportDeclaration stringUtilsImport = node.getAST().newImportDeclaration();
			stringUtilsImport.setName(node.getAST().newName(STRING_UTILS_FULLY_QUALLIFIED_NAME));
			if (node.imports().stream().noneMatch(importDeclaration -> (new ASTMatcher())
					.match((ImportDeclaration) importDeclaration, stringUtilsImport))) {
				astRewrite.getListRewrite(node, CompilationUnit.IMPORTS_PROPERTY).insertLast(stringUtilsImport, null);
			}
		}
	}
}
