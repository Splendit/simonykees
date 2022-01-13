package eu.jsparrow.rules.java16.switchexpression;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTMatcher;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.YieldStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.common.visitor.helper.CommentRewriter;
import eu.jsparrow.rules.common.visitor.helper.LocalVariableUsagesVisitor;
import eu.jsparrow.rules.common.visitor.helper.VariableDeclarationsVisitor;

/**
 * A visitor for replacing {@link SwitchStatement}s by {@link SwitchExpression}s
 * or rule-labeled {@link SwitchStatement}s. For example, the following code:
 * 
 * 
 * <pre>
 * <code>
 * 	String value = "test";
 *	switch (digit) {
 *	case 1:
 *		value = "one";
 *		break;
 *	case 2:
 *		value = "two";
 *		break;
 *	default:
 *		value = "none";
 *	}
 * </code>
 * </pre>
 * 
 * is transformed to:
 * 
 * <pre>
 * <code> 
 *	String value = switch (digit) {
 *	case 1 -> "one";
 *	case 2 -> "two";
 *	default -> "none";
 *	};
 * </code>
 * </pre>
 * 
 * 
 * @since 4.3.0
 *
 */
public class UseSwitchExpressionASTVisitor extends AbstractASTRewriteASTVisitor implements UseSwitchExpressionEvent {

	@Override
	public boolean visit(SwitchStatement switchStatement) {
		List<Statement> statements = ASTNodeUtil.convertToTypedList(switchStatement.statements(), Statement.class);
		if (hasLabeledSwitchCase(switchStatement)) {
			return true;
		}
		List<List<Statement>> switchCaseBucks = splitIntoSwitchCaseBucks(statements);
		if (!areTransformableBucks(switchCaseBucks)) {
			return true;
		}

		List<SwitchCaseClause> clauses = createClauses(switchCaseBucks);
		Expression switchHeaderExpression = switchStatement.getExpression();
		AST ast = switchStatement.getAST();
		boolean hasDefaultClause = hasDefaultClause(switchStatement);
		if (!hasDefaultClause) {
			SwitchStatement newSwitchStatement = createSwitchStatement(ast, switchHeaderExpression, clauses);
			astRewrite.replace(switchStatement, newSwitchStatement, null);
			addMarkerEvent(switchStatement);
			onRewrite();
			return true;
		}

		if (areAssigningValue(clauses)) {
			clauses.get(0)
				.findAssignedVariable()
				.ifPresent(assigned -> replaceBySwitchAssigningValue(assigned, switchStatement, clauses));
		} else if (areReturningValue(clauses)) {
			SwitchExpression newSwitchExpression = createSwitchWithYieldValue(ast, switchHeaderExpression, clauses);
			ReturnStatement newReturnStatement = ast.newReturnStatement();
			newReturnStatement.setExpression(newSwitchExpression);
			astRewrite.replace(switchStatement, newReturnStatement, null);
			addMarkerEvent(switchStatement);
			onRewrite();
		} else {
			SwitchStatement newSwitchStatement = createSwitchStatement(ast, switchHeaderExpression, clauses);
			astRewrite.replace(switchStatement, newSwitchStatement, null);
			addMarkerEvent(switchStatement);
			
			onRewrite();
		}
		CommentRewriter commentRewriter = getCommentRewriter();
		commentRewriter.saveLeadingComment(switchStatement);
		return true;
	}

	private boolean hasLabeledSwitchCase(SwitchStatement switchStatement) {
		return ASTNodeUtil.convertToTypedList(switchStatement.statements(), SwitchCase.class)
			.stream()
			.anyMatch(SwitchCase::isSwitchLabeledRule);
	}

	private void replaceBySwitchAssigningValue(Expression assigned, SwitchStatement switchStatement,
			List<SwitchCaseClause> clauses) {
		AST ast = switchStatement.getAST();
		Expression switchHeaderExpression = switchStatement.getExpression();
		VariableDeclarationFragment fragment = findDeclaringFragment(assigned, switchStatement).orElse(null);
		if (fragment != null) {
			SwitchExpression newSwitchExpression = createSwitchWithYieldValue(ast, switchHeaderExpression, clauses);
			astRewrite.set(fragment, VariableDeclarationFragment.INITIALIZER_PROPERTY, newSwitchExpression, null);
			astRewrite.remove(switchStatement, null);
		} else {
			Assignment assignment = ast.newAssignment();
			assignment.setLeftHandSide((Expression) astRewrite.createCopyTarget(assigned));
			SwitchExpression newSwitchExpression = createSwitchWithYieldValue(ast, switchHeaderExpression, clauses);
			assignment.setRightHandSide(newSwitchExpression);
			ExpressionStatement newAssignmentStatement = ast.newExpressionStatement(assignment);
			astRewrite.replace(switchStatement, newAssignmentStatement, null);
		}
		onRewrite();
	}

	private boolean hasDefaultClause(SwitchStatement switchStatement) {
		List<Statement> statements = ASTNodeUtil.convertToTypedList(switchStatement.statements(), Statement.class);
		List<SwitchCase> switchCaseStatements = filterSwitchCaseStatements(statements);
		return switchCaseStatements.stream()
			.anyMatch(SwitchCase::isDefault);
	}

	private Optional<VariableDeclarationFragment> findDeclaringFragment(Expression assigned,
			SwitchStatement switchStatement) {
		CompilationUnit compilationUnit = getCompilationUnit();
		if (assigned.getNodeType() != ASTNode.SIMPLE_NAME) {
			return Optional.empty();
		}
		SimpleName simpleName = (SimpleName) assigned;

		IBinding binding = simpleName.resolveBinding();
		ASTNode declaringNode = compilationUnit.findDeclaringNode(binding);
		if (declaringNode == null) {
			return Optional.empty();
		}
		int declaringNodeType = declaringNode.getNodeType();
		if (declaringNodeType != ASTNode.VARIABLE_DECLARATION_FRAGMENT) {
			return Optional.empty();
		}
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) declaringNode;
		if (fragment.getLocationInParent() != VariableDeclarationStatement.FRAGMENTS_PROPERTY) {
			return Optional.empty();
		}
		VariableDeclarationStatement declaraingStatement = (VariableDeclarationStatement) fragment.getParent();
		if (declaraingStatement.fragments()
			.size() != 1) {
			return Optional.empty();
		}
		boolean areStatementsInBlock = declaraingStatement.getLocationInParent() == Block.STATEMENTS_PROPERTY
				&& switchStatement.getLocationInParent() == Block.STATEMENTS_PROPERTY;
		if (!areStatementsInBlock) {
			return Optional.empty();
		}
		Block declarationParent = (Block) declaraingStatement.getParent();
		Block switchParent = (Block) switchStatement.getParent();
		if (switchParent != declarationParent) {
			return Optional.empty();
		}
		List<Statement> blockStatements = ASTNodeUtil.convertToTypedList(declarationParent.statements(),
				Statement.class);
		int declarationIndex = blockStatements.indexOf(declaraingStatement);
		int switchIndex = blockStatements.indexOf(switchStatement);

		if (switchIndex != declarationIndex + 1) {
			return Optional.empty();
		}

		Expression initializer = fragment.getInitializer();
		if (initializer != null && initializer.getNodeType() != ASTNode.SIMPLE_NAME
				&& !ASTNodeUtil.isLiteral(initializer)) {
			return Optional.empty();
		}

		return Optional.of(fragment);
	}

	private boolean areReturningValue(List<SwitchCaseClause> clauses) {

		boolean isReturningValue = clauses.stream()
			.allMatch(SwitchCaseClause::isReturningValue);
		if (!isReturningValue) {
			return false;
		}
		return clauses.stream()
			.map(SwitchCaseClause::getStatements)
			.noneMatch(this::containsMultipleReturnStatements);
	}

	private boolean areAssigningValue(List<SwitchCaseClause> clauses) {
		List<Expression> assignedExpressions = clauses.stream()
			.map(SwitchCaseClause::findAssignedVariable)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		if (assignedExpressions.size() != clauses.size()) {
			return false;
		}

		boolean hasInternalBreakStatements = clauses.stream()
			.anyMatch(SwitchCaseClause::hasInternalBreakStatements);
		if (hasInternalBreakStatements) {
			return false;
		}

		Expression firstAssigned = assignedExpressions.get(0);
		ASTMatcher matcher = new ASTMatcher();
		for (int i = 1; i < assignedExpressions.size(); i++) {
			Expression assigned = assignedExpressions.get(i);
			if (!firstAssigned.subtreeMatch(matcher, assigned)) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private SwitchStatement createSwitchStatement(AST ast, Expression switchHeaderExpression,
			List<SwitchCaseClause> clauses) {
		SwitchStatement newSwitchStatement = ast.newSwitchStatement();
		Expression newHeaderExpression = (Expression) astRewrite.createCopyTarget(switchHeaderExpression);
		newSwitchStatement.setExpression(newHeaderExpression);
		List<Statement> statements = newSwitchStatement.statements();
		for (SwitchCaseClause clause : clauses) {
			SwitchCase switchCase = replaceWithLabeledRuleSwitchCase(ast, clause);
			statements.add(switchCase);

			List<Statement> clauseStatements = clause.getStatements();
			if (clauseStatements.size() == 1) {
				Statement clauseStatement = clauseStatements.get(0);
				if (clauseStatement.getNodeType() == ASTNode.EXPRESSION_STATEMENT
						|| clauseStatement.getNodeType() == ASTNode.THROW_STATEMENT) {
					Statement newExpStatement = (Statement) astRewrite
						.createCopyTarget(clauseStatement);
					statements.add(newExpStatement);
				} else {
					Block block = ast.newBlock();
					Statement newStatement = (Statement) astRewrite.createCopyTarget(clauseStatement);
					block.statements()
						.add(newStatement);
					statements.add(block);
				}
			} else {
				Block block = ast.newBlock();
				for (Statement clauseStatement : clauseStatements) {
					Statement newStatement = (Statement) astRewrite.createCopyTarget(clauseStatement);
					block.statements()
						.add(newStatement);
				}
				statements.add(block);
			}
		}
		return newSwitchStatement;
	}

	@SuppressWarnings("unchecked")
	private SwitchCase replaceWithLabeledRuleSwitchCase(AST ast, SwitchCaseClause clause) {
		List<Expression> clauseExpressions = clause.getExpressions();
		SwitchCase switchCase = ast.newSwitchCase();
		switchCase.setSwitchLabeledRule(true);
		for (Expression expression : clauseExpressions) {
			Expression unwrappedExpression = ASTNodeUtil.unwrapParenthesizedExpression(expression);
			switchCase.expressions()
				.add((Expression) astRewrite.createCopyTarget(unwrappedExpression));
		}
		return switchCase;
	}

	@SuppressWarnings("unchecked")
	private SwitchExpression createSwitchWithYieldValue(AST ast, Expression switchHeaderExpression,
			List<SwitchCaseClause> clauses) {
		SwitchExpression newSwitchStatement = ast.newSwitchExpression();
		Expression newHeaderExpression = (Expression) astRewrite.createCopyTarget(switchHeaderExpression);
		newSwitchStatement.setExpression(newHeaderExpression);
		List<Statement> statements = newSwitchStatement.statements();
		for (SwitchCaseClause clause : clauses) {
			SwitchCase switchCase = replaceWithLabeledRuleSwitchCase(ast, clause);
			statements.add(switchCase);
			List<Statement> clauseStatements = clause.getStatements();
			Expression yieldExpression = clause.findYieldExpression();
			ThrowStatement throwStatement = clause.findThrowsStatement()
				.orElse(null);

			if (clauseStatements.size() == 1) {
				Statement yieldStatement;
				if (throwStatement != null) {
					yieldStatement = (Statement) astRewrite.createCopyTarget(throwStatement);
				} else {
					yieldStatement = ast
						.newExpressionStatement((Expression) astRewrite.createCopyTarget(yieldExpression));
				}
				statements.add(yieldStatement);
			} else {
				Block block = ast.newBlock();
				for (int i = 0; i < clauseStatements.size() - 1; i++) {
					Statement clauseStatement = clauseStatements.get(i);
					Statement newStatement = (Statement) astRewrite.createCopyTarget(clauseStatement);
					block.statements()
						.add(newStatement);
				}
				YieldStatement yieldStatement = ast.newYieldStatement();
				yieldStatement.setExpression((Expression) astRewrite.createCopyTarget(yieldExpression));
				block.statements()
					.add(yieldStatement);
				statements.add(block);
			}
		}
		return newSwitchStatement;
	}

	private boolean areTransformableBucks(List<List<Statement>> switchCaseBucks) {
		if (switchCaseBucks.isEmpty()) {
			return false;
		}
		List<SimpleName> undefinedVariables = new ArrayList<>();
		for (List<Statement> buck : switchCaseBucks) {
			if (containsNonConsecutiveSwitchCases(buck)) {
				return false;
			}

			if (containsLabeledStatement(buck)) {
				return false;
			}

			if (combinesSwitchCaseWithDefault(buck)) {
				return false;
			}

			if (usesUndefinedVariables(buck, undefinedVariables)) {
				return false;
			}
			undefinedVariables.addAll(findVariableDeclarations(buck));
		}
		return true;
	}

	private boolean combinesSwitchCaseWithDefault(List<Statement> buck) {
		List<SwitchCase> switchCases = filterSwitchCaseStatements(buck);
		return switchCases.size() > 1 && switchCases.stream()
			.anyMatch(SwitchCase::isDefault);
	}

	private boolean containsLabeledStatement(List<Statement> buck) {
		LabeledBreakStatementsVisitor visitor = new LabeledBreakStatementsVisitor();
		for (Statement statement : buck) {
			statement.accept(visitor);
			if (visitor.containsLabeledStatements()) {
				return true;
			}
		}
		return false;
	}

	private List<SimpleName> findVariableDeclarations(List<Statement> buck) {
		VariableDeclarationsVisitor visitor = new VariableDeclarationsVisitor();
		List<SimpleName> allDeclaredVariables = new ArrayList<>();
		for (Statement statement : buck) {
			if (statement.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT
					&& statement.getLocationInParent() == SwitchStatement.STATEMENTS_PROPERTY) {
				statement.accept(visitor);
				List<SimpleName> declaredVariables = visitor.getVariableDeclarationNames();
				allDeclaredVariables.addAll(declaredVariables);
			}
		}
		return allDeclaredVariables;
	}

	private boolean usesUndefinedVariables(List<Statement> buck, List<SimpleName> undefinedVariables) {
		for (SimpleName variableName : undefinedVariables) {
			LocalVariableUsagesVisitor visitor = new LocalVariableUsagesVisitor(variableName);
			for (Statement statement : buck) {
				statement.accept(visitor);
			}
			if (!visitor.getUsages()
				.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	private List<SwitchCase> filterSwitchCaseStatements(List<Statement> buck) {
		return buck.stream()
			.filter(node -> node.getNodeType() == ASTNode.SWITCH_CASE)
			.map(SwitchCase.class::cast)
			.collect(Collectors.toList());
	}

	private List<BreakStatement> findAllBreakStatements(List<Statement> buck) {
		SwitchCaseBreakStatementsVisitor visitor = new SwitchCaseBreakStatementsVisitor();
		for (Statement statement : buck) {
			statement.accept(visitor);
		}

		return visitor.getBreakStatements();
	}

	private boolean containsMultipleReturnStatements(List<Statement> buck) {
		SwitchCaseReturnStatementsVisitor visitor = new SwitchCaseReturnStatementsVisitor();
		for (Statement statement : buck) {
			statement.accept(visitor);
			if (visitor.hasMultipleReturnStatements()) {
				return true;
			}
		}
		return false;
	}

	private boolean containsNonConsecutiveSwitchCases(List<Statement> buck) {
		List<Integer> caseIndexes = new ArrayList<>();
		for (int i = 0; i < buck.size(); i++) {
			Statement statement = buck.get(i);
			if (statement.getNodeType() == ASTNode.SWITCH_CASE) {
				caseIndexes.add(i);
			}
		}
		if (!caseIndexes.contains(0)) {
			return true;
		}
		for (int index : caseIndexes) {
			if (index != 0 && !caseIndexes.contains(index - 1)) {
				return true;
			}
		}
		return false;
	}

	private List<List<Statement>> splitIntoSwitchCaseBucks(List<Statement> statements) {

		List<Statement> flatterned = new ArrayList<>();
		for (Statement statement : statements) {
			if (statement.getNodeType() == ASTNode.BLOCK) {
				Block block = (Block) statement;
				List<Statement> blockStatements = ASTNodeUtil.convertToTypedList(block.statements(), Statement.class);
				flatterned.addAll(blockStatements);
			} else {
				flatterned.add(statement);
			}
		}

		List<List<Statement>> switchCaseBucks = new ArrayList<>();
		List<Integer> breakIndexes = new ArrayList<>();
		for (int i = 0; i < flatterned.size(); i++) {
			Statement statement = flatterned.get(i);
			if (statement.getNodeType() == ASTNode.BREAK_STATEMENT
					|| statement.getNodeType() == ASTNode.RETURN_STATEMENT) {
				breakIndexes.add(i);
			}
		}
		List<Statement> buck = new ArrayList<>();
		for (int i = 0; i < flatterned.size(); i++) {
			buck.add(flatterned.get(i));
			if (breakIndexes.contains(i)) {
				switchCaseBucks.add(buck);
				buck = new ArrayList<>();
			}
		}
		if (!buck.isEmpty()) {
			switchCaseBucks.add(buck);
		}
		return switchCaseBucks;
	}

	private List<SwitchCaseClause> createClauses(List<List<Statement>> switchCaseBucks) {
		List<SwitchCaseClause> clauses = new ArrayList<>();
		for (List<Statement> buck : switchCaseBucks) {
			SwitchCaseClause clause = createClause(buck);
			clauses.add(clause);
		}
		return clauses;
	}

	private SwitchCaseClause createClause(List<Statement> buck) {
		List<SwitchCase> switchCases = filterSwitchCaseStatements(buck);
		@SuppressWarnings("unchecked")
		List<Expression> caseExpressions = switchCases.stream()
			.flatMap(node -> ((List<Expression>) node.expressions()).stream())
			.collect(Collectors.toList());
		List<Statement> blockStatements = buck.stream()
			.filter(node -> node.getNodeType() != ASTNode.SWITCH_CASE)
			.filter(node -> node.getNodeType() != ASTNode.BREAK_STATEMENT)
			.collect(Collectors.toList());
		List<BreakStatement> breakStatements = findAllBreakStatements(buck);
		return new SwitchCaseClause(caseExpressions, blockStatements, breakStatements);
	}

}
