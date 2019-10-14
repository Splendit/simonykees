package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.optional.OptionalIfPresentOrElseASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class OptionalIfPresentOrElseRule extends RefactoringRuleImpl<OptionalIfPresentOrElseASTVisitor>{
	
	public OptionalIfPresentOrElseRule() {
		this.visitorClass = OptionalIfPresentOrElseASTVisitor.class;
		this.id = "OptionalIfPresentOrElse"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Use Optional::ifPresentOrElse",
				"The usage of  Optional.get should be avoided in general because it can potentially throw a \n" + 
				" NoSuchElementException (it is likely to be deprecated in future releases).  It is often the case that \n" + 
				" the invocation of Optional.get is wrapped by a condition that uses  Optional.isPresent. Such cases \n" + 
				" can be replaced with the Optional.ifPresent(Consumer<? super T> consumer).", Duration.ofMinutes(2),
				Arrays.asList(Tag.JAVA_1_8, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.LAMBDA));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return  JavaCore.VERSION_9;
	}

}
