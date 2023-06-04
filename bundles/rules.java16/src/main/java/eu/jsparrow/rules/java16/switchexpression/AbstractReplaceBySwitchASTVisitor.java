package eu.jsparrow.rules.java16.switchexpression;

import java.util.Collections;
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
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.YieldStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
import eu.jsparrow.rules.common.util.VariableDeclarationBeforeStatement;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public abstract class AbstractReplaceBySwitchASTVisitor extends AbstractASTRewriteASTVisitor {

	private final ASTMatcher matcher = new ASTMatcher();

	protected Runnable createLambdaForRefactoring(Statement statementToReplace, Expression switchHeaderExpression,
			List<? extends SwitchCaseClause> clauses) {

		boolean hasDefaultClause = containsDefaultClause(clauses);
		if (hasDefaultClause) {
			Expression variableToAssignSwitchExpression = findVariableToAssignSwitchExpression(clauses)
				.orElse(null);

			if (variableToAssignSwitchExpression != null) {
				CompilationUnit compilationUnit = getCompilationUnit();
				VariableDeclarationFragment fragment = VariableDeclarationBeforeStatement
					.findDeclaringFragment(variableToAssignSwitchExpression, statementToReplace, compilationUnit)
					.orElse(null);

				if (fragment != null) {
					return () -> replaceByInitializationWithSwitch(statementToReplace, switchHeaderExpression, clauses,
							fragment);
				}

				return () -> replaceByAssignmentWithSwitch(variableToAssignSwitchExpression, statementToReplace,
						switchHeaderExpression, clauses);
			}

			if (areReturningValue(clauses)) {
				return () -> replaceByReturnWithSwitch(statementToReplace, switchHeaderExpression, clauses);
			}
		}
		return () -> replaceBySwitchStatement(statementToReplace, switchHeaderExpression, clauses);
	}

	protected boolean containsDefaultClause(List<? extends SwitchCaseClause> clauses) {
		return clauses.stream()
			.anyMatch(clause -> clause.getExpressions()
				.isEmpty());
	}

	protected Optional<Expression> findVariableToAssignSwitchExpression(List<? extends SwitchCaseClause> clauses) {

		if (hasReturnOrInternalBreak(clauses)) {
			return Optional.empty();
		}

		List<Expression> allAssignedVariables = findAssignmentLeftHandSideExpressions(clauses);
		if (allAssignedVariables.isEmpty()) {
			return Optional.empty();
		}

		List<Expression> subsequentAssignedVariables = allAssignedVariables.subList(1,
				allAssignedVariables.size());

		return Optional.of(allAssignedVariables.get(0))
			.filter(variable -> canAssignSwitchExpression(variable, subsequentAssignedVariables, clauses));

	}

	private boolean hasReturnOrInternalBreak(List<? extends SwitchCaseClause> clauses) {
		boolean hasInternalBreakStatement = clauses.stream()
			.anyMatch(SwitchCaseClause::hasInternalBreakStatements);
		if (hasInternalBreakStatement) {
			return true;
		}

		SwitchCaseReturnStatementsVisitor returnStatementVisitor = new SwitchCaseReturnStatementsVisitor();
		for (SwitchCaseClause clause : clauses) {
			boolean hasAnyReturnStatement = clause.getStatements()
				.stream()
				.anyMatch(statement -> {
					statement.accept(returnStatementVisitor);
					return returnStatementVisitor.hasAnyReturnStatement();
				});
			if (hasAnyReturnStatement) {
				return true;
			}
		}
		return false;
	}

	private List<Expression> findAssignmentLeftHandSideExpressions(List<? extends SwitchCaseClause> clauses) {
		List<Expression> assignedVariableNames = clauses.stream()
			.map(SwitchCaseClause::findAssignedVariable)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.collect(Collectors.toList());

		if (assignedVariableNames.size() != clauses.size()) {
			return Collections.emptyList();
		}

		return assignedVariableNames;
	}

	private boolean canAssignSwitchExpression(Expression firstAssignedVariable,
			List<Expression> subsequentAssignedVariables, List<? extends SwitchCaseClause> clauses) {

		if (isThisOrSuperFieldAccess(firstAssignedVariable)) {
			return areAllSubsequentVariablesMatching(firstAssignedVariable, subsequentAssignedVariables);
		}

		boolean allClausesContainingAssignmentAsOnlyStatement = clauses.stream()
			.map(SwitchCaseClause::getStatements)
			.map(List::size)
			.allMatch(size -> size == 1);
		if (allClausesContainingAssignmentAsOnlyStatement) {
			return VariableWithoutSideEffect.isVariableWithoutSideEffect(firstAssignedVariable)
					&& areAllSubsequentVariablesMatching(firstAssignedVariable, subsequentAssignedVariables);
		}

		if (firstAssignedVariable.getNodeType() != ASTNode.SIMPLE_NAME) {
			return false;
		}
		SimpleName simpleName = (SimpleName) firstAssignedVariable;
		return canAssignSwitchExpression(simpleName, subsequentAssignedVariables);

	}

	private boolean canAssignSwitchExpression(SimpleName firstAssignedVariable,
			List<Expression> subsequentAssignedVariables) {

		ASTNode expectedDeclaringNode = findNodeDeclaringVariable(firstAssignedVariable, getCompilationUnit())
			.orElse(null);
		if (expectedDeclaringNode == null) {
			return false;
		}

		for (Expression subsequentAssignedVariable : subsequentAssignedVariables) {
			if (subsequentAssignedVariable.getNodeType() != ASTNode.SIMPLE_NAME) {
				return false;
			}
			SimpleName simpleName = (SimpleName) subsequentAssignedVariable;
			if (!matcher.match(firstAssignedVariable, simpleName)) {
				return false;
			}

			if (expectedDeclaringNode != findNodeDeclaringVariable(simpleName, getCompilationUnit()).orElse(null)) {
				return false;
			}
		}
		return true;
	}

	private boolean isThisOrSuperFieldAccess(Expression firstAssignedVariable) {
		if (firstAssignedVariable.getNodeType() == ASTNode.SUPER_FIELD_ACCESS) {
			return true;
		}

		if (firstAssignedVariable.getNodeType() == ASTNode.FIELD_ACCESS) {
			FieldAccess fieldAccess = (FieldAccess) firstAssignedVariable;
			return fieldAccess.getExpression()
				.getNodeType() == ASTNode.THIS_EXPRESSION;
		}
		return false;
	}

	private boolean areAllSubsequentVariablesMatching(Expression firstAssignedVariable,
			List<Expression> subsequentAssignedVariables) {

		return subsequentAssignedVariables.stream()
			.allMatch(variable -> firstAssignedVariable.subtreeMatch(matcher, variable));
	}

	Optional<ASTNode> findNodeDeclaringVariable(SimpleName firstAssignedVariable, CompilationUnit compilationUnit) {
		IBinding binding = firstAssignedVariable.resolveBinding();
		if (binding == null) {
			return Optional.empty();
		}
		if (binding.getKind() != IBinding.VARIABLE) {
			return Optional.empty();
		}
		IVariableBinding variableBinding = (IVariableBinding) binding;
		return Optional.ofNullable(compilationUnit.findDeclaringNode(variableBinding));
	}

	protected boolean areReturningValue(List<? extends SwitchCaseClause> clauses) {

		boolean isReturningValue = clauses.stream()
			.allMatch(SwitchCaseClause::isReturningValue);
		if (!isReturningValue) {
			return false;
		}
		return clauses.stream()
			.map(SwitchCaseClause::getStatements)
			.noneMatch(this::containsMultipleReturnStatements);
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

	protected void replaceByInitializationWithSwitch(Statement statementToReplace, Expression switchHeaderExpression,
			List<? extends SwitchCaseClause> clauses, VariableDeclarationFragment fragment) {
		AST ast = statementToReplace.getAST();
		SwitchExpression newSwitchExpression = createSwitchWithYieldValue(ast, switchHeaderExpression, clauses);
		astRewrite.set(fragment, VariableDeclarationFragment.INITIALIZER_PROPERTY, newSwitchExpression, null);
		astRewrite.remove(statementToReplace, null);
	}

	protected void replaceByAssignmentWithSwitch(Expression assigned, Statement statementToReplace,
			Expression switchHeaderExpression, List<? extends SwitchCaseClause> clauses) {
		AST ast = statementToReplace.getAST();
		Assignment assignment = ast.newAssignment();
		assignment.setLeftHandSide((Expression) astRewrite.createCopyTarget(assigned));
		SwitchExpression newSwitchExpression = createSwitchWithYieldValue(ast, switchHeaderExpression, clauses);
		assignment.setRightHandSide(newSwitchExpression);
		ExpressionStatement newAssignmentStatement = ast.newExpressionStatement(assignment);
		astRewrite.replace(statementToReplace, newAssignmentStatement, null);
	}

	protected void replaceByReturnWithSwitch(Statement statementToReplace,
			Expression switchHeaderExpression, List<? extends SwitchCaseClause> clauses) {
		AST ast = statementToReplace.getAST();
		SwitchExpression newSwitchExpression = createSwitchWithYieldValue(ast, switchHeaderExpression, clauses);
		ReturnStatement newReturnStatement = ast.newReturnStatement();
		newReturnStatement.setExpression(newSwitchExpression);
		astRewrite.replace(statementToReplace, newReturnStatement, null);
	}

	protected void replaceBySwitchStatement(Statement statementToReplace, Expression switchHeaderExpression,
			List<? extends SwitchCaseClause> clauses) {
		AST ast = statementToReplace.getAST();
		SwitchStatement newSwitchStatement = createSwitchStatement(ast, switchHeaderExpression, clauses);
		astRewrite.replace(statementToReplace, newSwitchStatement, null);
	}

	@SuppressWarnings("unchecked")
	private SwitchExpression createSwitchWithYieldValue(AST ast, Expression switchHeaderExpression,
			List<? extends SwitchCaseClause> clauses) {
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
	private SwitchStatement createSwitchStatement(AST ast, Expression switchHeaderExpression,
			List<? extends SwitchCaseClause> clauses) {
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
}
