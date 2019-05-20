package eu.jsparrow.rules.java10.factory.methods;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class CollectionsFactoryMethodsRule extends RefactoringRuleImpl<CollectionsFactoryMethodsASTVisitor> {
	
	public CollectionsFactoryMethodsRule() {
		this.visitorClass = CollectionsFactoryMethodsASTVisitor.class;
		this.id = "CollectionsFactoryMethods"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(
				"Use Factory Methods for Collections",  //$NON-NLS-1$
				"Replace Collections.unmodifiable with factory methods for collections.",  //$NON-NLS-1$
				Duration.ofMinutes(5), 
				Arrays.asList(Tag.JAVA_1_9, Tag.OLD_LANGUAGE_CONSTRUCTS, Tag.READABILITY));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_9;
	}

}
