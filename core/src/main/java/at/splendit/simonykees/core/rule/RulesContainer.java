package at.splendit.simonykees.core.rule;

import java.util.Arrays;
import java.util.List;

import at.splendit.simonykees.core.rule.impl.ArithmethicAssignmentRule;
import at.splendit.simonykees.core.rule.impl.BracketsToControlRule;
import at.splendit.simonykees.core.rule.impl.CodeFormatterRule;
import at.splendit.simonykees.core.rule.impl.CollectionRemoveAllRule;
import at.splendit.simonykees.core.rule.impl.ForToForEachRule;
import at.splendit.simonykees.core.rule.impl.FunctionalInterfaceRule;
import at.splendit.simonykees.core.rule.impl.MultiCatchRule;
import at.splendit.simonykees.core.rule.impl.OrganiseImportsRule;
import at.splendit.simonykees.core.rule.impl.PrimitiveBoxedForStringRule;
import at.splendit.simonykees.core.rule.impl.RemoveNewStringConstructorRule;
import at.splendit.simonykees.core.rule.impl.RemoveToStringOnStringRule;
import at.splendit.simonykees.core.rule.impl.SerialVersionUidRule;
import at.splendit.simonykees.core.rule.impl.StringFormatLineSeperatorRule;
import at.splendit.simonykees.core.rule.impl.StringUtilsRule;
import at.splendit.simonykees.core.rule.impl.TryWithResourceRule;
import at.splendit.simonykees.core.rule.impl.WhileToForRule;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.core.visitor.BracketsToControlASTVisitor;
import at.splendit.simonykees.core.visitor.CollectionRemoveAllASTVisitor;
import at.splendit.simonykees.core.visitor.FunctionalInterfaceASTVisitor;
import at.splendit.simonykees.core.visitor.PrimitiveBoxedForStringASTVisitor;
import at.splendit.simonykees.core.visitor.RemoveNewStringConstructorASTVisitor;
import at.splendit.simonykees.core.visitor.RemoveToStringOnStringASTVisitor;
import at.splendit.simonykees.core.visitor.SerialVersionUidASTVisitor;
import at.splendit.simonykees.core.visitor.StringFormatLineSeperatorASTVisitor;
import at.splendit.simonykees.core.visitor.StringUtilsASTVisitor;
import at.splendit.simonykees.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;
import at.splendit.simonykees.core.visitor.loop.ForToForEachASTVisitor;
import at.splendit.simonykees.core.visitor.loop.WhileToForASTVisitor;
import at.splendit.simonykees.core.visitor.tryStatement.MultiCatchASTVisitor;
import at.splendit.simonykees.core.visitor.tryStatement.TryWithResourceASTVisitor;

/**
 * {@link RulesContainer} is a HelperClass that holds a static list of all
 * implemented rules
 * 
 * @author Ludwig Werzowa, Martin Huter, Hannes Schweighofer
 * @since 0.9
 */
public class RulesContainer {

	private RulesContainer() {
		// hiding the default constructor
	}

	/**
	 * Is a static List of all implemented rules
	 * 
	 * @return a List of {@link RefactoringRule} with all used Rules is
	 *         returned.
	 */
	public static List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getAllRules() {
		return Arrays.asList(new ArithmethicAssignmentRule(ArithmethicAssignmentASTVisitor.class),
				new TryWithResourceRule(TryWithResourceASTVisitor.class),
				new StringUtilsRule(StringUtilsASTVisitor.class),
				new MultiCatchRule(MultiCatchASTVisitor.class),
				new BracketsToControlRule(BracketsToControlASTVisitor.class),
				new FunctionalInterfaceRule(FunctionalInterfaceASTVisitor.class),
				new WhileToForRule(WhileToForASTVisitor.class),
				new ForToForEachRule(ForToForEachASTVisitor.class),
				new CollectionRemoveAllRule(CollectionRemoveAllASTVisitor.class),
				new SerialVersionUidRule(SerialVersionUidASTVisitor.class),
				new StringFormatLineSeperatorRule(StringFormatLineSeperatorASTVisitor.class),
				new RemoveToStringOnStringRule(RemoveToStringOnStringASTVisitor.class),
				new RemoveNewStringConstructorRule(RemoveNewStringConstructorASTVisitor.class),
				new PrimitiveBoxedForStringRule(PrimitiveBoxedForStringASTVisitor.class),
				new CodeFormatterRule(AbstractASTRewriteASTVisitor.class),
				new OrganiseImportsRule(AbstractASTRewriteASTVisitor.class));
	}

}
