package eu.jsparrow.sample.postRule.allRules;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestReplaceRequestMappingAnnotationRule {

	@SuppressWarnings("nls")
	@GetMapping(value = "/example/get")
	public String originalGet(@RequestParam String name) {
		return String.format("GET, name = %s", name);
	}
}
