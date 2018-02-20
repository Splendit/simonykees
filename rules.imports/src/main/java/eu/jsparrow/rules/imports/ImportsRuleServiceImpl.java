package eu.jsparrow.rules.imports;

import java.util.Collections;
import java.util.List;

import org.osgi.service.component.annotations.Component;

import eu.jsparrow.rules.api.RuleService;
import eu.jsparrow.rules.common.RefactoringRule;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Implementor of {@link RuleService}. Currently provides only {@link OrganiseImportsRule}.  
 *
 * @author Hans-Jörg Schrödl
 *
 */
@Component
public class ImportsRuleServiceImpl implements RuleService {

	@Override
	public List<RefactoringRule<? extends AbstractASTRewriteASTVisitor>> loadRules() {
		return Collections.singletonList(new OrganiseImportsRule());
	}

}
