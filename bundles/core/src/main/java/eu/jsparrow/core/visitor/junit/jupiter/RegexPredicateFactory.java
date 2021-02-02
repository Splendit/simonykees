package eu.jsparrow.core.visitor.junit.jupiter;

import java.util.function.Predicate;
import java.util.regex.Pattern;

class RegexPredicateFactory {

	static Predicate<String> createjUnit4PackagePredicate() {
		String regexOrgJunitChildPackages = "experimental|function|internal|matchers|rules|runner|runners|validator"; //$NON-NLS-1$
		String regexOrgJUnit = "org\\.junit(\\.(" + regexOrgJunitChildPackages + ")(\\..+)?)?$"; //$NON-NLS-1$ //$NON-NLS-2$

		String regexJUnit = "junit\\.(extensions|framework|runner|textui)$"; //$NON-NLS-1$
		Pattern pattern = Pattern.compile("^(" + regexJUnit + ")|(" + regexOrgJUnit + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		return pattern.asPredicate();
	}

	static Predicate<String> createSupportedAnnotationPredicate() {

		String regexSimpleNames = "Ignore|Test|After|AfterClass|Before|BeforeClass"; //$NON-NLS-1$
		String regex = "org\\.junit\\.(" + regexSimpleNames + ")$"; //$NON-NLS-1$ //$NON-NLS-2$
		return Pattern.compile(regex)
			.asPredicate();
	}

}
