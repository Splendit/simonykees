package eu.jsparrow.core.markers.visitor;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.Type;

import eu.jsparrow.core.markers.RefactoringEventImpl;
import eu.jsparrow.core.markers.common.Resolver;
import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.DiamondOperatorRule;
import eu.jsparrow.core.visitor.impl.DiamondOperatorASTVisitor;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;

/**
 * A visitor for resolving one issue of type {@link DiamondOperatorASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public class DiamondOperatorResolver extends DiamondOperatorASTVisitor implements Resolver {

	public static final String ID = "DiamondOperatorResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private IJavaElement javaElement;
	private RuleDescription description;

	public DiamondOperatorResolver(Predicate<ASTNode> positionChecker) {
		super(JavaCore.VERSION_1_8);
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(DiamondOperatorRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		javaElement = compilationUnit.getJavaElement();
		return super.visit(compilationUnit);
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		if (this.positionChecker.test(node)) {
			super.visit(node);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(ParameterizedType parameterizedType, List<Type> rhsTypeArguments) {
		int credit = description.getCredit();

		AST ast = parameterizedType.getAST();
		ParameterizedType typeCopy = (ParameterizedType) ASTNode.copySubtree(ast, parameterizedType);
		@SuppressWarnings("unchecked")
		List<Type> typeArguments = typeCopy.typeArguments();
		typeArguments.clear();
		int highlightLength = 0;
		RefactoringMarkerEvent event = new RefactoringEventImpl(ID,
				description.getName(),
				description.getDescription(),
				javaElement,
				highlightLength,
				parameterizedType.getParent(), typeCopy, credit);
		addMarkerEvent(event);

	}
}
