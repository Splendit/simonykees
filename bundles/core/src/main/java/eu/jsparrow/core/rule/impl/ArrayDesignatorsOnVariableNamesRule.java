package eu.jsparrow.core.rule.impl;

import java.time.Duration;
import java.util.Arrays;

import org.eclipse.jdt.core.JavaCore;

import eu.jsparrow.core.visitor.impl.extradimensions.ArrayDesignatorsOnVariableNamesASTVisitor;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

/**
 * @see IterateMapEntrySetASTVisitor
 * 
 * @since 4.20.0
 */
public class ArrayDesignatorsOnVariableNamesRule extends RefactoringRuleImpl<ArrayDesignatorsOnVariableNamesASTVisitor> {

	public static final String RULE_ID = "ArrayDesignatorsOnVariables"; //$NON-NLS-1$

	@SuppressWarnings("nls")
	public ArrayDesignatorsOnVariableNamesRule() {
		super();
		this.visitorClass = ArrayDesignatorsOnVariableNamesASTVisitor.class;
		this.id = RULE_ID;
		this.ruleDescription = new RuleDescription("Array Designators on Variables", ""
				+ "According to the rule S1197 on the web site 'sonarcloud.io', array designators '[]' should not be on the variable but should be on the type."
				+ " This rule looks for variable declarations with such 'extra dimensions' on the variable name and moves them to the type,"
				+ " thus making the array type of a veriable mor explicit.",
				Duration.ofMinutes(5),
				Arrays.asList(Tag.JAVA_1_1, Tag.CODING_CONVENTIONS));

	}

	// array designators "[]" should be on the type, not the variable
	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_1;
	}

}
