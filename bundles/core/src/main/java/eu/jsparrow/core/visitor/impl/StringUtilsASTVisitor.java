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

	private static final String STRING_UTILS = "StringUtils"; //$NON-NLS-1$
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
		boolean clashingImports = false;

		/*
		 * Check if there is any type inside the compilation unit with the same
		 * name as StringUtils
		 */
		ConflictingTypeDeclarationAstVisitor visitor = new ConflictingTypeDeclarationAstVisitor();
		compilationUnit.accept(visitor);
		boolean isConflictingTypeName = visitor.isSafe();
		if (!isConflictingTypeName) {
			clashingImports = true;
		}

		/*
		 * Check if there is another import declaration conflicting with
		 * org.apache.commons.lang3.StringUtils
		 */

		List<ImportDeclaration> imports = ASTNodeUtil.returnTypedList(compilationUnit.imports(),
				ImportDeclaration.class);

		if (!clashingImports) {
			clashingImports = imports.stream()
				.anyMatch(importDeclaration -> ClassRelationUtil.importsTypeOnDemand(importDeclaration,
						STRING_UTILS));
		}

		if (!clashingImports) {
			for (ImportDeclaration importDeclaration : imports) {
				Name qualifiedName = importDeclaration.getName();
				String fullyQualifiedName = qualifiedName.getFullyQualifiedName();
				if (ASTNode.QUALIFIED_NAME == qualifiedName.getNodeType()) {
					SimpleName name = ((QualifiedName) qualifiedName).getName();
					if (StringUtils.equals(name.getIdentifier(), STRING_UTILS)
							&& !StringUtils.equals(fullyQualifiedName, STRING_UTILS_FULLY_QUALIFIED_NAME)) {
						clashingImports = true;
						break;
					}
				}
			}
		}

		boolean safeToGo = false;
		if (!clashingImports) {
			safeToGo = super.visit(compilationUnit);
		}

		return safeToGo;
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
				addImports.add(STRING_UTILS_FULLY_QUALIFIED_NAME);
				astRewrite.set(node, MethodInvocation.EXPRESSION_PROPERTY, currentAST.newSimpleName(STRING_UTILS),
						null);
				astRewrite.set(node, MethodInvocation.NAME_PROPERTY, node.getAST()
					.newSimpleName(replacementOperation), null);
				astRewrite.getListRewrite(node, MethodInvocation.ARGUMENTS_PROPERTY)
					.insertFirst((Expression) ASTNode.copySubtree(currentAST, node.getExpression()), null);
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

	/**
	 * Checks for a type declaration with name
	 * {@value StringUtilsASTVisitor#STRING_UTILS}.
	 * 
	 * @author Ardit Ymeri
	 *
	 */
	private class ConflictingTypeDeclarationAstVisitor extends ASTVisitor {
		private boolean safe = true;

		@Override
		public boolean visit(TypeDeclaration node) {
			if (STRING_UTILS.equals(node.getName()
				.getIdentifier())) {
				safe = false;
			}
			return safe;
		}

		public boolean isSafe() {
			return safe;
		}

		@Override
		public boolean preVisit2(ASTNode node) {
			return safe;
		}
	}
}
