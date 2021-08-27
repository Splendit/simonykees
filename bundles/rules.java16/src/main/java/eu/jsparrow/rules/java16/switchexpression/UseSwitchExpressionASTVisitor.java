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
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.YieldStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class UseSwitchExpressionASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(SwitchStatement switchStatement) {
		List<Statement> statements = ASTNodeUtil.convertToTypedList(switchStatement.statements(), Statement.class);
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
			onRewrite();
		} else {
			SwitchStatement newSwitchStatement = createSwitchStatement(ast, switchHeaderExpression, clauses);
			astRewrite.replace(switchStatement, newSwitchStatement, null);
			onRewrite();
		}
		return true;
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
				&& initializer.getNodeType() != ASTNode.STRING_LITERAL
				&& initializer.getNodeType() != ASTNode.NUMBER_LITERAL
				&& initializer.getNodeType() != ASTNode.BOOLEAN_LITERAL
				&& initializer.getNodeType() != ASTNode.CHARACTER_LITERAL
				&& initializer.getNodeType() != ASTNode.NULL_LITERAL
				&& initializer.getNodeType() != ASTNode.TYPE_LITERAL) {
			return Optional.empty();
		}

		return Optional.of(fragment);
	}

	private boolean areReturningValue(List<SwitchCaseClause> clauses) {
		return clauses.stream()
			.map(SwitchCaseClause::findReturnedValue)
			.allMatch(Optional::isPresent);
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
			List<Expression> clauseExpressions = clause.getExpressions();
			SwitchCase switchCase = ast.newSwitchCase();
			switchCase.setSwitchLabeledRule(true);
			for (Expression expression : clauseExpressions) {
				switchCase.expressions()
					.add((Expression) astRewrite.createCopyTarget(expression));
			}
			statements.add(switchCase);

			List<Statement> clauseStatements = clause.getStatements();
			if (clauseStatements.size() == 1) {
				Statement clauseStatement = clauseStatements.get(0);
				if (clauseStatement.getNodeType() == ASTNode.EXPRESSION_STATEMENT) {
					ExpressionStatement newExpStatement = (ExpressionStatement) astRewrite
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
	private SwitchExpression createSwitchWithYieldValue(AST ast, Expression switchHeaderExpression,
			List<SwitchCaseClause> clauses) {
		SwitchExpression newSwitchStatement = ast.newSwitchExpression();
		Expression newHeaderExpression = (Expression) astRewrite.createCopyTarget(switchHeaderExpression);
		newSwitchStatement.setExpression(newHeaderExpression);
		List<Statement> statements = newSwitchStatement.statements();
		for (SwitchCaseClause clause : clauses) {
			List<Expression> clauseExpressions = clause.getExpressions();
			SwitchCase switchCase = ast.newSwitchCase();
			switchCase.setSwitchLabeledRule(true);
			for (Expression expression : clauseExpressions) {
				switchCase.expressions()
					.add((Expression) astRewrite.createCopyTarget(expression));
			}
			statements.add(switchCase);
			List<Statement> clauseStatements = clause.getStatements();
			Expression yieldExpression = clause.findYieldExpression();
			if (clauseStatements.size() == 1) {
				ExpressionStatement yieldStatement = ast
					.newExpressionStatement((Expression) astRewrite.createCopyTarget(yieldExpression));
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
		for (List<Statement> buck : switchCaseBucks) {
			if (containsNonConsecutiveSwitchCases(buck)) {
				return false;
			}

			if (containsMultipleBreakStatements(buck)) {
				return false;
			}

			if (containsSwitchCaseAndDefaultStatement(buck)) {
				return false;
			}
		}
		return true;
	}

	private boolean containsSwitchCaseAndDefaultStatement(List<Statement> buck) {
		List<SwitchCase> switchCases = filterSwitchCaseStatements(buck);
		if (switchCases.isEmpty()) {
			return false;
		}
		SwitchCase caseStatement = switchCases.get(0);
		boolean isDefault = caseStatement.isDefault();
		return switchCases.size() > 1 && switchCases.stream()
			.anyMatch(node -> node.isDefault() != isDefault);
	}

	private List<SwitchCase> filterSwitchCaseStatements(List<Statement> buck) {
		return buck.stream()
			.filter(node -> node.getNodeType() == ASTNode.SWITCH_CASE)
			.map(SwitchCase.class::cast)
			.collect(Collectors.toList());
	}

	private boolean containsMultipleBreakStatements(List<Statement> buck) {
		for (Statement statement : buck) {
			SwitchCaseBreakStatementsVisitor visitor = new SwitchCaseBreakStatementsVisitor();
			statement.accept(visitor);
			if (visitor.hasBreakStatements()) {
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
		List<List<Statement>> switchCaseBucks = new ArrayList<>();
		List<Integer> breakIndexes = new ArrayList<>();
		for (int i = 0; i < statements.size(); i++) {
			Statement statement = statements.get(i);
			if (statement.getNodeType() == ASTNode.BREAK_STATEMENT
					|| statement.getNodeType() == ASTNode.RETURN_STATEMENT) {
				breakIndexes.add(i);
			}
		}
		List<Statement> buck = new ArrayList<>();
		for (int i = 0; i < statements.size(); i++) {
			buck.add(statements.get(i));
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
		Statement breakStatement = buck.get(buck.size() - 1);
		return new SwitchCaseClause(caseExpressions, blockStatements, breakStatement);
	}

}
