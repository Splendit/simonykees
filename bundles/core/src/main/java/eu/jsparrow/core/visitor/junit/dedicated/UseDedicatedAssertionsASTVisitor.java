package eu.jsparrow.core.visitor.junit.dedicated;

import org.eclipse.jdt.core.dom.MethodInvocation;

import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Replaces boolean assertions by dedicated assertions, for example:
 * 
 * <pre>
 * assertTrue(a.equals(b));
 * </pre>
 * 
 * is replaced by
 * 
 * <pre>
 * assertEquals(a, b);
 * </pre>
 * 
 * 
 * @since 3.32.0
 * 
 */
public class UseDedicatedAssertionsASTVisitor extends AbstractASTRewriteASTVisitor {

	@Override
	public boolean visit(MethodInvocation node) {
		BooleanAssertionAnalyzer assertionAnalyzer = new BooleanAssertionAnalyzer();
		DedicatedAssertionsAnalysisResult analysisResult = assertionAnalyzer.analyzeAssertInvocation(node)
			.orElse(null);
		if (analysisResult != null) {
			analysisResult.toString();
		}
		return true;
	}
}