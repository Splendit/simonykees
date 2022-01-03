package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Map;

import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

@SuppressWarnings("nls")
class BooleanStringAssertionsAnalyzer extends AbstractBooleanAssertionsAnalyzer {

	protected BooleanStringAssertionsAnalyzer() {
		super(
				Map.of("equalsIgnoreCase", "isEqualToIgnoringCase",
						"startsWith", "startsWith",
						"contains", "contains",
						"endsWith", "endsWith",
						"matches", "matches",
						"isEmpty", "isEmpty",
						"isBlank", "isBlank"),
				Map.of("equalsIgnoreCase", "isNotEqualToIgnoringCase",
						"startsWith", "doesNotStartWith",
						"contains", "doesNotContain",
						"endsWith", "doesNotEndWith",
						"matches", "doesNotMatch",
						"isEmpty", "isNotEmpty",
						"isBlank", "isNotBlank"));
	}

	@Override
	protected boolean isSupportedTypeForAssertion(ITypeBinding typeBinding) {
		return ClassRelationUtil.isContentOfType(typeBinding, String.class.getName());
	}

}
