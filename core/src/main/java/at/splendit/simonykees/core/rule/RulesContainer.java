package at.splendit.simonykees.core.rule;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import at.splendit.simonykees.core.rule.impl.ArithmethicAssignmentRule;
import at.splendit.simonykees.core.rule.impl.BracketsToControlRule;
import at.splendit.simonykees.core.rule.impl.CodeFormatterRule;
import at.splendit.simonykees.core.rule.impl.CollectionRemoveAllRule;
import at.splendit.simonykees.core.rule.impl.DiamondOperatorRule;
import at.splendit.simonykees.core.rule.impl.EnhancedForLoopToStreamAnyMatchRule;
import at.splendit.simonykees.core.rule.impl.EnhancedForLoopToStreamFindFirstRule;
import at.splendit.simonykees.core.rule.impl.EnhancedForLoopToStreamForEachRule;
import at.splendit.simonykees.core.rule.impl.EnhancedForLoopToStreamSumRule;
import at.splendit.simonykees.core.rule.impl.EnumsWithoutEqualsRule;
import at.splendit.simonykees.core.rule.impl.FieldNameConventionRule;
import at.splendit.simonykees.core.rule.impl.FlatMapInsteadOfNestedLoopsRule;
import at.splendit.simonykees.core.rule.impl.ForToForEachRule;
import at.splendit.simonykees.core.rule.impl.FunctionalInterfaceRule;
import at.splendit.simonykees.core.rule.impl.IndexOfToContainsRule;
import at.splendit.simonykees.core.rule.impl.InefficientConstructorRule;
import at.splendit.simonykees.core.rule.impl.LambdaForEachCollectRule;
import at.splendit.simonykees.core.rule.impl.LambdaForEachIfWrapperToFilterRule;
import at.splendit.simonykees.core.rule.impl.LambdaForEachMapRule;
import at.splendit.simonykees.core.rule.impl.LambdaToMethodReferenceRule;
import at.splendit.simonykees.core.rule.impl.MultiCatchRule;
import at.splendit.simonykees.core.rule.impl.MultiVariableDeclarationLineRule;
import at.splendit.simonykees.core.rule.impl.OrganiseImportsRule;
import at.splendit.simonykees.core.rule.impl.OverrideAnnotationRule;
import at.splendit.simonykees.core.rule.impl.PrimitiveBoxedForStringRule;
import at.splendit.simonykees.core.rule.impl.RearrangeClassMembersRule;
import at.splendit.simonykees.core.rule.impl.RemoveNewStringConstructorRule;
import at.splendit.simonykees.core.rule.impl.RemoveToStringOnStringRule;
import at.splendit.simonykees.core.rule.impl.SerialVersionUidRule;
import at.splendit.simonykees.core.rule.impl.StatementLambdaToExpressionRule;
import at.splendit.simonykees.core.rule.impl.StringConcatToPlusRule;
import at.splendit.simonykees.core.rule.impl.StringFormatLineSeparatorRule;
import at.splendit.simonykees.core.rule.impl.StringLiteralEqualityCheckRule;
import at.splendit.simonykees.core.rule.impl.StringUtilsRule;
import at.splendit.simonykees.core.rule.impl.TryWithResourceRule;
import at.splendit.simonykees.core.rule.impl.UseIsEmptyRule;
import at.splendit.simonykees.core.rule.impl.WhileToForEachRule;
import at.splendit.simonykees.core.visitor.AbstractASTRewriteASTVisitor;
import at.splendit.simonykees.core.visitor.BracketsToControlASTVisitor;
import at.splendit.simonykees.core.visitor.CollectionRemoveAllASTVisitor;
import at.splendit.simonykees.core.visitor.DiamondOperatorASTVisitor;
import at.splendit.simonykees.core.visitor.EnumsWithoutEqualsASTVisitor;
import at.splendit.simonykees.core.visitor.IndexOfToContainsASTVisitor;
import at.splendit.simonykees.core.visitor.FlatMapInsteadOfNestedLoopsASTVisitor;
import at.splendit.simonykees.core.visitor.InefficientConstructorASTVisitor;
import at.splendit.simonykees.core.visitor.LambdaToMethodReferenceASTVisitor;
import at.splendit.simonykees.core.visitor.MultiVariableDeclarationLineASTVisitor;
import at.splendit.simonykees.core.visitor.OverrideAnnotationRuleASTVisitor;
import at.splendit.simonykees.core.visitor.PrimitiveBoxedForStringASTVisitor;
import at.splendit.simonykees.core.visitor.RearrangeClassMembersASTVisitor;
import at.splendit.simonykees.core.visitor.RemoveNewStringConstructorASTVisitor;
import at.splendit.simonykees.core.visitor.RemoveToStringOnStringASTVisitor;
import at.splendit.simonykees.core.visitor.SerialVersionUidASTVisitor;
import at.splendit.simonykees.core.visitor.StatementLambdaToExpressionASTVisitor;
import at.splendit.simonykees.core.visitor.StringConcatToPlusASTVisitor;
import at.splendit.simonykees.core.visitor.StringFormatLineSeparatorASTVisitor;
import at.splendit.simonykees.core.visitor.StringLiteralEqualityCheckASTVisitor;
import at.splendit.simonykees.core.visitor.StringUtilsASTVisitor;
import at.splendit.simonykees.core.visitor.UseIsEmptyRuleASTVisitor;
import at.splendit.simonykees.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;
import at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach.EnhancedForLoopToStreamAnyMatchASTVisitor;
import at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach.EnhancedForLoopToStreamFindFirstASTVisitor;
import at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach.EnhancedForLoopToStreamForEachASTVisitor;
import at.splendit.simonykees.core.visitor.enhancedForLoopToStreamForEach.EnhancedForLoopToStreamSumASTVisitor;
import at.splendit.simonykees.core.visitor.functionalInterface.FunctionalInterfaceASTVisitor;
import at.splendit.simonykees.core.visitor.lambdaForEach.LambdaForEachCollectASTVisitor;
import at.splendit.simonykees.core.visitor.lambdaForEach.LambdaForEachIfWrapperToFilterASTVisitor;
import at.splendit.simonykees.core.visitor.lambdaForEach.LambdaForEachMapASTVisitor;
import at.splendit.simonykees.core.visitor.loop.forToForEach.ForToForEachASTVisitor;
import at.splendit.simonykees.core.visitor.loop.whileToForEach.WhileToForEachASTVisitor;
import at.splendit.simonykees.core.visitor.renaming.FieldNameConventionASTVisitor;
import at.splendit.simonykees.core.visitor.tryStatement.MultiCatchASTVisitor;
import at.splendit.simonykees.core.visitor.tryStatement.TryWithResourceASTVisitor;

/**
 * {@link RulesContainer} is a HelperClass that holds a static list of all
 * implemented rules.
 * 
 * @author Ludwig Werzowa, Martin Huter, Hannes Schweighofer, Ardit Ymeri,
 *         Hans-Jörg Schrödl
 * @since 0.9
 */
public class RulesContainer {

	private RulesContainer() {
		// hiding the default constructor
	}

	/**
	 * This {@link List} holds all implemented rules and returns them in a
	 * certain order. The execution order of each rule is determined by the
	 * position of each rule in this {@link List}.
	 * 
	 * @return a List of {@link RefactoringRule} with all used Rules is
	 *         returned.
	 */
	public static List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getAllRules() {
		return Arrays.asList(
				/*
				 * Coding conventions
				 */
				new TryWithResourceRule(TryWithResourceASTVisitor.class),
				new MultiCatchRule(MultiCatchASTVisitor.class),
				new FunctionalInterfaceRule(FunctionalInterfaceASTVisitor.class),
				new CollectionRemoveAllRule(CollectionRemoveAllASTVisitor.class),
				new DiamondOperatorRule(DiamondOperatorASTVisitor.class),
				new OverrideAnnotationRule(OverrideAnnotationRuleASTVisitor.class),
				new SerialVersionUidRule(SerialVersionUidASTVisitor.class),
				new RearrangeClassMembersRule(RearrangeClassMembersASTVisitor.class),
				new BracketsToControlRule(BracketsToControlASTVisitor.class),
				new FieldNameConventionRule(FieldNameConventionASTVisitor.class),
				new MultiVariableDeclarationLineRule(MultiVariableDeclarationLineASTVisitor.class),
				new EnumsWithoutEqualsRule(EnumsWithoutEqualsASTVisitor.class),
				/*
				 * String manipulations and arithmetic expressions
				 */
				new RemoveNewStringConstructorRule(RemoveNewStringConstructorASTVisitor.class),
				new InefficientConstructorRule(InefficientConstructorASTVisitor.class),
				new PrimitiveBoxedForStringRule(PrimitiveBoxedForStringASTVisitor.class),
				new StringFormatLineSeparatorRule(StringFormatLineSeparatorASTVisitor.class),
				new IndexOfToContainsRule(IndexOfToContainsASTVisitor.class),
				new RemoveToStringOnStringRule(RemoveToStringOnStringASTVisitor.class),
				new StringUtilsRule(StringUtilsASTVisitor.class),
				new StringLiteralEqualityCheckRule(StringLiteralEqualityCheckASTVisitor.class),
				new StringConcatToPlusRule(StringConcatToPlusASTVisitor.class),
				new UseIsEmptyRule(UseIsEmptyRuleASTVisitor.class),
				new ArithmethicAssignmentRule(ArithmethicAssignmentASTVisitor.class),

				/*
				 * Loops and streams
				 */
				new WhileToForEachRule(WhileToForEachASTVisitor.class),
				new ForToForEachRule(ForToForEachASTVisitor.class),
				new EnhancedForLoopToStreamForEachRule(EnhancedForLoopToStreamForEachASTVisitor.class),
				new LambdaForEachIfWrapperToFilterRule(LambdaForEachIfWrapperToFilterASTVisitor.class),
				new StatementLambdaToExpressionRule(StatementLambdaToExpressionASTVisitor.class),
				new LambdaForEachCollectRule(LambdaForEachCollectASTVisitor.class),
				new LambdaForEachMapRule(LambdaForEachMapASTVisitor.class),
				new FlatMapInsteadOfNestedLoopsRule(FlatMapInsteadOfNestedLoopsASTVisitor.class),
				new EnhancedForLoopToStreamAnyMatchRule(EnhancedForLoopToStreamAnyMatchASTVisitor.class),
				new EnhancedForLoopToStreamFindFirstRule(EnhancedForLoopToStreamFindFirstASTVisitor.class),
				new EnhancedForLoopToStreamSumRule(EnhancedForLoopToStreamSumASTVisitor.class),
				new LambdaToMethodReferenceRule(LambdaToMethodReferenceASTVisitor.class),

				/*
				 * Code formatting and organizing imports should always happen
				 * last.
				 */
				new CodeFormatterRule(AbstractASTRewriteASTVisitor.class),
				new OrganiseImportsRule(AbstractASTRewriteASTVisitor.class));
	}

	public static List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getRulesForProject(
			IJavaProject selectedJavaProjekt) {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> result = getAllRules();
		result.stream().forEach(rule -> rule.calculateEnabledForProject(selectedJavaProjekt));
		return result;
	}

}
