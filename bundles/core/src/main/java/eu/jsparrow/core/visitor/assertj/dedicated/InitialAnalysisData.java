package eu.jsparrow.core.visitor.assertj.dedicated;

import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * Stores all informations about an AssertJ assertThat invocation in combination
 * with exactly one AssertJ assertion which are needed by the
 * {@link UseDedicatedAssertJAssertionsASTVisitor} for subsequent analysis.
 * 
 * @since 4.8.0
 *
 */
class InitialAnalysisData {
	private final MethodInvocation assertThatInvocation;
	private final AssertJAssertThatWithAssertionData initialAnalysisChainData;

	InitialAnalysisData(MethodInvocation assertThat,
			AssertJAssertThatWithAssertionData initialAnalysisChainData) {
		this.assertThatInvocation = assertThat;
		this.initialAnalysisChainData = initialAnalysisChainData;
	}

	MethodInvocation getAssertThatInvocation() {
		return assertThatInvocation;
	}

	AssertJAssertThatWithAssertionData getInitialAnalysisChainData() {
		return initialAnalysisChainData;
	}

}
