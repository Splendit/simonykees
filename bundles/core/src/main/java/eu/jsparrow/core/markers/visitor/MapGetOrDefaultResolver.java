package eu.jsparrow.core.markers.visitor;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.MapGetOrDefaultRule;
import eu.jsparrow.core.visitor.impl.MapGetOrDefaultASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

public class MapGetOrDefaultResolver extends MapGetOrDefaultASTVisitor {
	public static final String ID = MapGetOrDefaultResolver.class.getName();

	private Predicate<ASTNode> positionChecker;
	private IJavaElement javaElement;
	private RuleDescription description;

	public MapGetOrDefaultResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(MapGetOrDefaultRule.RULE_ID);
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
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
		RefactoringMarkerEvent event = new RefactoringEventImpl(ID,
				description.getName(),
				description.getDescription(),
				javaElement,
				highlightLength,
				methodInvocation, getOrDefault, credit);
		addMarkerEvent(event);
	}
}
