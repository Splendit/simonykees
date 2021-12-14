package eu.jsparrow.core.markers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.dom.ASTNode;

import eu.jsparrow.core.markers.common.Resolver;
import eu.jsparrow.core.markers.visitor.AvoidConcatenationInLoggingStatementsResolver;
import eu.jsparrow.core.markers.visitor.CollectionRemoveAllResolver;
import eu.jsparrow.core.markers.visitor.DiamondOperatorResolver;
import eu.jsparrow.core.markers.visitor.EnumsWithoutEqualsResolver;
import eu.jsparrow.core.markers.visitor.FunctionalInterfaceResolver;
import eu.jsparrow.core.markers.visitor.IndexOfToContainsResolver;
import eu.jsparrow.core.markers.visitor.InefficientConstructorResolver;
import eu.jsparrow.core.markers.visitor.InsertBreakStatementInLoopsResolver;
import eu.jsparrow.core.markers.visitor.LambdaToMethodReferenceResolver;
import eu.jsparrow.core.markers.visitor.MapGetOrDefaultResolver;
import eu.jsparrow.core.markers.visitor.PrimitiveBoxedForStringResolver;
import eu.jsparrow.core.markers.visitor.PutIfAbsentResolver;
import eu.jsparrow.core.markers.visitor.RemoveNewStringConstructorResolver;
import eu.jsparrow.core.markers.visitor.RemoveNullCheckBeforeInstanceofResolver;
import eu.jsparrow.core.markers.visitor.RemoveRedundantTypeCastResolver;
import eu.jsparrow.core.markers.visitor.RemoveUnusedParameterResolver;
import eu.jsparrow.core.markers.visitor.StringLiteralEqualityCheckResolver;
import eu.jsparrow.core.markers.visitor.UseCollectionsSingletonListResolver;
import eu.jsparrow.core.markers.visitor.UseComparatorMethodsResolver;
import eu.jsparrow.core.markers.visitor.UseIsEmptyOnCollectionsResolver;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.markers.RefactoringMarkerListener;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * A registry for jSparrow marker resolvers implemented in this module.
 * 
 * @since 4.0.0
 *
 */
public class ResolverVisitorsFactory {

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
		return Collections.unmodifiableMap(map);
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
