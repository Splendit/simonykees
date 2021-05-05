package eu.jsparrow.core.markers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;

import eu.jsparrow.core.markers.visitor.FunctionalInterfaceResolver;
import eu.jsparrow.core.markers.visitor.UseComparatorMethodsResolver;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public class ResolverVisitorsFactory {
	
	public static List<AbstractASTRewriteASTVisitor> getAllResolvers(Predicate<ASTNode> predicate) {
		List<AbstractASTRewriteASTVisitor> resolvers = new ArrayList<>();
		resolvers.add(new FunctionalInterfaceResolver(predicate));
		resolvers.add(new UseComparatorMethodsResolver(predicate));
		return resolvers;
	}
}
