package eu.jsparrow.core.rule;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;

import eu.jsparrow.core.rule.impl.ArithmethicAssignmentRule;
import eu.jsparrow.core.rule.impl.BracketsToControlRule;
import eu.jsparrow.core.rule.impl.CodeFormatterRule;
import eu.jsparrow.core.rule.impl.CollectionRemoveAllRule;
import eu.jsparrow.core.rule.impl.DiamondOperatorRule;
import eu.jsparrow.core.rule.impl.EnhancedForLoopToStreamAnyMatchRule;
import eu.jsparrow.core.rule.impl.EnhancedForLoopToStreamFindFirstRule;
import eu.jsparrow.core.rule.impl.EnhancedForLoopToStreamForEachRule;
import eu.jsparrow.core.rule.impl.EnhancedForLoopToStreamSumRule;
import eu.jsparrow.core.rule.impl.EnumsWithoutEqualsRule;
import eu.jsparrow.core.rule.impl.FieldNameConventionRule;
import eu.jsparrow.core.rule.impl.FlatMapInsteadOfNestedLoopsRule;
import eu.jsparrow.core.rule.impl.ForToForEachRule;
import eu.jsparrow.core.rule.impl.FunctionalInterfaceRule;
import eu.jsparrow.core.rule.impl.ImmutableStaticFinalCollectionsRule;
import eu.jsparrow.core.rule.impl.IndexOfToContainsRule;
import eu.jsparrow.core.rule.impl.InefficientConstructorRule;
import eu.jsparrow.core.rule.impl.LambdaForEachCollectRule;
import eu.jsparrow.core.rule.impl.LambdaForEachIfWrapperToFilterRule;
import eu.jsparrow.core.rule.impl.LambdaForEachMapRule;
import eu.jsparrow.core.rule.impl.LambdaToMethodReferenceRule;
import eu.jsparrow.core.rule.impl.MultiCatchRule;
import eu.jsparrow.core.rule.impl.MultiVariableDeclarationLineRule;
import eu.jsparrow.core.rule.impl.OrganiseImportsRule;
import eu.jsparrow.core.rule.impl.OverrideAnnotationRule;
import eu.jsparrow.core.rule.impl.PrimitiveBoxedForStringRule;
import eu.jsparrow.core.rule.impl.PrimitiveObjectUseEqualsRule;
import eu.jsparrow.core.rule.impl.ReImplementingInterfaceRule;
import eu.jsparrow.core.rule.impl.RearrangeClassMembersRule;
import eu.jsparrow.core.rule.impl.RemoveNewStringConstructorRule;
import eu.jsparrow.core.rule.impl.RemoveToStringOnStringRule;
import eu.jsparrow.core.rule.impl.SerialVersionUidRule;
import eu.jsparrow.core.rule.impl.StatementLambdaToExpressionRule;
import eu.jsparrow.core.rule.impl.StringBufferToBuilderRule;
import eu.jsparrow.core.rule.impl.StringBuildingLoopRule;
import eu.jsparrow.core.rule.impl.StringConcatToPlusRule;
import eu.jsparrow.core.rule.impl.StringFormatLineSeparatorRule;
import eu.jsparrow.core.rule.impl.StringLiteralEqualityCheckRule;
import eu.jsparrow.core.rule.impl.StringUtilsRule;
import eu.jsparrow.core.rule.impl.TryWithResourceRule;
import eu.jsparrow.core.rule.impl.UseIsEmptyRule;
import eu.jsparrow.core.rule.impl.WhileToForEachRule;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.core.visitor.BracketsToControlASTVisitor;
import eu.jsparrow.core.visitor.CollectionRemoveAllASTVisitor;
import eu.jsparrow.core.visitor.DiamondOperatorASTVisitor;
import eu.jsparrow.core.visitor.EnumsWithoutEqualsASTVisitor;
import eu.jsparrow.core.visitor.FlatMapInsteadOfNestedLoopsASTVisitor;
import eu.jsparrow.core.visitor.ImmutableStaticFinalCollectionsASTVisitor;
import eu.jsparrow.core.visitor.IndexOfToContainsASTVisitor;
import eu.jsparrow.core.visitor.InefficientConstructorASTVisitor;
import eu.jsparrow.core.visitor.LambdaToMethodReferenceASTVisitor;
import eu.jsparrow.core.visitor.MultiVariableDeclarationLineASTVisitor;
import eu.jsparrow.core.visitor.OverrideAnnotationRuleASTVisitor;
import eu.jsparrow.core.visitor.PrimitiveBoxedForStringASTVisitor;
import eu.jsparrow.core.visitor.PrimitiveObjectUseEqualsASTVisitor;
import eu.jsparrow.core.visitor.ReImplementingInterfaceASTVisitor;
import eu.jsparrow.core.visitor.RearrangeClassMembersASTVisitor;
import eu.jsparrow.core.visitor.RemoveNewStringConstructorASTVisitor;
import eu.jsparrow.core.visitor.RemoveToStringOnStringASTVisitor;
import eu.jsparrow.core.visitor.SerialVersionUidASTVisitor;
import eu.jsparrow.core.visitor.StatementLambdaToExpressionASTVisitor;
import eu.jsparrow.core.visitor.StringBufferToBuilderASTVisitor;
import eu.jsparrow.core.visitor.StringBuildingLoopASTVisitor;
import eu.jsparrow.core.visitor.StringConcatToPlusASTVisitor;
import eu.jsparrow.core.visitor.StringFormatLineSeparatorASTVisitor;
import eu.jsparrow.core.visitor.StringLiteralEqualityCheckASTVisitor;
import eu.jsparrow.core.visitor.StringUtilsASTVisitor;
import eu.jsparrow.core.visitor.UseIsEmptyRuleASTVisitor;
import eu.jsparrow.core.visitor.arithmetic.ArithmethicAssignmentASTVisitor;
import eu.jsparrow.core.visitor.functionalinterface.FunctionalInterfaceASTVisitor;
import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachCollectASTVisitor;
import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachIfWrapperToFilterASTVisitor;
import eu.jsparrow.core.visitor.lambdaforeach.LambdaForEachMapASTVisitor;
import eu.jsparrow.core.visitor.loop.fortoforeach.ForToForEachASTVisitor;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamAnyMatchASTVisitor;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamFindFirstASTVisitor;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamForEachASTVisitor;
import eu.jsparrow.core.visitor.loop.stream.EnhancedForLoopToStreamSumASTVisitor;
import eu.jsparrow.core.visitor.loop.whiletoforeach.WhileToForEachASTVisitor;
import eu.jsparrow.core.visitor.renaming.FieldNameConventionASTVisitor;
import eu.jsparrow.core.visitor.trycatch.MultiCatchASTVisitor;
import eu.jsparrow.core.visitor.trycatch.TryWithResourceASTVisitor;

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
				new ReImplementingInterfaceRule(ReImplementingInterfaceASTVisitor.class),

				new ImmutableStaticFinalCollectionsRule(ImmutableStaticFinalCollectionsASTVisitor.class),
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
				new StringBufferToBuilderRule(StringBufferToBuilderASTVisitor.class),
				new PrimitiveObjectUseEqualsRule(PrimitiveObjectUseEqualsASTVisitor.class),
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
				new StringBuildingLoopRule(StringBuildingLoopASTVisitor.class),
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
