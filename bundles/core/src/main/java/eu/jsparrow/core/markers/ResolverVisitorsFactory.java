package eu.jsparrow.core.markers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;

import eu.jsparrow.core.markers.visitor.FunctionalInterfaceResolver;
import eu.jsparrow.core.markers.visitor.UseComparatorMethodsResolver;
import eu.jsparrow.rules.common.markers.RefactoringMarkerListener;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class ResolverVisitorsFactory {

	public static List<AbstractASTRewriteASTVisitor> getAllResolvers() {
		List<AbstractASTRewriteASTVisitor> resolvers = new ArrayList<>();
		
		Map<String, AbstractASTRewriteASTVisitor> registeredEventGenerators = initEventGenerators();
		
		registeredEventGenerators.forEach((key, value) -> {
			RefactoringMarkerListener listener = RefactoringMarkers.getFor(key);
			value.addMarkerListener(listener);
			resolvers.add(value);
		});
		
		return resolvers;
	}
	
	public static List<AbstractASTRewriteASTVisitor> getAllResolvers(Predicate<ASTNode>poistionChcker) {
		List<AbstractASTRewriteASTVisitor> eventResolvers = new ArrayList<>();
		eventResolvers.add(new FunctionalInterfaceResolver(poistionChcker));
		eventResolvers.add(new UseComparatorMethodsResolver(poistionChcker));
		return Collections.unmodifiableList(eventResolvers);
	}

	private static Map<String, AbstractASTRewriteASTVisitor> initEventGenerators() {
		Map<String, AbstractASTRewriteASTVisitor> eventGenerators = new HashMap<>();
		eventGenerators.put(FunctionalInterfaceResolver.RESOLVER_NAME, new FunctionalInterfaceResolver());
		eventGenerators.put(UseComparatorMethodsResolver.RESOLVER_NAME, new UseComparatorMethodsResolver());
		return Collections.unmodifiableMap(eventGenerators);
	}
}
