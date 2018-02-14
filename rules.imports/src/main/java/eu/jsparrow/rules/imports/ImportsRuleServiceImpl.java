package eu.jsparrow.rules.imports;

import java.util.Collections;
import java.util.List;

import org.osgi.service.component.annotations.Component;

import eu.jsparrow.rules.api.RuleService;

@Component
public class ImportsRuleServiceImpl implements RuleService {

	@Override
	public List<String> loadRules() {
		// TODO Auto-generated method stub
		return Collections.singletonList("Zervas!");
	}

}
