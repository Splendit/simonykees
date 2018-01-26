package eu.jsparrow.standalone;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;
import eu.jsparrow.i18n.Messages;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.3.0
 */
public class ListRulesUtil {

	private static final Logger logger = LoggerFactory.getLogger(ListRulesUtil.class);

	private static final String LINE_SEPARATOR_EQUAL = "================================================================================\n"; //$NON-NLS-1$
	private static final String LINE_SEPARATOR_HIPHEN = "--------------------------------------------------------------------------------\n"; //$NON-NLS-1$

	private ListRulesUtil() {

	}

	/**
	 * writes all rules available in {@link RulesContainer} to the logger in a
	 * readable form
	 */
	public static void listRules() {
		listRules(null);
	}

	/**
	 * writes all rules stated in the {@code ruleId} parameter to the logger in
	 * a readable form
	 * 
	 * @param ruleId
	 *            a comma-separated string of rule IDs or {@code null}, if all
	 *            rules should be printed
	 */
	public static void listRules(String ruleId) {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> allRules;

		if (ruleId != null) {
			String newRuleId = ruleId.replace(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
			newRuleId = newRuleId.replace("\t", ""); //$NON-NLS-1$ //$NON-NLS-2$
			List<String> ruleIds = Arrays.asList(newRuleId.split(",")); //$NON-NLS-1$

			allRules = RulesContainer.getAllRules(true)
				.stream()
				.filter(rule -> ruleIds.contains(rule.getId()))
				.collect(Collectors.toList());
		} else {
			allRules = RulesContainer.getAllRules(true);
		}

		StringBuilder sb = new StringBuilder();
		sb.append("\n"); //$NON-NLS-1$

		if (!allRules.isEmpty()) {
			allRules.forEach(rule -> {
				sb.append(LINE_SEPARATOR_EQUAL);

				sb.append(Messages.ListRulesUtil_ID);
				sb.append(": "); //$NON-NLS-1$
				sb.append(rule.getId());
				sb.append("\n"); //$NON-NLS-1$

				sb.append(LINE_SEPARATOR_HIPHEN);

				sb.append(Messages.ListRulesUtil_Name);
				sb.append(": "); //$NON-NLS-1$
				sb.append(rule.getRuleDescription()
					.getName());
				sb.append("\n"); //$NON-NLS-1$

				sb.append(LINE_SEPARATOR_HIPHEN);

				sb.append(Messages.ListRulesUtil_Description);
				sb.append(": "); //$NON-NLS-1$
				sb.append(rule.getRuleDescription()
					.getDescription());
				sb.append("\n"); //$NON-NLS-1$
			});
		} else {
			sb.append(LINE_SEPARATOR_EQUAL);
			sb.append(Messages.ListRulesUtil_NoRulesAvailable);
		}

		sb.append(LINE_SEPARATOR_EQUAL);

		String result = sb.toString();
		logger.info(result);
	}

	/**
	 * writes a table with each rule's ID and name to the logger
	 */
	public static void listRulesShort() {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> allRules = RulesContainer.getAllRules(true);

		Optional<Integer> maxWordLength = allRules.stream()
			.map(rule -> rule.getId()
				.length())
			.max(Integer::compare);

		StringBuilder sb = new StringBuilder();

		sb.append("\n"); //$NON-NLS-1$
		sb.append(LINE_SEPARATOR_EQUAL);

		if (!allRules.isEmpty()) {
			sb.append("| "); //$NON-NLS-1$
			sb.append(Messages.ListRulesUtil_ID);
			sb.append(calculateWhitespace(2, maxWordLength.orElse(1)));
			sb.append("| "); //$NON-NLS-1$
			sb.append(Messages.ListRulesUtil_Name);
			sb.append("\n"); //$NON-NLS-1$

			sb.append(LINE_SEPARATOR_HIPHEN);

			allRules.forEach(rule -> {
				sb.append("| "); //$NON-NLS-1$
				sb.append(rule.getId());
				sb.append(calculateWhitespace(rule.getId()
					.length(), maxWordLength.orElse(1)));
				sb.append("| "); //$NON-NLS-1$
				sb.append(rule.getRuleDescription()
					.getName());
				sb.append("\n"); //$NON-NLS-1$
			});
		} else {
			sb.append(Messages.ListRulesUtil_NoRulesAvailable);
		}

		sb.append(LINE_SEPARATOR_EQUAL);

		String result = sb.toString();
		logger.info(result);
	}

	private static String calculateWhitespace(int currentWordLength, int maxWordLength) {
		int whitespaceLength = maxWordLength - currentWordLength;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i <= whitespaceLength; i++) {
			sb.append(" "); //$NON-NLS-1$
		}

		return sb.toString();
	}
}
