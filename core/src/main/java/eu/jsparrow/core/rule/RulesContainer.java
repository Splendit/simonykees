package eu.jsparrow.core.rule;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
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
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import eu.jsparrow.core.rule.impl.PutIfAbsentRule;
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
import eu.jsparrow.core.rule.impl.UseIsEmptyOnCollectionsRule;
import eu.jsparrow.core.rule.impl.WhileToForEachRule;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.api.RuleService;

/**
 * {@link RulesContainer} is a HelperClass that holds a static list of all
 * implemented rules.
 * 
 * @author Ludwig Werzowa, Martin Huter, Hannes Schweighofer, Ardit Ymeri,
 *         Hans-Jörg Schrödl
 * @since 0.9
 */
public class RulesContainer {

	private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup()
		.lookupClass());

	private static List<RuleService> getExternalRuleServices() {
		BundleContext bundleContext = FrameworkUtil.getBundle(RulesContainer.class)
			.getBundleContext();
		ServiceReference<?>[] serviceReferences = null;
		try {
			serviceReferences = bundleContext.getServiceReferences(RuleService.class.getName(), null);
		} catch (InvalidSyntaxException e) {
			logger.error("Failed to load external rules due to bad filter expression.", e);
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
	public static List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getAllRules(boolean isStandalone) {
		List<RuleService> services = getExternalRuleServices();

		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> rules = new LinkedList<>();
		rules.addAll(Arrays.asList(
				/*
				 * Coding conventions
				 */
				new TryWithResourceRule(), new MultiCatchRule(), new FunctionalInterfaceRule(),
				new CollectionRemoveAllRule(), new DiamondOperatorRule(), new OverrideAnnotationRule(),
				new SerialVersionUidRule(), new RearrangeClassMembersRule(), new BracketsToControlRule(),
				new FieldNameConventionRule(), new MultiVariableDeclarationLineRule(), new EnumsWithoutEqualsRule(),
				new ReImplementingInterfaceRule(), new PutIfAbsentRule(),

				new ImmutableStaticFinalCollectionsRule(),
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
				new WhileToForEachRule(), new ForToForEachRule(), new EnhancedForLoopToStreamForEachRule(),
				new LambdaForEachIfWrapperToFilterRule(), new StatementLambdaToExpressionRule(),
				new LambdaForEachCollectRule(), new LambdaForEachMapRule(), new FlatMapInsteadOfNestedLoopsRule(),
				new EnhancedForLoopToStreamAnyMatchRule(), new EnhancedForLoopToStreamFindFirstRule(),
				new EnhancedForLoopToStreamSumRule(), new StringBuildingLoopRule(), new LambdaToMethodReferenceRule(),

				/*
				 * Code formatting and organizing imports should always happen
				 * last.
				 */
				new CodeFormatterRule()));

		if (!isStandalone) {
			rules.add(new OrganiseImportsRule());
		}

		return rules;
	}

	public static List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getRulesForProject(
			IJavaProject selectedJavaProjekt, boolean isStandalone) {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> result = getAllRules(isStandalone);
		result.stream()
			.forEach(rule -> rule.calculateEnabledForProject(selectedJavaProjekt));
		return result;
	}

}
