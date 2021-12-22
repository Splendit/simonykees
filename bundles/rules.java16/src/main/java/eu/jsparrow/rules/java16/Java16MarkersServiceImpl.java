package eu.jsparrow.rules.java16;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.osgi.service.component.annotations.Component;

import eu.jsparrow.rules.api.MarkerService;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.rules.java16.textblock.UseTextBlockResolver;

@Component
public class Java16MarkersServiceImpl implements MarkerService {

	private static final Map<String, Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor>> registry = initMap();
	
	@Override
	public Map<String, Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor>> loadGeneratingFunctions() {
		return registry;
	}

	private static Map<String, Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor>> initMap() {
		Map<String, Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor>> map = new HashMap<>();
		map.put(UseTextBlockResolver.ID, UseTextBlockResolver::new);
		return Collections.unmodifiableMap(map);
	}

}
