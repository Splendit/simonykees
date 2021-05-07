package eu.jsparrow.core.markers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;

import eu.jsparrow.core.markers.visitor.FunctionalInterfaceResolver;
import eu.jsparrow.core.markers.visitor.UseComparatorMethodsResolver;
import eu.jsparrow.rules.common.markers.RefactoringMarkerListener;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class ResolverVisitorsFactory {

	private static final Map<String, Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor>> registry = initRegistry();

	private static Map<String, Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor>> initRegistry() {
		Map<String, Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor>> map = new HashMap<>();
		map.put(FunctionalInterfaceResolver.RESOLVER_NAME, FunctionalInterfaceResolver::new);
		map.put(UseComparatorMethodsResolver.RESOLVER_NAME, UseComparatorMethodsResolver::new);
		return Collections.unmodifiableMap(map);
	}

	public static List<AbstractASTRewriteASTVisitor> getAllResolvers(Predicate<ASTNode> checker) {
		List<AbstractASTRewriteASTVisitor> resolvers = new ArrayList<>();
		registry.forEach((key, value) -> {
			AbstractASTRewriteASTVisitor resolver = value.apply(checker);
			RefactoringMarkerListener listener = RefactoringMarkers.getFor(key);
			resolver.addMarkerListener(listener);
			resolvers.add(resolver);
		});
		return resolvers;
	}

	public static Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor> getResolverGenerator(String resolverName) {
		return registry.getOrDefault(resolverName, p -> null);
	}
}
