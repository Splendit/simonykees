package eu.jsparrow.rules.java16;

import java.util.Arrays;
import java.util.List;

import org.osgi.service.component.annotations.Component;

import eu.jsparrow.rules.api.RuleService;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.java16.switchexpression.UseSwitchExpressionRule;
import eu.jsparrow.rules.java16.textblock.UseTextBlockRule;
import eu.jsparrow.rules.java16.javarecords.UseJavaRecordsRule;
import eu.jsparrow.rules.java16.patternmatching.UsePatternMatchingForInstanceofRule;

/**
 * Implementor of {@link RuleService}.
 *
 * @since 4.2.0
 *
 */
@Component
public class Java16RulesServiceImpl implements RuleService {

	@Override
	public List<RefactoringRule> loadRules() {
		return Arrays.asList(new UsePatternMatchingForInstanceofRule(), new UseSwitchExpressionRule(),
				new UseTextBlockRule(), new UseJavaRecordsRule());
	}
}
