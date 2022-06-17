package eu.jsparrow.sample.postRule.requestmapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
public class TestReplaceRequestMappingAnnotationRule {

	@SuppressWarnings("nls")
	@GetMapping(value = "/example/get")
	public String originalGet(@RequestParam String name) {
		return String.format("GET, name = %s", name);
	}
}
