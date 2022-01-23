package eu.jsparrow.core.visitor.assertj.dedicated;

import org.eclipse.jdt.core.dom.MethodInvocation;

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
