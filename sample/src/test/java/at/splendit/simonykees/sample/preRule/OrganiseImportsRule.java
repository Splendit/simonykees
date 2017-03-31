package at.splendit.simonykees.sample.preRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.applet.*;
import java.awt.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.CharSequenceUtils;
import org.apache.commons.lang3.RandomStringUtils;

public class OrganiseImportsRule {

	public String a(String s) {
		return StringUtils.upperCase(s, Locale.CANADA_FRENCH);
	}

	public String b(String s) {
		List<String> list = new ArrayList<>();
		list.add(s);
		return list.get(list.indexOf(s));
	}

}
