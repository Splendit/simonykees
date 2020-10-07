package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.visitor.sub.LiveVariableScope;
import eu.jsparrow.rules.common.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;

/**
 * ASTVisitor that searches deprecated Date constructs and replaces it with
 * legal ones.
 * 
 * @author Martin Huter, Ardit Ymeri
 * @since 2.5
 *
 */
public class DateDeprecatedASTVisitor extends AbstractAddImportASTVisitor {

	private static final Logger logger = LoggerFactory.getLogger(DateDeprecatedASTVisitor.class);

	private static final String DATE_QUALIFIED_NAME = java.util.Date.class.getName();
	private static final String CALENDAR_QUALIFIED_NAME = java.util.Calendar.class.getName();
	private static final String CALENDAR = "calendar"; //$NON-NLS-1$

	private LiveVariableScope scope = new LiveVariableScope();

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		boolean continueVisiting = super.visit(compilationUnit);
		if (continueVisiting) {
			verifyImport(compilationUnit, CALENDAR_QUALIFIED_NAME);
		}
		return continueVisiting;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (ClassRelationUtil.isContentOfType(node.getType()
			.resolveBinding(), DATE_QUALIFIED_NAME)) {
			List<Expression> expressionList = ASTNodeUtil.returnTypedList(node.arguments(), Expression.class);

			switch (expressionList.size()) {
			/*
			 * Constructors with 3, 5 and 6 arguments are deprecated.
			 */
			case 3:
			case 5:
			case 6:
				ASTNode enclosingScope = this.scope.findEnclosingScope(node)
					.orElse(null);
				if (enclosingScope == null) {
					logger.warn("The scope of the Date declaration cannot be found!"); //$NON-NLS-1$
					break;
				}
				this.scope.lazyLoadScopeNames(enclosingScope);
				String calendarName = findCalendarName();

				if (enclosingScope.getNodeType() == ASTNode.TYPE_DECLARATION) {
					replaceFiledInstantiation(node, calendarName, expressionList);
				} else {
					replaceConstructorInStatement(node, calendarName, expressionList, enclosingScope);
				}
				break;
			default:
				break;
			}
		}
		return true;
	}

	@Override
	public void endVisit(CompilationUnit compilationUnit) {
		this.scope.clearCompilationUnitScope(compilationUnit);
		super.endVisit(compilationUnit);
	}

	@Override
	public void endVisit(TypeDeclaration typeDeclaration) {
		this.scope.clearFieldScope(typeDeclaration);
	}

	@Override
	public void endVisit(MethodDeclaration methodDeclaration) {
		this.scope.clearLocalVariablesScope(methodDeclaration);
	}

	@Override
	public void endVisit(FieldDeclaration fieldDeclaration) {
		this.scope.clearLocalVariablesScope(fieldDeclaration);
	}

	@Override
	public void endVisit(Initializer initializer) {
		this.scope.clearLocalVariablesScope(initializer);
	}

	/**
	 * Removes the deprecated constructor from the field declaration statement
	 * and introduces a {@link Initializer} to initialize the field with a
	 * calendar.
	 * 
	 * @param node
	 *            the node representing a deprecated {@link Date} constructor
	 * @param calendarName
	 *            the name of the calendar instance to be introduced
	 * @param expressionList
	 *            the list of the arguments in the deprecated constructor.
	 */
	private void replaceFiledInstantiation(ClassInstanceCreation node, String calendarName,
			List<Expression> expressionList) {
		/*
		 * get the declaration fragment, e.g. date = new Date(99, 1, 1)
		 */
		VariableDeclarationFragment fragment = ASTNodeUtil.getSpecificAncestor(node, VariableDeclarationFragment.class);
		if (FieldDeclaration.FRAGMENTS_PROPERTY != fragment.getLocationInParent()) {
			logger.warn("Not a field declaration!"); //$NON-NLS-1$
			return;
		}

		/*
		 * get the whole field declaration statement
		 */
		FieldDeclaration fieldDeclaration = (FieldDeclaration) fragment.getParent();
		if (fieldDeclaration.getLocationInParent() != TypeDeclaration.BODY_DECLARATIONS_PROPERTY) {
			logger.warn("Not a field of a type declaration!"); //$NON-NLS-1$
			return;
		}

		// the class wrapping the declaration
		TypeDeclaration typeDeclaration = (TypeDeclaration) fieldDeclaration.getParent();
		AST ast = node.getAST();

		/*
		 * Creating the body of the initializer
		 */
		Block body = ast.newBlock();
		@SuppressWarnings("unchecked")
		List<Statement> bodyStatements = (List<Statement>) body.statements();
		List<Statement> calendarStatemetns = generateCalendar(ast, calendarName, expressionList);
		bodyStatements.addAll(calendarStatemetns);

		SimpleName dateName = fragment.getName();
		MethodInvocation calendarGetTime = getMethodInvocation(ast, calendarName);
		Assignment assignment = ast.newAssignment();
		SimpleName dateNameCopy = ast.newSimpleName(dateName.getIdentifier());
		assignment.setLeftHandSide(dateNameCopy);
		assignment.setRightHandSide(calendarGetTime);
		ExpressionStatement assignmentStatement = ast.newExpressionStatement(assignment);
		bodyStatements.add(assignmentStatement);

		/*
		 * Creating the initializer and setting the body created above
		 */
		Initializer initializer = ast.newInitializer();
		initializer.setBody(body);

		/*
		 * Insert the Initializer after the field declaration and remove the
		 * existing deprecated constructor.
		 */
		ListRewrite listRewrite = astRewrite.getListRewrite(typeDeclaration,
				TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertAfter(initializer, fieldDeclaration, null);
		astRewrite.replace(fragment, astRewrite.createCopyTarget(dateName), null);
		this.scope.addName(fieldDeclaration, calendarName);
		onRewrite();
		CommentRewriter commentRewriter = getCommentRewriter();
		List<Comment> relatedComments = commentRewriter.findRelatedComments(node);
		Collections.reverse(relatedComments);
		commentRewriter.saveCommentsInBlock(body, relatedComments);

	}

	/**
	 * Replaces the deprecated {@link Date} constructor with
	 * {@link Calendar#getTime()}. Covers only the cases where the deprecated
	 * constructor occurs in a {@link Statement}. The constructors occurring in
	 * a field declaration are handled by
	 * {@link #replaceFiledInstantiation(ClassInstanceCreation, String, List)}.
	 * Introduces an instance of {@link Calendar} and sets irs time
	 * corresponding to the arguments provided in the deprecated constructor.
	 * 
	 * @param node
	 *            representing the deprecated constructor to be replaced.
	 * @param calendarName
	 *            the name of the new calendar instance
	 * @param arguments
	 *            the list of arguments occurring in the deprecated constructor.
	 * @param scope
	 *            containing variables which are currently visible
	 */
	private void replaceConstructorInStatement(ClassInstanceCreation node, String calendarName,
			List<Expression> arguments, ASTNode scope) {
		AST ast = node.getAST();
		astRewrite.replace(node, getMethodInvocation(ast, calendarName), null);
		Statement ancestorStatment = ASTNodeUtil.getSpecificAncestor(node, Statement.class);
		CommentRewriter commentRewriter = getCommentRewriter();
		if (ancestorStatment.getLocationInParent() == Block.STATEMENTS_PROPERTY) {
			Block surroundingBlock = (Block) ancestorStatment.getParent();
			ListRewrite lrw = astRewrite.getListRewrite(surroundingBlock, Block.STATEMENTS_PROPERTY);
			generateCalendar(ast, calendarName, arguments).forEach(s -> lrw.insertBefore(s, ancestorStatment, null));
			commentRewriter.saveCommentsInParentStatement(node);
		} else {
			Block injectionBlock = ast.newBlock();
			@SuppressWarnings("unchecked")
			List<Statement> blockStatements = (List<Statement>) injectionBlock.statements();
			blockStatements.addAll(generateCalendar(ast, calendarName, arguments));
			blockStatements.add((Statement) astRewrite.createMoveTarget(ancestorStatment));
			astRewrite.replace(ancestorStatment, injectionBlock, null);
			List<Comment> relatedComments = commentRewriter.findRelatedComments(node);
			Collections.reverse(relatedComments);
			commentRewriter.saveCommentsInBlock(injectionBlock, relatedComments);
		}

		onRewrite();
		this.scope.addName(scope, calendarName);
	}

	/**
	 * Generates a safe variable name having {@value #CALENDAR} as a prefix and
	 * number suffix to ensure the uniqueness. Makes use of
	 * {@link #isInScope(String)} to check if the generated name is safe.
	 * 
	 * @return a unique identifier w.r.t to variables that are visible in the
	 *         current scope.
	 */
	private String findCalendarName() {
		String name = CALENDAR;
		int suffix = 1;
		while (this.scope.isInScope(name)) {
			name = CALENDAR + suffix;
			suffix++;
		}
		return name;
	}

	private MethodInvocation getMethodInvocation(AST ast, String nameOfCalendar) {
		return NodeBuilder.newMethodInvocation(ast, ast.newSimpleName(nameOfCalendar), ast.newSimpleName("getTime")); //$NON-NLS-1$
	}

	/**
	 * Generates one statement for creating a calendar instance and one for
	 * setting the time corresponding to the provided list of arguments.
	 * 
	 * @param ast
	 *            the ast for genereating the nodes
	 * @param nameOfCalendar
	 *            the identifier of the calendar to be introduced
	 * @param arguments
	 *            the list of arguments used for setting the calendar time
	 * @return the list of generated statements as described above
	 */
	@SuppressWarnings("unchecked")
	private List<Statement> generateCalendar(AST ast, String nameOfCalendar, List<Expression> arguments) {
		List<Statement> statementList = new ArrayList<>();
		// Calendar cal = Calendar.getInstance(); done
		VariableDeclarationFragment variableDeclFragment = ast.newVariableDeclarationFragment();
		variableDeclFragment.setName(NodeBuilder.newSimpleName(ast, nameOfCalendar));

		Name typeName = addImport(CALENDAR_QUALIFIED_NAME);
		variableDeclFragment.setInitializer(NodeBuilder.newMethodInvocation(ast,
				typeName, NodeBuilder.newSimpleName(ast, "getInstance"))); //$NON-NLS-1$

		VariableDeclarationStatement variableDeclStatement = ast.newVariableDeclarationStatement(variableDeclFragment);
		Type calendarType = ast.newSimpleType(ast.newName(typeName.getFullyQualifiedName()));
		variableDeclStatement.setType(calendarType);
		statementList.add(variableDeclStatement);
		// cal.set(1900 + 99, 1, 1); set arguments need to be added
		MethodInvocation setMethod = NodeBuilder.newMethodInvocation(ast,
				NodeBuilder.newSimpleName(ast, nameOfCalendar), NodeBuilder.newSimpleName(ast, "set")); //$NON-NLS-1$

		// Preparing the year
		Expression firstArgument = arguments.remove(0);
		if (ASTNode.NUMBER_LITERAL == firstArgument.getNodeType()) {
			try {
				Integer year = Integer.parseInt(((NumberLiteral) firstArgument).getToken());
				// Conversion to Calendar initialization
				year = year + 1900;
				setMethod.arguments()
					.add(ast.newNumberLiteral(year.toString()));
			} catch (NumberFormatException e) {
				// If number could not be parsed use the expression
				setMethod.arguments()
					.add(NodeBuilder.newInfixExpression(ast, InfixExpression.Operator.PLUS,
							ast.newNumberLiteral("1900"), //$NON-NLS-1$
							(Expression) astRewrite.createMoveTarget(firstArgument)));
			}

		}

		// Adding other arguments
		arguments.forEach(a -> setMethod.arguments()
			.add(astRewrite.createMoveTarget(a)));
		ExpressionStatement setStatement = NodeBuilder.newExpressionStatement(ast, setMethod);
		statementList.add(setStatement);
		return statementList;
	}
}
