package eu.jsparrow.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

public class OrganiseImportsRule {

	public String a(String s) {
		return StringUtils.upperCase(s, Locale.CANADA_FRENCH);
	}

	public String b(String s) {
		final List<String> list = new ArrayList<>();
		list.add(s);
		return list.get(list.indexOf(s));
	}

}
