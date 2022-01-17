package eu.jsparrow.rules.api;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Service interface for jSparrow marker resolver providers. Implementors
 * provide a map from resolver IDs to functions that generate resolver
 * instances.
 * 
 * @since 4.7.0
 *
 */
public interface MarkerService {

	Map<String, Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor>> loadGeneratingFunctions();
}
