package eu.jsparrow.independent;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRule;

/**
 * 
 * @author Matthias Webhofer
 * @since 2.3.0
 */
public class ListRulesUtil {

	private static final Logger logger = LoggerFactory.getLogger(ListRulesUtil.class);

	private static final String LINE_SEPARATOR_EQUAL = "================================================================================\n"; //$NON-NLS-1$
	private static final String LINE_SEPARATOR_HIPHEN = "--------------------------------------------------------------------------------\n"; //$NON-NLS-1$

	/**
	 * writes all rules available in {@link RulesContainer} to the logger in a
	 * readable form
	 */
	public String listRules() {
		return listRules(null);
	}

	/**
	 * writes all rules stated in the {@code ruleId} parameter to the logger in
	 * a readable form
	 * 
	 * @param ruleId
	 *            a comma-separated string of rule IDs or {@code null}, if all
	 *            rules should be printed
	 */
	public String listRules(String ruleId) {
		List<RefactoringRule> allRules = getAllRulesFilteredById(ruleId);

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
			sb.append("\n"); //$NON-NLS-1$
		}

		sb.append(LINE_SEPARATOR_EQUAL);

		String result = sb.toString();
		logger.info(result);

		return result;
	}

	/**
	 * writes a table with each rule's ID and name to the logger
	 */
	public String listRulesShort() {
		List<RefactoringRule> allRules = getAllRules();
		Optional<Integer> maxWordLength = getMaxWordLength(allRules);

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
			sb.append("\n"); //$NON-NLS-1$
		}

		sb.append(LINE_SEPARATOR_EQUAL);

		String result = sb.toString();
		logger.info(result);

		return result;
	}

	List<RefactoringRule> getAllRules() {
		return getAllRulesFilteredById(null);
	}

	List<RefactoringRule> getAllRulesFilteredById(String ruleId) {
		List<RefactoringRule> allRules;

		if (ruleId != null) {
			List<String> ruleIds = Arrays.stream(ruleId.split(",")) //$NON-NLS-1$
				.map(String::trim)
				.filter(value -> !value.isEmpty())
				.collect(Collectors.toList());

			allRules = getAllRulesFromContainer().stream()
				.filter(rule -> ruleIds.contains(rule.getId()))
				.collect(Collectors.toList());
		} else {
			allRules = getAllRulesFromContainer();
		}

		return allRules;
	}

	Optional<Integer> getMaxWordLength(List<RefactoringRule> allRules) {
		return allRules.stream()
			.map(rule -> rule.getId()
				.length())
			.max(Integer::compare);
	}

	String calculateWhitespace(int currentWordLength, int maxWordLength) {
		int whitespaceLength = maxWordLength - currentWordLength;

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i <= whitespaceLength; i++) {
			sb.append(" "); //$NON-NLS-1$
		}

		return sb.toString();
	}

	protected List<RefactoringRule> getAllRulesFromContainer() {
		List<RefactoringRule> allRules = new LinkedList<>();

		// all regular rules
		allRules.addAll(RulesContainer.getAllRules(true));

		// all semiautomatic rules (i.e. filed renaming rule, logger rule, ...)
		allRules.addAll(RulesContainer.getAllSemiAutomaticRules());

		return allRules;
	}
}
