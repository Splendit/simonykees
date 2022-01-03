package eu.jsparrow.core.visitor.assertj.dedicated;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.jdt.core.dom.ITypeBinding;

import eu.jsparrow.rules.common.util.ClassRelationUtil;

@SuppressWarnings("nls")
class BooleanIterableAssertionsAnalyzer extends AbstractBooleanAssertionsAnalyzer {

	private static final String JAVA_UTIL = "java.util"; //$NON-NLS-1$

	protected BooleanIterableAssertionsAnalyzer() {
		super(
				Map.of("contains", "contains",
						"containsAll", "containsAll",
						"isEmpty", "isEmpty"),
				Map.of("contains", "doesNotContain",
						"isEmpty", "isNotEmpty"));
	}

	@Override
	protected boolean isSupportedTypeForAssertion(ITypeBinding typeBinding) {
		if (ClassRelationUtil.isContentOfType(typeBinding, java.lang.Iterable.class.getName())) {
			return true;
		}

		String packageName = typeBinding.getPackage()
			.getName();
		return packageName.equals(JAVA_UTIL)
				&& ClassRelationUtil.isInheritingContentOfTypes(typeBinding,
						Arrays.asList(java.lang.Iterable.class.getName()));
	}

}
