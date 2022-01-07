package eu.jsparrow.core.markers.visitor.loop;

import java.util.function.Predicate;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;

import eu.jsparrow.core.rule.RuleDescriptionFactory;
import eu.jsparrow.core.rule.impl.StringBuildingLoopRule;
import eu.jsparrow.core.visitor.impl.StringBuildingLoopASTVisitor;
import eu.jsparrow.rules.common.RefactoringEventImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.Resolver;

/**
 * A visitor for resolving one issue of type
 * {@link StringBuildingLoopASTVisitor}.
 * 
 * @since 4.7.0
 *
 */
public class StringBuildingLoopResolver extends StringBuildingLoopASTVisitor implements Resolver {

	public static final String ID = "StringBuildingLoopResolver"; //$NON-NLS-1$
	private static final String DEFAULT_JAVA_VERSION = "1.5"; //$NON-NLS-1$

	private Predicate<ASTNode> positionChecker;
	private RuleDescription description;

	public StringBuildingLoopResolver(Predicate<ASTNode> positionChecker) {
		super(DEFAULT_JAVA_VERSION);
		this.positionChecker = positionChecker;
		this.description = RuleDescriptionFactory
			.findByRuleId(StringBuildingLoopRule.RULE_ID);
	}

	@Override
	public RuleDescription getDescription() {
		return this.description;
	}

	@Override 
	public boolean visit(CompilationUnit compilationUnit) {
		IJavaElement javaElement = compilationUnit.getJavaElement();
		IJavaProject javaProject = javaElement.getJavaProject();
		String javaVersion = javaProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		super.updateJavaVersion(javaVersion);
		return super.visit(compilationUnit);
	}
	@Override
	public boolean visit(EnhancedForStatement forStatement) {
		if (positionChecker.test(forStatement)) {
			super.visit(forStatement);
		}
		return false;
	}

	@Override
	public void addMarkerEvent(EnhancedForStatement loop) {
		int credit = description.getCredit();
		int highlightLength = 0;
		int offset = loop.getStartPosition();
		int length = loop.getLength();
		CompilationUnit cu = getCompilationUnit();
		int lineNumber = cu.getLineNumber(loop.getStartPosition());
		IJavaElement javaElement = cu.getJavaElement();
		RefactoringMarkerEvent event = new RefactoringEventImpl.Builder()
			.withResolver(ID)
			.withName(description.getName())
			.withMessage(description.getDescription())
			.withIJavaElement(javaElement)
			.withHighlightLength(highlightLength)
			.withOffset(offset)
			.withCodePreview(description.getDescription())
			.withLength(length)
			.withWeightValue(credit)
			.withLineNumber(lineNumber)
			.build();
		addMarkerEvent(event);
	}
}
