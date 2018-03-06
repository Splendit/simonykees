package eu.jsparrow.core.visitor.impl;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.builder.NodeBuilder;
import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.ClassRelationUtil;
import eu.jsparrow.rules.common.visitor.AbstractAddImportASTVisitor;

/**
 * ASTVisitor that searches deprecated Date constructs and replaces it with
 * legal ones.
 * 
 * @author Martin Huter
 * @since 2.5
 *
 */
public class DateDeprecatedASTVisitor extends AbstractAddImportASTVisitor {

	private static final Logger logger = LoggerFactory.getLogger(DateDeprecatedASTVisitor.class);

	private static final String DATE_QUALIFIED_NAME = java.util.Date.class.getName();
	private static final String CALENDAR_QUALIFIED_NAME = java.util.Calendar.class.getName();
	private static final String CALENDAR_NAME = java.util.Calendar.class.getSimpleName();
	
	private static final String CAL = "cal"; //$NON-NLS-1$

	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (ClassRelationUtil.isContentOfType(node.getType()
			.resolveBinding(), DATE_QUALIFIED_NAME)) {
			List<Expression> expressionList = ASTNodeUtil.returnTypedList(node.arguments(), Expression.class);

			switch (expressionList.size()) {
			case 3:
			case 5:
			case 6:
				astRewrite.replace(node, getMethodInvocation(node.getAST(), CAL), null);
				Statement ancestorStatment = ASTNodeUtil.getSpecificAncestor(node, Statement.class);
				Block surroundingBlock;
				if (ASTNode.BLOCK == ancestorStatment.getParent()
					.getNodeType()) {
					surroundingBlock = (Block) ancestorStatment.getParent();
					ListRewrite lrw = astRewrite.getListRewrite(surroundingBlock, Block.STATEMENTS_PROPERTY);
					generateCalendar(node.getAST(), CAL, expressionList)
						.forEach(s -> lrw.insertBefore(s, ancestorStatment, null));
				} else {
					Block injectionBlock = node.getAST().newBlock();
					@SuppressWarnings("unchecked")
					List<Statement> blockStatements = (List<Statement>) injectionBlock.statements();
					blockStatements.addAll(generateCalendar(node.getAST(), CAL, expressionList));
					blockStatements.add((Statement) astRewrite.createMoveTarget(ancestorStatment));
					astRewrite.replace(ancestorStatment, injectionBlock, null);
				}
				addImports.add(CALENDAR_QUALIFIED_NAME);
				logger.info("I'm a Date!"); //$NON-NLS-1$
				break;
			default:
				break;
			}
		}
		return true;
	}

	private MethodInvocation getMethodInvocation(AST ast, String nameOfCalendar) {
		return NodeBuilder.newMethodInvocation(ast, ast.newSimpleName(nameOfCalendar), ast.newSimpleName("getTime")); //$NON-NLS-1$
	}

	@SuppressWarnings("unchecked")
	private List<Statement> generateCalendar(AST ast, String nameOfCalendar, List<Expression> arguments) {
		List<Statement> statementList = new ArrayList<>();
		// Calendar cal = Calendar.getInstance(); done
		VariableDeclarationFragment variableDeclFragment = ast.newVariableDeclarationFragment();
		variableDeclFragment.setName(NodeBuilder.newSimpleName(ast, nameOfCalendar));
		variableDeclFragment.setInitializer(NodeBuilder.newMethodInvocation(ast,
				NodeBuilder.newSimpleName(ast, CALENDAR_NAME), NodeBuilder.newSimpleName(ast, "getInstance"))); //$NON-NLS-1$
		VariableDeclarationStatement variableDeclStatement = ast.newVariableDeclarationStatement(variableDeclFragment);
		Type calendarType = ast.newSimpleType(NodeBuilder.newSimpleName(ast, CALENDAR_NAME));
		variableDeclStatement.setType(calendarType);
		statementList.add(variableDeclStatement);
		// cal.set(1900 + 99, 1, 1); set arguments need to be added
		MethodInvocation setMethod = NodeBuilder.newMethodInvocation(ast,
				NodeBuilder.newSimpleName(ast, nameOfCalendar), NodeBuilder.newSimpleName(ast, "set")); //$NON-NLS-1$
		
		//Preparing the year
		Expression firstArgument = arguments.remove(0);
		if(ASTNode.NUMBER_LITERAL == firstArgument.getNodeType()) {
			try {
				Integer year = Integer.parseInt(((NumberLiteral)firstArgument).getToken());
				// Conversion to Calendar initialization
				year=year+1900;
				setMethod.arguments().add(ast.newNumberLiteral(year.toString()));
			}catch (NumberFormatException e) {
				//If number could not be parsed use the expression
				setMethod.arguments()
				.add(NodeBuilder.newInfixExpression(ast, InfixExpression.Operator.PLUS, ast.newNumberLiteral("1900"), //$NON-NLS-1$
						(Expression) astRewrite.createMoveTarget(firstArgument)));
			}
			
		}
		
		//Adding other arguments
		arguments.forEach(a -> setMethod.arguments()
			.add(astRewrite.createMoveTarget(a)));
		ExpressionStatement setStatement = NodeBuilder.newExpressionStatement(ast, setMethod);
		statementList.add(setStatement);
		return statementList;
	}
}
