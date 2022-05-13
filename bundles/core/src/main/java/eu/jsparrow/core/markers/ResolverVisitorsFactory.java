package eu.jsparrow.core.markers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.markers.visitor.AvoidConcatenationInLoggingStatementsResolver;
import eu.jsparrow.core.markers.visitor.BracketsToControlResolver;
import eu.jsparrow.core.markers.visitor.CollapseIfStatementsResolver;
import eu.jsparrow.core.markers.visitor.CollectionRemoveAllResolver;
import eu.jsparrow.core.markers.visitor.DateDeprecatedResolver;
import eu.jsparrow.core.markers.visitor.DiamondOperatorResolver;
import eu.jsparrow.core.markers.visitor.EnumsWithoutEqualsResolver;
import eu.jsparrow.core.markers.visitor.FlatMapInsteadOfNestedLoopsResolver;
import eu.jsparrow.core.markers.visitor.FunctionalInterfaceResolver;
import eu.jsparrow.core.markers.visitor.GuardConditionResolver;
import eu.jsparrow.core.markers.visitor.ImmutableStaticFinalCollectionsResolver;
import eu.jsparrow.core.markers.visitor.IndexOfToContainsResolver;
import eu.jsparrow.core.markers.visitor.InefficientConstructorResolver;
import eu.jsparrow.core.markers.visitor.InsertBreakStatementInLoopsResolver;
import eu.jsparrow.core.markers.visitor.LambdaToMethodReferenceResolver;
import eu.jsparrow.core.markers.visitor.MakeFieldsAndVariablesFinalResolver;
import eu.jsparrow.core.markers.visitor.MapGetOrDefaultResolver;
import eu.jsparrow.core.markers.visitor.MultiVariableDeclarationLineResolver;
import eu.jsparrow.core.markers.visitor.OverrideAnnotationResolver;
import eu.jsparrow.core.markers.visitor.PrimitiveBoxedForStringResolver;
import eu.jsparrow.core.markers.visitor.PrimitiveObjectUseEqualsResolver;
import eu.jsparrow.core.markers.visitor.PutIfAbsentResolver;
import eu.jsparrow.core.markers.visitor.ReImplementingInterfaceResolver;
import eu.jsparrow.core.markers.visitor.RemoveCollectionAddAllResolver;
import eu.jsparrow.core.markers.visitor.RemoveDoubleNegationResolver;
import eu.jsparrow.core.markers.visitor.RemoveEmptyStatementResolver;
import eu.jsparrow.core.markers.visitor.RemoveExplicitCallToSuperResolver;
import eu.jsparrow.core.markers.visitor.RemoveModifiersInInterfacePropertiesResolver;
import eu.jsparrow.core.markers.visitor.RemoveNewStringConstructorResolver;
import eu.jsparrow.core.markers.visitor.RemoveNullCheckBeforeInstanceofResolver;
import eu.jsparrow.core.markers.visitor.RemoveRedundantTypeCastResolver;
import eu.jsparrow.core.markers.visitor.RemoveToStringOnStringResolver;
import eu.jsparrow.core.markers.visitor.RemoveUnnecessaryThrownExceptionsResolver;
import eu.jsparrow.core.markers.visitor.RemoveUnusedParameterResolver;
import eu.jsparrow.core.markers.visitor.ReorderModifiersResolver;
import eu.jsparrow.core.markers.visitor.ReplaceStringFormatByFormattedResolver;
import eu.jsparrow.core.markers.visitor.StatementLambdaToExpressionResolver;
import eu.jsparrow.core.markers.visitor.StringBufferToBuilderResolver;
import eu.jsparrow.core.markers.visitor.StringFormatLineSeparatorResolver;
import eu.jsparrow.core.markers.visitor.StringLiteralEqualityCheckResolver;
import eu.jsparrow.core.markers.visitor.StringUtilsResolver;
import eu.jsparrow.core.markers.visitor.UseArraysStreamResolver;
import eu.jsparrow.core.markers.visitor.UseCollectionsSingletonListResolver;
import eu.jsparrow.core.markers.visitor.UseComparatorMethodsResolver;
import eu.jsparrow.core.markers.visitor.UseIsEmptyOnCollectionsResolver;
import eu.jsparrow.core.markers.visitor.UseListSortResolver;
import eu.jsparrow.core.markers.visitor.UseOffsetBasedStringMethodsResolver;
import eu.jsparrow.core.markers.visitor.UsePredefinedStandardCharsetResolver;
import eu.jsparrow.core.markers.visitor.UseStringJoinResolver;
import eu.jsparrow.core.markers.visitor.arithmetic.ArithmeticAssignmentResolver;
import eu.jsparrow.core.markers.visitor.factory.methods.CollectionsFactoryMethodsResolver;
import eu.jsparrow.core.markers.visitor.files.UseFilesBufferedReaderResolver;
import eu.jsparrow.core.markers.visitor.files.UseFilesBufferedWriterResolver;
import eu.jsparrow.core.markers.visitor.files.writestring.UseFilesWriteStringResolver;
import eu.jsparrow.core.markers.visitor.lambdaforeach.LambdaForEachCollectResolver;
import eu.jsparrow.core.markers.visitor.lambdaforeach.LambdaForEachIfWrapperToFilterResolver;
import eu.jsparrow.core.markers.visitor.lambdaforeach.LambdaForEachMapResolver;
import eu.jsparrow.core.markers.visitor.loop.ForToForEachResolver;
import eu.jsparrow.core.markers.visitor.loop.StringBuildingLoopResolver;
import eu.jsparrow.core.markers.visitor.loop.WhileToForEachResolver;
import eu.jsparrow.core.markers.visitor.loop.bufferedreader.BufferedReaderLinesResolver;
import eu.jsparrow.core.markers.visitor.loop.stream.EnhancedForLoopToStreamAnyMatchResolver;
import eu.jsparrow.core.markers.visitor.loop.stream.EnhancedForLoopToStreamFindFirstResolver;
import eu.jsparrow.core.markers.visitor.loop.stream.EnhancedForLoopToStreamForEachResolver;
import eu.jsparrow.core.markers.visitor.loop.stream.EnhancedForLoopToStreamSumResolver;
import eu.jsparrow.core.markers.visitor.loop.stream.EnhancedForLoopToStreamTakeWhileResolver;
import eu.jsparrow.core.markers.visitor.optional.OptionalFilterResolver;
import eu.jsparrow.core.markers.visitor.optional.OptionalIfPresentOrElseResolver;
import eu.jsparrow.core.markers.visitor.optional.OptionalIfPresentResolver;
import eu.jsparrow.core.markers.visitor.optional.OptionalMapResolver;
import eu.jsparrow.core.markers.visitor.security.CreateTempFilesUsingJavaNIOResolver;
import eu.jsparrow.core.markers.visitor.security.UseParameterizedJPAQueryResolver;
import eu.jsparrow.core.markers.visitor.security.UseParameterizedLDAPQueryResolver;
import eu.jsparrow.core.markers.visitor.security.UseParameterizedQueryResolver;
import eu.jsparrow.core.markers.visitor.security.random.ReuseRandomObjectsResolver;
import eu.jsparrow.core.markers.visitor.security.random.UseSecureRandomResolver;
import eu.jsparrow.core.markers.visitor.stream.tolist.ReplaceStreamCollectByToListResolver;
import eu.jsparrow.core.markers.visitor.trycatch.MultiCatchResolver;
import eu.jsparrow.core.markers.visitor.trycatch.TryWithResourceResolver;
import eu.jsparrow.rules.api.MarkerService;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerListener;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;
import eu.jsparrow.rules.common.markers.Resolver;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * A registry for jSparrow marker resolvers implemented in this module.
 * 
 * @since 4.0.0
 *
 */
public class ResolverVisitorsFactory {

	private static final Logger logger = LoggerFactory.getLogger(ResolverVisitorsFactory.class);
	private static final Map<String, Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor>> registry = initRegistry();

	private ResolverVisitorsFactory() {
		/*
		 * Hide the default constructor.
		 */
	}

	private static Map<String, Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor>> initRegistry() {
		Map<String, Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor>> map = new HashMap<>();
		map.put(FunctionalInterfaceResolver.ID, FunctionalInterfaceResolver::new);
		map.put(UseComparatorMethodsResolver.ID, UseComparatorMethodsResolver::new);
		map.put(InefficientConstructorResolver.ID, InefficientConstructorResolver::new);
		map.put(LambdaToMethodReferenceResolver.ID, LambdaToMethodReferenceResolver::new);
		map.put(PutIfAbsentResolver.ID, PutIfAbsentResolver::new);
		map.put(RemoveNullCheckBeforeInstanceofResolver.ID, RemoveNullCheckBeforeInstanceofResolver::new);
		map.put(StringLiteralEqualityCheckResolver.ID, StringLiteralEqualityCheckResolver::new);
		map.put(PrimitiveBoxedForStringResolver.ID, PrimitiveBoxedForStringResolver::new);
		map.put(UseIsEmptyOnCollectionsResolver.ID, UseIsEmptyOnCollectionsResolver::new);
		map.put(EnumsWithoutEqualsResolver.ID, EnumsWithoutEqualsResolver::new);
		map.put(AvoidConcatenationInLoggingStatementsResolver.ID, AvoidConcatenationInLoggingStatementsResolver::new);
		map.put(CollectionRemoveAllResolver.ID, CollectionRemoveAllResolver::new);
		map.put(DiamondOperatorResolver.ID, DiamondOperatorResolver::new);
		map.put(IndexOfToContainsResolver.ID, IndexOfToContainsResolver::new);
		map.put(InsertBreakStatementInLoopsResolver.ID, InsertBreakStatementInLoopsResolver::new);
		map.put(MapGetOrDefaultResolver.ID, MapGetOrDefaultResolver::new);
		map.put(RemoveNewStringConstructorResolver.ID, RemoveNewStringConstructorResolver::new);
		map.put(RemoveRedundantTypeCastResolver.ID, RemoveRedundantTypeCastResolver::new);
		map.put(RemoveUnusedParameterResolver.ID, RemoveUnusedParameterResolver::new);
		map.put(UseCollectionsSingletonListResolver.ID, UseCollectionsSingletonListResolver::new);
		map.put(ForToForEachResolver.ID, ForToForEachResolver::new);
		map.put(WhileToForEachResolver.ID, WhileToForEachResolver::new);
		map.put(StringBuildingLoopResolver.ID, StringBuildingLoopResolver::new);
		map.put(UseStringJoinResolver.ID, UseStringJoinResolver::new);
		map.put(MultiCatchResolver.ID, MultiCatchResolver::new);
		map.put(TryWithResourceResolver.ID, TryWithResourceResolver::new);
		map.put(UseArraysStreamResolver.ID, UseArraysStreamResolver::new);
		map.put(LambdaForEachIfWrapperToFilterResolver.ID, LambdaForEachIfWrapperToFilterResolver::new);
		map.put(LambdaForEachMapResolver.ID, LambdaForEachMapResolver::new);
		map.put(LambdaForEachCollectResolver.ID, LambdaForEachCollectResolver::new);
		map.put(CollectionsFactoryMethodsResolver.ID, CollectionsFactoryMethodsResolver::new);
		map.put(ReplaceStreamCollectByToListResolver.ID, ReplaceStreamCollectByToListResolver::new);
		map.put(EnhancedForLoopToStreamAnyMatchResolver.ID, EnhancedForLoopToStreamAnyMatchResolver::new);
		map.put(EnhancedForLoopToStreamFindFirstResolver.ID, EnhancedForLoopToStreamFindFirstResolver::new);
		map.put(EnhancedForLoopToStreamForEachResolver.ID, EnhancedForLoopToStreamForEachResolver::new);
		map.put(EnhancedForLoopToStreamSumResolver.ID, EnhancedForLoopToStreamSumResolver::new);
		map.put(EnhancedForLoopToStreamTakeWhileResolver.ID, EnhancedForLoopToStreamTakeWhileResolver::new);
		map.put(ArithmeticAssignmentResolver.ID, ArithmeticAssignmentResolver::new);
		map.put(UseFilesBufferedReaderResolver.ID, UseFilesBufferedReaderResolver::new);
		map.put(UseFilesBufferedWriterResolver.ID, UseFilesBufferedWriterResolver::new);
		map.put(UseFilesWriteStringResolver.ID, UseFilesWriteStringResolver::new);
		map.put(BufferedReaderLinesResolver.ID, BufferedReaderLinesResolver::new);
		map.put(OptionalIfPresentResolver.ID, OptionalIfPresentResolver::new);
		map.put(OptionalIfPresentOrElseResolver.ID, OptionalIfPresentOrElseResolver::new);
		map.put(OptionalMapResolver.ID, OptionalMapResolver::new);
		map.put(OptionalFilterResolver.ID, OptionalFilterResolver::new);
		map.put(CreateTempFilesUsingJavaNIOResolver.ID, CreateTempFilesUsingJavaNIOResolver::new);
		map.put(ReuseRandomObjectsResolver.ID, ReuseRandomObjectsResolver::new);
		map.put(UseSecureRandomResolver.ID, UseSecureRandomResolver::new);
		map.put(UseParameterizedQueryResolver.ID, UseParameterizedQueryResolver::new);
		map.put(UseParameterizedJPAQueryResolver.ID, UseParameterizedJPAQueryResolver::new);
		map.put(UseParameterizedLDAPQueryResolver.ID, UseParameterizedLDAPQueryResolver::new);
		map.put(CollapseIfStatementsResolver.ID, CollapseIfStatementsResolver::new);
		map.put(GuardConditionResolver.ID, GuardConditionResolver::new);
		map.put(MultiVariableDeclarationLineResolver.ID, MultiVariableDeclarationLineResolver::new);
		map.put(ReImplementingInterfaceResolver.ID, ReImplementingInterfaceResolver::new);
		map.put(RemoveDoubleNegationResolver.ID, RemoveDoubleNegationResolver::new);
		map.put(RemoveEmptyStatementResolver.ID, RemoveEmptyStatementResolver::new);
		map.put(RemoveModifiersInInterfacePropertiesResolver.ID, RemoveModifiersInInterfacePropertiesResolver::new);
		map.put(RemoveToStringOnStringResolver.ID, RemoveToStringOnStringResolver::new);
		map.put(ReplaceStringFormatByFormattedResolver.ID, ReplaceStringFormatByFormattedResolver::new);
		map.put(UseListSortResolver.ID, UseListSortResolver::new);
		map.put(MakeFieldsAndVariablesFinalResolver.ID, MakeFieldsAndVariablesFinalResolver::new);
		map.put(OverrideAnnotationResolver.ID, OverrideAnnotationResolver::new);
		map.put(PrimitiveObjectUseEqualsResolver.ID, PrimitiveObjectUseEqualsResolver::new);
		map.put(RemoveCollectionAddAllResolver.ID, RemoveCollectionAddAllResolver::new);
		map.put(RemoveExplicitCallToSuperResolver.ID, RemoveExplicitCallToSuperResolver::new);
		map.put(RemoveUnnecessaryThrownExceptionsResolver.ID, RemoveUnnecessaryThrownExceptionsResolver::new);
		map.put(ReorderModifiersResolver.ID, ReorderModifiersResolver::new);
		map.put(StatementLambdaToExpressionResolver.ID, StatementLambdaToExpressionResolver::new);
		map.put(StringBufferToBuilderResolver.ID, StringBufferToBuilderResolver::new);
		map.put(UseOffsetBasedStringMethodsResolver.ID, UseOffsetBasedStringMethodsResolver::new);
		map.put(UsePredefinedStandardCharsetResolver.ID, UsePredefinedStandardCharsetResolver::new);
		map.put(BracketsToControlResolver.ID, BracketsToControlResolver::new);
		map.put(DateDeprecatedResolver.ID, DateDeprecatedResolver::new);
		map.put(FlatMapInsteadOfNestedLoopsResolver.ID, FlatMapInsteadOfNestedLoopsResolver::new);
		map.put(ImmutableStaticFinalCollectionsResolver.ID, ImmutableStaticFinalCollectionsResolver::new);
		map.put(StringFormatLineSeparatorResolver.ID, StringFormatLineSeparatorResolver::new);
		map.put(StringUtilsResolver.ID, StringUtilsResolver::new);

		List<MarkerService> markerServices = getExternalRuleServices();
		markerServices.stream()
			.map(MarkerService::loadGeneratingFunctions)
			.forEach(map::putAll);

		return Collections.unmodifiableMap(map);
	}

	private static List<MarkerService> getExternalRuleServices() {
		BundleContext bundleContext = FrameworkUtil.getBundle(ResolverVisitorsFactory.class)
			.getBundleContext();
		ServiceReference<?>[] serviceReferences = null;
		try {
			serviceReferences = bundleContext.getServiceReferences(MarkerService.class.getName(), null);
		} catch (InvalidSyntaxException e) {
			logger.error("Failed to load external markers due to bad filterexpression.", e); //$NON-NLS-1$
		}
		// BundleContext returns null if no services are found,
		return serviceReferences == null ? Collections.emptyList()
				: Arrays.asList(serviceReferences)
					.stream()
					.map(x -> (MarkerService) bundleContext.getService(x))
					.collect(Collectors.toList());
	}

	public static Map<String, RuleDescription> getAllMarkerDescriptions() {
		return registry
			.entrySet()
			.stream()
			.collect(Collectors.toMap(Map.Entry::getKey, entry -> getDescription(entry.getValue())));
	}

	private static RuleDescription getDescription(Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor> function) {
		AbstractASTRewriteASTVisitor visitor = function.apply(node -> true);
		Resolver resolver = (Resolver) visitor;
		return resolver.getDescription();
	}

	/**
	 * @param markerIds
	 *            the list of activated markers
	 * @param checker
	 *            a predicate for testing the relevant nodes by their position
	 *            in the compilation unit.
	 * @return the list of all recorded resolvers.
	 */
	public static List<AbstractASTRewriteASTVisitor> getAllResolvers(List<String> markerIds,
			Predicate<ASTNode> checker) {
		List<AbstractASTRewriteASTVisitor> resolvers = new ArrayList<>();
		registry.forEach((name, generatingFunction) -> {
			if (markerIds.contains(name)) {
				AbstractASTRewriteASTVisitor resolver = generatingFunction.apply(checker);
				RefactoringMarkerListener listener = RefactoringMarkers.getFor(name);
				resolver.addMarkerListener(listener);
				resolvers.add(resolver);
			}
		});
		return resolvers;
	}

	/**
	 * Get the registered resolvers with the given ID (i.e., the fully qualified
	 * name).
	 * 
	 * @param resolverName
	 *            the resolver name.
	 * @return a function that gets a position function predicate and returns an
	 *         instance of a registered recorder.
	 */
	public static Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor> getResolverGenerator(String resolverName) {
		return registry.getOrDefault(resolverName, p -> null);
	}

	/**
	 * 
	 * @return the unsorted list of all registered resolver ids.
	 */
	public static List<String> getAllResolverIds() {
		return new ArrayList<>(registry.keySet());
	}
}
