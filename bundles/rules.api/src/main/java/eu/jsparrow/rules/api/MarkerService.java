package eu.jsparrow.rules.api;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

public interface MarkerService {

	Map<String, Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor>> loadGeneratingFunctions();
}
