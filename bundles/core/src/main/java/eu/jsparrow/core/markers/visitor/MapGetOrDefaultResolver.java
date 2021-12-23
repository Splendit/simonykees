package eu.jsparrow.core.markers.visitor;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.MapGetOrDefaultRule;
import eu.jsparrow.core.visitor.impl.MapGetOrDefaultASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link MapGetOrDefaultASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public class MapGetOrDefaultResolver extends MapGetOrDefaultASTVisitor implements Resolver {
	public static final String ID = "MapGetOrDefaultResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public MapGetOrDefaultResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(MapGetOrDefaultRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (positionChecker.test(methodInvocation)) {
			super.visit(methodInvocation);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(MethodInvocation methodInvocation, Expression key, Expression defaultValue) {
		AST ast = methodInvocation.getAST();
		MethodInvocation getOrDefault = ast.newMethodInvocation();
		getOrDefault.setExpression((Expression) ASTNode.copySubtree(ast, methodInvocation.getExpression()));
		getOrDefault.setName(ast.newSimpleName("getOrDefault")); //$NON-NLS-1$

		Expression keyCopy = (Expression) ASTNode.copySubtree(ast, key);
		Expression defaultCopy = (Expression) ASTNode.copySubtree(ast, defaultValue);
		@SuppressWarnings("unchecked")
		List<Expression> getOrDefaultArgumetns = getOrDefault.arguments();
		getOrDefaultArgumetns.add(keyCopy);
		getOrDefaultArgumetns.add(defaultCopy);

		int credit = description.getCredit();
		int highlightLength = getOrDefault.getLength();
		int offset = methodInvocation.getStartPosition();
		int length = methodInvocation.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(methodInvocation.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(getOrDefault.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}
}
