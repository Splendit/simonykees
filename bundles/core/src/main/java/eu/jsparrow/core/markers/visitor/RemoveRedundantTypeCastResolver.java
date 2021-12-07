package eu.jsparrow.core.markers.visitor;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CompilationUnit;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.RemoveRedundantTypeCastRule;
import eu.jsparrow.core.visitor.impl.RemoveRedundantTypeCastASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

public class RemoveRedundantTypeCastResolver extends RemoveRedundantTypeCastASTVisitor {

	public static final String ID = RemoveRedundantTypeCastResolver.class.getName();

	private Predicate<ASTNode> positionChecker;
	private IJavaElement javaElement;
	private RuleDescription description;

	public RemoveRedundantTypeCastResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(RemoveRedundantTypeCastRule.RULE_ID);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(CastExpression castExpression) {
		if (positionChecker.test(castExpression)) {
			super.visit(castExpression);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(ASTNode node, ASTNode toBeReplaced) {
		int credit = description.getCredit();
		AST ast = node.getAST();
		ASTNode newNode = ASTNode.copySubtree(ast, toBeReplaced);
		int highlightLength = newNode.getLength();
		RefactoringMarkerEvent event = new RefactoringEventImpl(ID,
				description.getName(),
				description.getDescription(),
				javaElement,
				highlightLength,
				node, newNode, credit);
		addMarkerEvent(event);
	}
}
