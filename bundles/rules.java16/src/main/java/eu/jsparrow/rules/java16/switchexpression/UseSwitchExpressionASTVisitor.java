package eu.jsparrow.rules.java16.switchexpression;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;

import eu.jsparrow.rules.common.util.ASTNodeUtil;
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
public class UseSwitchExpressionASTVisitor extends AbstractReplaceBySwitchASTVisitor
		implements UseSwitchExpressionEvent {

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
		Runnable lambdaForRefactoring = createLambdaForRefactoring(switchStatement, switchHeaderExpression, clauses);
		lambdaForRefactoring.run();
		addMarkerEvent(switchStatement);
		onRewrite();
		CommentRewriter commentRewriter = getCommentRewriter();
		commentRewriter.saveLeadingComment(switchStatement);
		return true;
	}

	private boolean hasLabeledSwitchCase(SwitchStatement switchStatement) {
		return ASTNodeUtil.convertToTypedList(switchStatement.statements(), SwitchCase.class)
			.stream()
			.anyMatch(SwitchCase::isSwitchLabeledRule);
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
