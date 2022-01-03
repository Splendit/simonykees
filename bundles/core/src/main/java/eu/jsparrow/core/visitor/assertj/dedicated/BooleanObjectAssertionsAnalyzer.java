package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Map;

import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

class BooleanObjectAssertionsAnalyzer extends AbstractBooleanAssertionsAnalyzer {

	protected BooleanObjectAssertionsAnalyzer() {
		super(Map.of(), Map.of());
	}

	@Override
	protected boolean isSupportedTypeForAssertion(ITypeBinding typeBinding) {
		return ClassRelationUtil.isContentOfType(typeBinding, Object.class.getName());
	}
}
