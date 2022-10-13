package eu.jsparrow.rules.java16.switchexpression;

import java.util.List;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public abstract class AbstractReplaceBySwitchASTVisitor extends AbstractASTRewriteASTVisitor {

	protected boolean containsDefaultClause(List<? extends SwitchCaseClause> clauses) {
		return clauses.stream()
			.anyMatch(clause -> clause.getExpressions()
				.isEmpty());
	}
}
