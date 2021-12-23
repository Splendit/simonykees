package org.eu.jsparrow.rules.java16.textblock;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.java16.textblock.UseTextBlockResolver;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class UseTextBlockResolverTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	void setUp() throws Exception {
		RefactoringMarkers.clear();
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseTextBlockResolver visitor = new UseTextBlockResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseTextBlockResolver"));
		setVisitor(visitor);
		String original = "" +
				"		String html = \"\" +\n" +
				"				\"              <html>\\n\" + \n" +
				"				\"                  <body>\\n\"+ \n" +
				"				\"                      <p>Hello, world</p>\\n\" + \n" +
				"				\"                  </body>\\n\"+\n" +
				"				\"              </html>\\n\";";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseTextBlockResolver visitor = new UseTextBlockResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseTextBlockResolver"));
		setVisitor(visitor);
		String original = "" +
				"		String html = \"\" +\n" +
				"				\"              <html>\\n\" + \n" +
				"				\"                  <body>\\n\"+ \n" +
				"				\"                      <p>Hello, world</p>\\n\" + \n" +
				"				\"                  </body>\\n\"+\n" +
				"				\"              </html>\\n\";";

		String expected = "" +
				"  String html=\"\"\"\n"
				+ "                      <html>\n"
				+ "                          <body>\n"
				+ "                              <p>Hello, world</p>\n"
				+ "                          </body>\n"
				+ "                      </html>\n"
				+ "        \"\"\";";
		String codePreview = ""
				+ "\"\"\"\n"
				+ "              <html>\n"
				+ "                  <body>\n"
				+ "                      <p>Hello, world</p>\n"
				+ "                  </body>\n"
				+ "              </html>\n"
				+ "\"\"\"";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Java 15 introduced Text Blocks to express String literals spanning several lines of code and significantly reduce the need for escape sequences. \n"
				+ "This rule replaces multiline String concatenation expressions with Text Blocks String literals. Thus, removing some boilerplate code and increasing the readability of String expressions.";
		assertAll(
				() -> assertEquals("Use Text Block", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("UseTextBlockResolver", event.getResolver()),
				() -> assertEquals(codePreview, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(103, event.getOffset()),
				() -> assertEquals(200, event.getLength()),
				() -> assertEquals(6, event.getLineNumber()),
				() -> assertEquals(5, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseTextBlockResolver visitor = new UseTextBlockResolver(node -> node.getStartPosition() == 103);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseTextBlockResolver"));
		setVisitor(visitor);
		String original = "" +
				"		String html = \"\" +\n" +
				"				\"              <html>\\n\" + \n" +
				"				\"                  <body>\\n\"+ \n" +
				"				\"                      <p>Hello, world</p>\\n\" + \n" +
				"				\"                  </body>\\n\"+\n" +
				"				\"              </html>\\n\";";

		String expected = "" +
				"  String html=\"\"\"\n"
				+ "                      <html>\n"
				+ "                          <body>\n"
				+ "                              <p>Hello, world</p>\n"
				+ "                          </body>\n"
				+ "                      </html>\n"
				+ "        \"\"\";";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}
