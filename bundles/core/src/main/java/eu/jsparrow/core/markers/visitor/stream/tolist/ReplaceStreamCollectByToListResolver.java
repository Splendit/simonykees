package eu.jsparrow.core.markers.visitor.stream.tolist;

import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.ReplaceStreamCollectByToListRule;
import eu.jsparrow.core.visitor.stream.tolist.ReplaceStreamCollectByToListASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;
import eu.jsparrow.rules.common.util.ASTNodeUtil;

/**
 * A visitor for resolving one issue of type
 * {@link ReplaceStreamCollectByToListASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public class ReplaceStreamCollectByToListResolver extends ReplaceStreamCollectByToListASTVisitor implements Resolver {

	public static final String ID = "ReplaceStreamCollectByToListResolver"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public ReplaceStreamCollectByToListResolver(Predicate<ASTNode> positionChecker) {
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(ReplaceStreamCollectByToListRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		StructuralPropertyDescriptor loc = methodInvocation.getLocationInParent();
		if (loc != MethodInvocation.ARGUMENTS_PROPERTY) {
			return true;
		}

		MethodInvocation parent = (MethodInvocation) methodInvocation.getParent();
		SimpleName name = parent.getName();

		if (positionChecker.test(name)) {
			super.visit(methodInvocation);
		}
		return true;
	}

	@Override
	public void addMarkerEvent(MethodInvocation methodInvocation) {
		AST ast = methodInvocation.getAST();
		MethodInvocation streamToList = ast.newMethodInvocation();
		streamToList.setName(ast.newSimpleName("toList")); //$NON-NLS-1$
		List<Expression> arguments = ASTNodeUtil.convertToTypedList(methodInvocation.arguments(), Expression.class);
		SimpleName name = methodInvocation.getName();
		Expression collectorsToList = arguments.get(0);
		int credit = description.getCredit();
		int highlightLength = 0;
		int offset = name.getStartPosition();
		int length = collectorsToList.getLength() + (collectorsToList.getStartPosition() - name.getStartPosition());
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(collectorsToList.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();

		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(streamToList.toString())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}
}
