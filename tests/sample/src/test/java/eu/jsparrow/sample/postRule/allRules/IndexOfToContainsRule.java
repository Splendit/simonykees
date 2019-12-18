package eu.jsparrow.sample.postRule.allRules;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.1.1
 *
 */
@SuppressWarnings("nls")
public class IndexOfToContainsRule {

	private static final Logger logger = LoggerFactory.getLogger(IndexOfToContainsRule.class);

	public void testList() {
		final List<String> l = new ArrayList<>();
		final String s = "searchString";

		// not contains
		if (!l.contains(s)) {
			l.add(s);
		}

		if (!l.contains(s)) {
			l.add(s);
		}

		if (!l.contains(s)) {
			l.add(s);
		}

		if (!l.contains(s)) {
			l.add(s);
		}

		if (!l.contains(s)) {
			l.add(s);
		}

		if (!l.contains(s)) {
			l.add(s);
		}

		if (!l.contains(s)) {
			l.add(s);
		}

		// contains
		if (l.contains(s)) {
			l.remove(s);
		}

		if (l.contains(s)) {
			l.remove(s);
		}

		if (l.contains(s)) {
			l.remove(s);
		}

		if (l.contains(s)) {
			l.remove(s);
		}

		if (l.contains(s)) {
			l.remove(s);
		}

		if (l.contains(s)) {
			l.remove(s);
		}

		if (l.contains(s)) {
			l.remove(s);
		}

		// other cases without transformation
		if (l.indexOf(s) == 0) {
			l.remove(s);
		}

		if (l.indexOf(s) == 1) {
			l.remove(s);
		}

		if (l.indexOf(s) > 0) {
			l.remove(s);
		}

		if (0 == l.indexOf(s)) {
			l.remove(s);
		}

		if (1 == l.indexOf(s)) {
			l.remove(s);
		}

		if (0 < l.indexOf(s)) {
			l.remove(s);
		}

		int i = 0;
		while ((i = l.indexOf(s)) != -1) {
			logger.info(s);
		}

		while ((i = l.indexOf(s)) >= 0) {
			logger.info(s);
		}

		final int index = l.indexOf(s);
		logger.info("object found at index: " + index);

		/*
		 * Some cases with comments
		 */

		/* saving comments */
		if (!l.contains(s)) {
			l.add(s);
		}

		// index of
		if (!l // index of
			.contains(s)) {
			l.add(s);
		}

		// I don't want to break anything
		if (!l // I don't want to break anything
			.contains(s)) {
			l.add(s);
		}
	}

	public void testString() {
		final String s = "Hello World";

		// not contains
		if (!StringUtils.contains(s, "ello")) {
			logger.info(s);
		}

		if (!StringUtils.contains(s, "ello")) {
			logger.info(s);
		}

		if (!StringUtils.contains(s, "ello")) {
			logger.info(s);
		}

		if (!StringUtils.contains(s, "ello")) {
			logger.info(s);
		}

		if (!StringUtils.contains(s, "ello")) {
			logger.info(s);
		}

		if (!StringUtils.contains(s, "ello")) {
			logger.info(s);
		}

		if (!StringUtils.contains(s, "ello")) {
			logger.info(s);
		}

		// contains
		if (StringUtils.contains(s, "ello")) {
			logger.info(s);
		}

		if (StringUtils.contains(s, "ello")) {
			logger.info(s);
		}

		if (StringUtils.contains(s, "ello")) {
			logger.info(s);
		}

		if (StringUtils.contains(s, "ello")) {
			logger.info(s);
		}

		if (StringUtils.contains(s, "ello")) {
			logger.info(s);
		}

		if (StringUtils.contains(s, "ello")) {
			logger.info(s);
		}

		if (StringUtils.contains(s, "ello")) {
			logger.info(s);
		}

		/* save me */
		if (StringUtils.contains(s, "ello")) {
			logger.info(s);
		}

		// char as argument of indexOf
		if (StringUtils.indexOf(s, 'l') != -1) {
			logger.info(s);
		}

		if (StringUtils.indexOf(s, 'l') >= 0) {
			logger.info(s);
		}

		// other cases without transformation
		if (StringUtils.indexOf(s, "ello") == 0) {
			logger.info(s);
		}

		if (StringUtils.indexOf(s, "ello") == 1) {
			logger.info(s);
		}

		if (StringUtils.indexOf(s, "ello") > 0) {
			logger.info(s);
		}

		if (0 == StringUtils.indexOf(s, "ello")) {
			logger.info(s);
		}

		if (1 == StringUtils.indexOf(s, "ello")) {
			logger.info(s);
		}

		if (0 < StringUtils.indexOf(s, "ello")) {
			logger.info(s);
		}

		int i = 0;
		while ((i = StringUtils.indexOf(s, "ello")) != -1) {
			logger.info(s);
		}

		while ((i = StringUtils.indexOf(s, "ello")) >= 0) {
			logger.info(s);
		}

		final int index = StringUtils.indexOf(s, "ello");
		logger.info("substring found at index: " + index);
	}
}
