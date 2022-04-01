package eu.jsparrow.core.rule.impl.unused;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ltk.core.refactoring.DocumentChange;

import eu.jsparrow.core.visitor.unused.UnusedClassMemberWrapper;
import eu.jsparrow.core.visitor.unused.type.RemoveUnusedTypesASTVisitor;
import eu.jsparrow.core.visitor.unused.type.UnusedTypeWrapper;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class RemoveUnusedTypesRule extends RefactoringRuleImpl<RemoveUnusedTypesASTVisitor> {

	private List<UnusedTypeWrapper> unusedTypes;

	public RemoveUnusedTypesRule(List<UnusedTypeWrapper> unusedTypes) {
		this.visitorClass = RemoveUnusedTypesASTVisitor.class;
		this.id = "RemoveUnusedTypesRule"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription("Remove Unused Types", //$NON-NLS-1$
				"Finds and removes types that are not used.", Duration.ofMinutes(2), //$NON-NLS-1$
				Arrays.asList(Tag.JAVA_1_1, Tag.READABILITY, Tag.CODING_CONVENTIONS));
		this.unusedTypes = unusedTypes;
	}
	
	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}
	
	@Override
	protected AbstractASTRewriteASTVisitor visitorFactory() {
		RemoveUnusedTypesASTVisitor visitor = new RemoveUnusedTypesASTVisitor(unusedTypes);
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}

	public Map<UnusedClassMemberWrapper, Map<ICompilationUnit, DocumentChange>> computeDocumentChangesPerType() {
		return Collections.emptyMap();
	}
	
	public void dropUnusedType(UnusedClassMemberWrapper unusedType) {
		this.unusedTypes.remove(unusedType);
	}

	public void addUnusedType(UnusedClassMemberWrapper unusedType) {
		if (unusedType instanceof UnusedTypeWrapper) {
			this.unusedTypes.add((UnusedTypeWrapper) unusedType);
		}
	}
}
