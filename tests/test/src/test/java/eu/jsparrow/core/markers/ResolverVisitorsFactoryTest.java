package eu.jsparrow.core.markers;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
		List<AbstractASTRewriteASTVisitor> allResovlers = ResolverVisitorsFactory.getAllResolvers(node -> true);
		assertEquals(2, allResovlers.size());
	}
	
	@Test
	void test_getResolverByName_shouldReturnOneResolver() {
		Function<Predicate<ASTNode>, AbstractASTRewriteASTVisitor> resovlerGenerator = 
				ResolverVisitorsFactory.getResolverGenerator(FunctionalInterfaceResolver.class.getName());
		AbstractASTRewriteASTVisitor resolver = resovlerGenerator.apply(node -> true);
		assertTrue(resolver instanceof FunctionalInterfaceResolver);
	}

}
