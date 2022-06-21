package eu.jsparrow.sample.preRule;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestReplaceRequestMappingAnnotationRule {

	@SuppressWarnings("nls")
	@RequestMapping(value = "/example/get", method = RequestMethod.GET)
	public String originalGet(@RequestParam String name) {
		return String.format("GET, name = %s", name);
	}
}
