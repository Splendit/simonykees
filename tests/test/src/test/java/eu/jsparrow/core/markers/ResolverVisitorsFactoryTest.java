package eu.jsparrow.core.markers;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.eclipse.jdt.core.dom.ASTNode;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.markers.visitor.FunctionalInterfaceResolver;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

class ResolverVisitorsFactoryTest {

	@Test
	void test_getAllResolvers_shouldReturnAllResolvers() {
		List<String> allIds = new ArrayList<String>(ResolverVisitorsFactory.getAllMarkerDescriptions().keySet());
		List<AbstractASTRewriteASTVisitor> allResovlers = ResolverVisitorsFactory
			.getAllResolvers(allIds, node -> true);
		assertEquals(88, allResovlers.size());
	}

	@Test
	void test_getResolverByName_shouldReturnOneResolver() {
		Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor> resovlerGenerator = ResolverVisitorsFactory
			.getResolverGenerator("FunctionalInterfaceResolver");
		AbstractASTRewriteASTVisitor resolver = resovlerGenerator.apply(node -> true);
		assertTrue(resolver instanceof FunctionalInterfaceResolver);
	}

}
