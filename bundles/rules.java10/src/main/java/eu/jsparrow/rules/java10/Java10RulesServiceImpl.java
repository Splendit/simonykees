package eu.jsparrow.rules.java10;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.osgi.service.component.annotations.Component;

import eu.jsparrow.rules.api.RuleService;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.java10.factory.methods.CollectionsFactoryMethodsRule;

/**
 * Implementor of {@link RuleService}. Currently provides only
 * {@link LocalVariableTypeInferenceRule}.
 *
 * @author Hans-Jörg Schrödl
 *
 */
@Component
public class Java10RulesServiceImpl implements RuleService {

	@Override
	public List<RefactoringRule> loadRules() {
		return Collections
			.unmodifiableList(Arrays.asList(new LocalVariableTypeInferenceRule(), new CollectionsFactoryMethodsRule()));
	}

}
