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

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.DiamondOperatorRule;
import eu.jsparrow.core.visitor.impl.DiamondOperatorASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type {@link DiamondOperatorASTVisitor}.
 * 
 * @since 4.6.0
 *
 */
public class DiamondOperatorResolver extends DiamondOperatorASTVisitor implements Resolver {

	public static final String ID = "DiamondOperatorResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
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
		ASTNode original = parameterizedType.getParent();
		int offset = original.getStartPosition();
		int length = original.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(parameterizedType.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(typeCopy.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);

	}
}
