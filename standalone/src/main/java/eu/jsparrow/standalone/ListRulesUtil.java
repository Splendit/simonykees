package eu.jsparrow.standalone;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Splitter;

import eu.jsparrow.core.rule.RefactoringRule;
import eu.jsparrow.core.rule.RulesContainer;
import eu.jsparrow.core.visitor.AbstractASTRewriteASTVisitor;

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
	@SuppressWarnings("nls")
	public String listRules(String ruleId) {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> allRules = getAllRulesFilteredById(ruleId);

		StringBuilder sb = new StringBuilder();
		sb.append("\n");

		if (!allRules.isEmpty()) {
			allRules.forEach(rule -> {
				sb.append(LINE_SEPARATOR_EQUAL);

				sb.append("ID: ");
				sb.append(rule.getId());
				sb.append("\n");

				sb.append(LINE_SEPARATOR_HIPHEN);

				sb.append("Name: ");
				sb.append(rule.getRuleDescription()
					.getName());
				sb.append("\n");

				sb.append(LINE_SEPARATOR_HIPHEN);

				sb.append("Description: ");
				sb.append(rule.getRuleDescription()
					.getDescription());
				sb.append("\n");
			});
		} else {
			sb.append(LINE_SEPARATOR_EQUAL);
			sb.append("No rules available!");
		}

		sb.append(LINE_SEPARATOR_EQUAL);

		String result = sb.toString();
		logger.info(result);

		return result;
	}

	/**
	 * writes a table with each rule's ID and name to the logger
	 */
	@SuppressWarnings("nls")
	public String listRulesShort() {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> allRules = getAllRules();
		Optional<Integer> maxWordLength = getMaxWordLength(allRules);

		StringBuilder sb = new StringBuilder();

		sb.append("\n");
		sb.append(LINE_SEPARATOR_EQUAL);

		if (!allRules.isEmpty()) {
			sb.append("| ID");
			sb.append(calculateWhitespace(2, maxWordLength.orElse(1)));
			sb.append("| Name\n");

			sb.append(LINE_SEPARATOR_HIPHEN);

			allRules.forEach(rule -> {
				sb.append("| ");
				sb.append(rule.getId());
				sb.append(calculateWhitespace(rule.getId()
					.length(), maxWordLength.orElse(1)));
				sb.append("| ");
				sb.append(rule.getRuleDescription()
					.getName());
				sb.append("\n");
			});
		} else {
			sb.append("No rules available!");
		}

		sb.append(LINE_SEPARATOR_EQUAL);

		String result = sb.toString();
		logger.info(result);

		return result;
	}

	List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getAllRules() {
		return getAllRulesFilteredById(null);
	}

	List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getAllRulesFilteredById(String ruleId) {
		List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> allRules;

		if (ruleId != null) {
			List<String> ruleIds = Splitter.on(",") //$NON-NLS-1$
				.trimResults()
				.omitEmptyStrings()
				.splitToList(ruleId);

			allRules = getAllRulesFromContainer().stream()
				.filter(rule -> ruleIds.contains(rule.getId()))
				.collect(Collectors.toList());
		} else {
			allRules = getAllRulesFromContainer();
		}

		return allRules;
	}

	Optional<Integer> getMaxWordLength(List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> allRules) {
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

	protected List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> getAllRulesFromContainer() {
		return RulesContainer.getAllRules(true);
	}
}
