package eu.jsparrow.core.rule;

import java.lang.invoke.MethodHandles;
import java.util.Arrays;
import java.util.Collection;
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

import eu.jsparrow.core.rule.impl.*;
import eu.jsparrow.core.rule.impl.logger.StandardLoggerRule;
import eu.jsparrow.core.rule.impl.unused.RemoveUnusedLocalVariablesRule;
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
				new UseTernaryOperatorRule(),
				new RemoveUnusedLocalVariablesRule(),
				new TryWithResourceRule(), new MultiCatchRule(), new FunctionalInterfaceRule(),
				new CollectionRemoveAllRule(), new ReplaceSetRemoveAllWithForEachRule(),
				new ImmutableStaticFinalCollectionsRule(), new DiamondOperatorRule(),
				new OverrideAnnotationRule(), new SerialVersionUidRule(), new RearrangeClassMembersRule(),
				new BracketsToControlRule(), new MultiVariableDeclarationLineRule(), new EnumsWithoutEqualsRule(),
				new ReImplementingInterfaceRule(), new PutIfAbsentRule(), new MapGetOrDefaultRule(),
				new DateDeprecatedRule(), new RemoveDoubleNegationRule(), new OptionalIfPresentRule(),
				new OptionalMapRule(), new OptionalFilterRule(), new OptionalIfPresentOrElseRule(),
				new RemoveNullCheckBeforeInstanceofRule(), new GuardConditionRule(), new CollapseIfStatementsRule(),
				new RemoveExplicitCallToSuperRule(), new RemoveEmptyStatementRule(),
				new RemoveUnnecessaryThrownExceptionsRule(), new RemoveModifiersInInterfacePropertiesRule(),
				new RemoveUnusedParameterRule(), new ReorderModifiersRule(), new UseListSortRule(),
				new CollectionsFactoryMethodsRule(), new UseCollectionsSingletonListRule(),
				new HideDefaultConstructorInUtilityClassesRule(), new MakeFieldsAndVariablesFinalRule(),
				new RemoveCollectionAddAllRule(), new RemoveRedundantTypeCastRule(),
				new UseFilesBufferedReaderRule(), new UseFilesBufferedWriterRule(),
				new UsePredefinedStandardCharsetRule(),
				new UseFilesWriteStringRule(),
				new RemoveRedundantCloseRule(),
				new ReplaceRequestMappingAnnotationRule(),
				new ReplaceWrongClassForLoggerRule(),

				/*
				 * Security
				 */
				new UseParameterizedQueryRule(),
				new UseParameterizedJPAQueryRule(),
				new UseParameterizedLDAPQueryRule(),
				new EscapeUserInputsInSQLQueriesRule(),
				new ReuseRandomObjectsRule(),
				new UseSecureRandomRule(),
				new CreateTempFilesUsingJavaNIORule(),

				/*
				 * Testing
				 */
				new ReplaceJUnit3TestCasesRule(),
				new ReplaceJUnitExpectedExceptionRule(), new ReplaceJUnitExpectedAnnotationPropertyRule(),
				new ReplaceJUnitTimeoutAnnotationPropertyRule(),
				new ReplaceJUnit4CategoryWithJupiterTagRule(),
				new ReplaceJUnit4AnnotationsWithJupiterRule(),
				new ReplaceJUnit4AssertionsWithJupiterRule(),
				new UseDedicatedAssertionsRule(),
				new ReplaceJUnitAssertThatWithHamcrestRule(),
				new ReplaceJUnit4AssumptionsWithJupiterRule(),
				new ReplaceJUnit4AssumptionsWithHamcrestJUnitRule(),
				new UseDedicatedAssertJAssertionsRule(),
				new ChainAssertJAssertThatStatementsRule(),
				new ShiftAssertJDescriptionBeforeAssertionRule(),

				/*
				 * String manipulations and arithmetic expressions
				 */
				new RemoveNewStringConstructorRule(), new InefficientConstructorRule(),
				new PrimitiveBoxedForStringRule(), new StringFormatLineSeparatorRule(),
				new ReplaceStringFormatByFormattedRule(), new IndexOfToContainsRule(),
				new RemoveToStringOnStringRule(), new UseOffsetBasedStringMethodsRule(), new StringUtilsRule(),
				new StringLiteralEqualityCheckRule(),
				new StringConcatToPlusRule(), new UseIsEmptyOnCollectionsRule(), new ArithmethicAssignmentRule(),
				new StringBufferToBuilderRule(), new PrimitiveObjectUseEqualsRule(),
				new AvoidConcatenationInLoggingStatementsRule(),
				/*
				 * Loops and streams
				 */
				new WhileToForEachRule(), new ForToForEachRule(), new InsertBreakStatementInLoopsRule(),
				new EnhancedForLoopToStreamTakeWhileRule(), new EnhancedForLoopToStreamForEachRule(),
				new BufferedReaderLinesRule(), new LambdaForEachIfWrapperToFilterRule(),
				new StatementLambdaToExpressionRule(), new LambdaForEachCollectRule(), new LambdaForEachMapRule(),
				new FlatMapInsteadOfNestedLoopsRule(), new EnhancedForLoopToStreamAnyMatchRule(),
				new EnhancedForLoopToStreamFindFirstRule(), new EnhancedForLoopToStreamSumRule(),
				new StringBuildingLoopRule(), new UseComparatorMethodsRule(), new LambdaToMethodReferenceRule(),
				new UseArraysStreamRule(), new ReplaceStreamCollectByToListRule(),

				/*
				 * String manipulations. These rules must be applied after
				 * StringBuildingLoopRule.
				 */
				new UseStringBuilderAppendRule(), new UseStringJoinRule(),

				/*
				 * Code formatting and organizing imports should always happen
				 * last.
				 */
				new CodeFormatterRule()));

		if (services != null) {
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

	public static List<RefactoringRule> getRulesForProjects(Collection<IJavaProject> selectedJavaProjects,
			boolean isStandalone) {
		List<RefactoringRule> rules = getAllRules(isStandalone);
		List<RefactoringRule> result = new LinkedList<>();

		for (RefactoringRule rule : rules) {

			for (IJavaProject javaProject : selectedJavaProjects) {
				rule.calculateEnabledForProject(javaProject);
				if (!rule.isEnabled()) {
					break;
				}
			}

			result.add(rule);
		}

		return result;
	}
}
