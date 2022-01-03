package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

@SuppressWarnings("nls")
class BooleanMapAssertionsAnalyzer extends AbstractBooleanAssertionsAnalyzer {

	private static final String JAVA_UTIL = "java.util"; //$NON-NLS-1$

	protected BooleanMapAssertionsAnalyzer() {
		super(
				Map.of("containsKey", "containsKey",
						"containsValue", "containsValue",
						"isEmpty", "isEmpty"),
				Map.of("containsKey", "doesNotContainKey",
						"containsValue", "doesNotContainValue",
						"isEmpty", "isNotEmpty"));
	}

	@Override
	protected boolean isSupportedTypeForAssertion(ITypeBinding typeBinding) {
		if (ClassRelationUtil.isContentOfType(typeBinding, java.util.Map.class.getName())) {
			return true;
		}

		String packageName = typeBinding.getPackage()
			.getName();
		return packageName.equals(JAVA_UTIL)
				&& ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
						Arrays.asList(java.util.Map.class.getName()));
	}

}
