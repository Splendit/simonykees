package eu.jsparrow.core.visitor.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * This ASTVisitor finds the usage of specified string operation and wraps it in
 * {@link StringUtils} commands.
 * 
 * 
 * @author Martin Huter
 * @since 0.9
 */
public class StringUtilsASTVisitor extends AbstractAddImportASTVisitor {

	private static final String STRING_FULLY_QUALIFIED_NAME = java.lang.String.class.getName();
	private static final String STRING_UTILS_FULLY_QUALIFIED_NAME = org.apache.commons.lang3.StringUtils.class
		.getName();

	private static final String IS_EMPTY = "isEmpty"; //$NON-NLS-1$
	private static final String TRIM = "trim"; //$NON-NLS-1$

	// removed due to SIM-86
	// private static final String EQUALS = "equals"; //$NON-NLS-1$

	private static final String EQUALS_IGNORE_CASE = "equalsIgnoreCase"; //$NON-NLS-1$
	private static final String ENDSWITH = "endsWith"; //$NON-NLS-1$
	private static final String INDEXOF = "indexOf"; //$NON-NLS-1$
	private static final String CONTAINS = "contains"; //$NON-NLS-1$
	private static final String SUBSTRING = "substring"; //$NON-NLS-1$

	// removed due to SIM-78
	// private static final String SPLIT = "split"; //$NON-NLS-1$

	// removed due to SIM-85
	// private static final String REPLACE = "replace"; //$NON-NLS-1$

	private static final String STARTS_WITH = "startsWith"; //$NON-NLS-1$

	private static final String TO_UPPER_CASE = "toUpperCase"; //$NON-NLS-1$
	private static final String UPPER_CASE = "upperCase"; //$NON-NLS-1$

	private static final String TO_LOWER_CASE = "toLowerCase"; //$NON-NLS-1$
	private static final String LOWER_CASE = "lowerCase"; //$NON-NLS-1$

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (continueVisiting) {
			verifyImport(compilationUnit, STRING_UTILS_FULLY_QUALIFIED_NAME);
		}
		return continueVisiting;
	}

	@Override
	public boolean visit(MethodInvocation node) {

		Expression optionalExpression = node.getExpression();
		if (optionalExpression == null) {
			return true;
		}

		if (ClassRelationUtil.isContentOfTypes(optionalExpression.resolveTypeBinding(),
				generateFullyQualifiedNameList(STRING_FULLY_QUALIFIED_NAME))) {
			AST currentAST = node.getAST();
			String replacementOperation = null;
			String stringOperation = node.getName()
				.getFullyQualifiedName();
			switch (stringOperation) {
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
				replacementOperation = stringOperation;
				break;
			case TO_UPPER_CASE:
				replacementOperation = UPPER_CASE;
				break;
			case TO_LOWER_CASE:
				replacementOperation = LOWER_CASE;
				break;
			case STARTS_WITH:
				if (node.arguments()
					.size() == 1) {
					replacementOperation = STARTS_WITH;
				}
				break;
			default:
				break;
			}
			if (replacementOperation != null) {
				addImport(STRING_UTILS_FULLY_QUALIFIED_NAME);
				Name stringUtilsName = findTypeName(STRING_UTILS_FULLY_QUALIFIED_NAME);
				astRewrite.set(node, MethodInvocation.EXPRESSION_PROPERTY, stringUtilsName,
						null);
				astRewrite.set(node, MethodInvocation.NAME_PROPERTY, node.getAST()
					.newSimpleName(replacementOperation), null);
				astRewrite.getListRewrite(node, MethodInvocation.ARGUMENTS_PROPERTY)
					.insertFirst(ASTNode.copySubtree(currentAST, node.getExpression()), null);
				saveComments(node);
				onRewrite();
			}
		}
		return true;
	}

	private void saveComments(MethodInvocation node) {
		CommentRewriter commRewrite = getCommentRewriter();
		Statement parent = ASTNodeUtil.getSpecificAncestor(node, Statement.class);
		List<Comment> internalComments = commRewrite.findInternalComments(node);
		commRewrite.saveBeforeStatement(parent, internalComments);
	}
}
