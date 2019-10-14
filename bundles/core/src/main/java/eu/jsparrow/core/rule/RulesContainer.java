package eu.jsparrow.core.rule;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.IJavaProject;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.rule.impl.ArithmethicAssignmentRule;
import eu.jsparrow.core.rule.impl.BracketsToControlRule;
import eu.jsparrow.core.rule.impl.BufferedReaderLinesRule;
import eu.jsparrow.core.rule.impl.CodeFormatterRule;
import eu.jsparrow.core.rule.impl.CollapseIfStatementsRule;
import eu.jsparrow.core.rule.impl.CollectionRemoveAllRule;
import eu.jsparrow.core.rule.impl.CollectionsFactoryMethodsRule;
import eu.jsparrow.core.rule.impl.DateDeprecatedRule;
import eu.jsparrow.core.rule.impl.DiamondOperatorRule;
import eu.jsparrow.core.rule.impl.EnhancedForLoopToStreamAnyMatchRule;
import eu.jsparrow.core.rule.impl.EnhancedForLoopToStreamFindFirstRule;
import eu.jsparrow.core.rule.impl.EnhancedForLoopToStreamForEachRule;
import eu.jsparrow.core.rule.impl.EnhancedForLoopToStreamSumRule;
import eu.jsparrow.core.rule.impl.EnhancedForLoopToStreamTakeWhileRule;
import eu.jsparrow.core.rule.impl.EnumsWithoutEqualsRule;
import eu.jsparrow.core.rule.impl.FieldsRenamingRule;
import eu.jsparrow.core.rule.impl.FlatMapInsteadOfNestedLoopsRule;
import eu.jsparrow.core.rule.impl.ForToForEachRule;
import eu.jsparrow.core.rule.impl.FunctionalInterfaceRule;
import eu.jsparrow.core.rule.impl.GuardConditionRule;
import eu.jsparrow.core.rule.impl.ImmutableStaticFinalCollectionsRule;
import eu.jsparrow.core.rule.impl.IndexOfToContainsRule;
import eu.jsparrow.core.rule.impl.InefficientConstructorRule;
import eu.jsparrow.core.rule.impl.InsertBreakStatementInLoopsRule;
import eu.jsparrow.core.rule.impl.LambdaForEachCollectRule;
import eu.jsparrow.core.rule.impl.LambdaForEachIfWrapperToFilterRule;
import eu.jsparrow.core.rule.impl.LambdaForEachMapRule;
import eu.jsparrow.core.rule.impl.LambdaToMethodReferenceRule;
import eu.jsparrow.core.rule.impl.MapGetOrDefaultRule;
import eu.jsparrow.core.rule.impl.MultiCatchRule;
import eu.jsparrow.core.rule.impl.MultiVariableDeclarationLineRule;
import eu.jsparrow.core.rule.impl.OptionalIfPresentOrElseRule;
import eu.jsparrow.core.rule.impl.OptionalIfPresentRule;
import eu.jsparrow.core.rule.impl.OverrideAnnotationRule;
import eu.jsparrow.core.rule.impl.PrimitiveBoxedForStringRule;
import eu.jsparrow.core.rule.impl.PrimitiveObjectUseEqualsRule;
import eu.jsparrow.core.rule.impl.PutIfAbsentRule;
import eu.jsparrow.core.rule.impl.ReImplementingInterfaceRule;
import eu.jsparrow.core.rule.impl.RearrangeClassMembersRule;
import eu.jsparrow.core.rule.impl.RemoveDoubleNegationRule;
import eu.jsparrow.core.rule.impl.RemoveEmptyStatementRule;
import eu.jsparrow.core.rule.impl.RemoveExplicitCallToSuperRule;
import eu.jsparrow.core.rule.impl.RemoveModifiersInInterfacePropertiesRule;
import eu.jsparrow.core.rule.impl.RemoveNewStringConstructorRule;
import eu.jsparrow.core.rule.impl.RemoveNullCheckBeforeInstanceofRule;
import eu.jsparrow.core.rule.impl.RemoveToStringOnStringRule;
import eu.jsparrow.core.rule.impl.RemoveUnnecessaryThrownExceptionsRule;
import eu.jsparrow.core.rule.impl.RemoveUnusedParameterRule;
import eu.jsparrow.core.rule.impl.ReorderModifiersRule;
import eu.jsparrow.core.rule.impl.SerialVersionUidRule;
import eu.jsparrow.core.rule.impl.StatementLambdaToExpressionRule;
import eu.jsparrow.core.rule.impl.StringBufferToBuilderRule;
import eu.jsparrow.core.rule.impl.StringBuildingLoopRule;
import eu.jsparrow.core.rule.impl.StringConcatToPlusRule;
import eu.jsparrow.core.rule.impl.StringFormatLineSeparatorRule;
import eu.jsparrow.core.rule.impl.StringLiteralEqualityCheckRule;
import eu.jsparrow.core.rule.impl.StringUtilsRule;
import eu.jsparrow.core.rule.impl.TryWithResourceRule;
import eu.jsparrow.core.rule.impl.UseCollectionsSingletonListRule;
import eu.jsparrow.core.rule.impl.UseIsEmptyOnCollectionsRule;
import eu.jsparrow.core.rule.impl.UseListSortRule;
import eu.jsparrow.core.rule.impl.UseStringBuilderAppendRule;
import eu.jsparrow.core.rule.impl.WhileToForEachRule;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.rules.api.RuleService;
import eu.jsparrow.rules.common.RefactoringRule;

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
	}

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static List<RuleService> getExternalRuleServices() {
		BundleContext bundleContext = FrameworkUtil.getBundle(RulesContainer.class)
			.getBundleContext();
		ServiceReference<?>[] serviceReferences = null;
		try {
			serviceReferences = bundleContext.getServiceReferences(RuleService.class.getName(), null);
		} catch (InvalidSyntaxException e) {
			logger.error("Failed to load external rules due to bad filterexpression.", e); //$NON-NLS-1$
		}
		// BundleContext returns null if no services are found,
		return serviceReferences == null ? Collections.emptyList()
				: Arrays.asList(serviceReferences)
					.stream()
					.map(x -> (RuleService) bundleContext.getService(x))
					.collect(Collectors.toList());
	}

	/**
	 * This {@link List} holds all implemented rules and returns them in a
	 * certain order. The execution order of each rule is determined by the
	 * position of each rule in this {@link List}.
	 * 
	 * @return a List of {@link RefactoringRule} with all used Rules is
	 *         returned.
	 */
	public static List<RefactoringRule> getAllRules(boolean isStandalone) {
		List<RuleService> services = getExternalRuleServices();

		List<RefactoringRule> rules = new LinkedList<>();

		rules.addAll(Arrays.asList(

				/*
				 * Coding conventions
				 */
				new TryWithResourceRule(), new MultiCatchRule(), new FunctionalInterfaceRule(),
				new CollectionRemoveAllRule(), new ImmutableStaticFinalCollectionsRule(), new DiamondOperatorRule(),
				new OverrideAnnotationRule(), new SerialVersionUidRule(), new RearrangeClassMembersRule(),
				new BracketsToControlRule(), new MultiVariableDeclarationLineRule(), new EnumsWithoutEqualsRule(),
				new ReImplementingInterfaceRule(), new PutIfAbsentRule(), new MapGetOrDefaultRule(),
				new DateDeprecatedRule(), new RemoveDoubleNegationRule(), new OptionalIfPresentRule(),
				new OptionalIfPresentOrElseRule(), new RemoveNullCheckBeforeInstanceofRule(), new GuardConditionRule(),
				new CollapseIfStatementsRule(), new RemoveExplicitCallToSuperRule(), new RemoveEmptyStatementRule(),
				new RemoveUnnecessaryThrownExceptionsRule(), new RemoveModifiersInInterfacePropertiesRule(),
				new RemoveUnusedParameterRule(), new ReorderModifiersRule(), new UseListSortRule(),
				new CollectionsFactoryMethodsRule(), new UseCollectionsSingletonListRule(),

				/*
				 * String manipulations and arithmetic expressions
				 */
				new RemoveNewStringConstructorRule(), new InefficientConstructorRule(),
				new PrimitiveBoxedForStringRule(), new StringFormatLineSeparatorRule(), new IndexOfToContainsRule(),
				new RemoveToStringOnStringRule(), new StringUtilsRule(), new StringLiteralEqualityCheckRule(),
				new StringConcatToPlusRule(), new UseIsEmptyOnCollectionsRule(), new ArithmethicAssignmentRule(),
				new StringBufferToBuilderRule(), new PrimitiveObjectUseEqualsRule(),
				/*
				 * Loops and streams
				 */
				new WhileToForEachRule(), new ForToForEachRule(), new InsertBreakStatementInLoopsRule(),
				new EnhancedForLoopToStreamTakeWhileRule(), new EnhancedForLoopToStreamForEachRule(),
				new BufferedReaderLinesRule(), new LambdaForEachIfWrapperToFilterRule(),
				new StatementLambdaToExpressionRule(), new LambdaForEachCollectRule(), new LambdaForEachMapRule(),
				new FlatMapInsteadOfNestedLoopsRule(), new EnhancedForLoopToStreamAnyMatchRule(),
				new EnhancedForLoopToStreamFindFirstRule(), new EnhancedForLoopToStreamSumRule(),
				new StringBuildingLoopRule(), new LambdaToMethodReferenceRule(),

				/*
				 * String manipulations. This rule must be applied after
				 * StringBuildingLoopRule.
				 */
				new UseStringBuilderAppendRule(),

				/*
				 * Code formatting and organizing imports should always happen
				 * last.
				 */
				new CodeFormatterRule()));

		if (!isStandalone && services != null) {
			services.stream()
				.forEach(service -> rules.addAll(service.loadRules()));
		}

		return rules;
	}

	/**
	 * 
	 * @return the list of all rules that require user configuration, e.g.
	 *         {@link FieldsRenamingRule}, {@link StandardLoggerRule}.
	 *         <em>NOTE:</em> No initialization data is provided in the
	 *         instances of this list.
	 */
	public static List<RefactoringRule> getAllSemiAutomaticRules() {
		List<RefactoringRule> semiautomaticRules = new LinkedList<>();

		semiautomaticRules.addAll(Arrays.asList(new StandardLoggerRule(),
				new FieldsRenamingRule(Collections.emptyList(), Collections.emptyList())));

		return Collections.unmodifiableList(semiautomaticRules);
	}

	public static List<RefactoringRule> getRulesForProject(IJavaProject selectedJavaProjekt, boolean isStandalone) {
		List<RefactoringRule> result = getAllRules(isStandalone);
		result.stream()
			.forEach(rule -> rule.calculateEnabledForProject(selectedJavaProjekt));
		return result;
	}

}
