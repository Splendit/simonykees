package eu.jsparrow.core.rule.impl;

import java.time.Duration;

import org.apache.commons.lang3.JavaVersion;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.core.exception.RefactoringException;
import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RuleDescription;
import eu.jsparrow.core.util.TagUtil;
import eu.jsparrow.core.visitor.trycatch.TryWithResourceASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * @see TryWithResourceASTVisitor
 * 
 * @author Martin Huter
 * @since 0.9
 *
 */
public class TryWithResourceRule extends RefactoringRule<TryWithResourceASTVisitor> {

	public TryWithResourceRule() {
		super();
		this.visitorClass = TryWithResourceASTVisitor.class;
	}

	@Override
	protected JavaVersion provideRequiredJavaVersion() {
		return JavaVersion.JAVA_1_7;
	}
	
	
	@Override
	public RuleDescription getRuleDescription() {
		return new RuleDescription(Messages.TryWithResourceRule_name, Messages.TryWithResourceRule_description,
				Duration.ofMinutes(15), TagUtil.getTagsForRule(this.getClass()));
	}

	@Override
	protected DocumentChange applyRuleImpl(ICompilationUnit workingCopy)
			throws ReflectiveOperationException, JavaModelException, RefactoringException {

		/*
		 * The TryWithResourceRule has to be applied twice.
		 * 
		 * See: FIXME SIM-396: Make all changes of the TryWithResourceRule
		 * visible
		 */
		DocumentChange tmp1 = super.applyRuleImpl(workingCopy);
		DocumentChange tmp2 = super.applyRuleImpl(workingCopy);

		return null == tmp2 ? tmp1 : tmp2;
	}

}
