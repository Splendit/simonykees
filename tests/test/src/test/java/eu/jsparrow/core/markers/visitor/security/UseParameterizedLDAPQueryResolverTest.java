package eu.jsparrow.core.markers.visitor.security;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;
import eu.jsparrow.rules.common.markers.RefactoringMarkerEvent;
import eu.jsparrow.rules.common.markers.RefactoringMarkers;

class UseParameterizedLDAPQueryResolverTest extends UsesSimpleJDTUnitFixture {
	
	@BeforeEach
	void setUpVisitor() throws Exception {
		RefactoringMarkers.clear();
		fixture.addImport(javax.naming.directory.DirContext.class.getName());
		fixture.addImport(javax.naming.directory.SearchResult.class.getName());
		fixture.addImport(javax.naming.directory.SearchControls.class.getName());
		fixture.addImport(javax.naming.NamingEnumeration.class.getName());
		fixture.addImport(javax.naming.NamingException.class.getName());
	}
	
	@Test
	void test_AlwaysFalsePredicate_shouldGenerateNoMarkers() throws Exception {
		UseParameterizedLDAPQueryResolver visitor = new UseParameterizedLDAPQueryResolver(node -> false);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseParameterizedLDAPQueryResolver"));
		setVisitor(visitor);
		String original = "" +
				"String user = null;\n" +
				"String pass = null;\n" +
				"DirContext ctx = null;\n" +
				"String filter = \"(&(uid=\" + user + \")(userPassword=\" + pass + \"))\";\n" +
				"try {\n" +
				"	NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new SearchControls());\n"
				+
				"} catch (NamingException e) {\n" +
				"	e.printStackTrace();\n" +
				"}";

		assertNoChange(original);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertTrue(events.isEmpty());
	}

	@Test
	void test_markerGeneration_shouldGenerateOneMarkerEvent() throws Exception {
		UseParameterizedLDAPQueryResolver visitor = new UseParameterizedLDAPQueryResolver(node -> true);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseParameterizedLDAPQueryResolver"));
		setVisitor(visitor);
		String original = "" +
				"String user = null;\n" +
				"String pass = null;\n" +
				"DirContext ctx = null;\n" +
				"String filter = \"(&(uid=\" + user + \")(userPassword=\" + pass + \"))\";\n" +
				"try {\n" +
				"	NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new SearchControls());\n"
				+
				"} catch (NamingException e) {\n" +
				"	e.printStackTrace();\n" +
				"}";
		String expected = "" +
				"String user = null;\n" +
				"String pass = null;\n" +
				"DirContext ctx = null;\n" +
				"String filter = \"(&(uid={0}\" + \")(userPassword={1}\" + \"))\";\n" +
				"try {\n" +
				"NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new Object[] {user, pass}, new SearchControls());\n"
				+
				"} catch (NamingException e) {\n" +
				"	e.printStackTrace();\n" +
				"}";

		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
		RefactoringMarkerEvent event = events.get(0);
		String description = ""
				+ "Similar to SQL or JPA queries, the LDAP search statements are also vulnerable to injection "
				+ "attacks. This rule parameterizes potential user supplied input concatenated into an LDAP "
				+ "search filter. This ensures a separation between the intended search filter and the supplied parameters.";
		
		assertAll(
				() -> assertEquals("Use Parameterized LDAP Query", event.getName()),
				() -> assertEquals(description, event.getMessage()), 
				() -> assertEquals("UseParameterizedLDAPQueryResolver", event.getResolver()),
				() -> assertEquals(description, event.getCodePreview()),
				() -> assertEquals(0, event.getHighlightLength()),
				() -> assertEquals(520, event.getOffset()),
				() -> assertEquals(53, event.getLength()),
				() -> assertEquals(16, event.getLineNumber()),
				() -> assertEquals(20, event.getWeightValue()));
	}

	@Test
	void test_resolveMarkers_shouldResolveOne() throws Exception {
		UseParameterizedLDAPQueryResolver visitor = new UseParameterizedLDAPQueryResolver(node -> node.getStartPosition() == 521);
		visitor.addMarkerListener(RefactoringMarkers.getFor("UseParameterizedLDAPQueryResolver"));
		setVisitor(visitor);
		String original = "" +
				"String user = null;\n" +
				"String pass = null;\n" +
				"DirContext ctx = null;\n" +
				"String filter = \"(&(uid=\" + user + \")(userPassword=\" + pass + \"))\";\n" +
				"try {\n" +
				"	NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new SearchControls());\n"
				+
				"} catch (NamingException e) {\n" +
				"	e.printStackTrace();\n" +
				"}";
		String expected = "" +
				"String user = null;\n" +
				"String pass = null;\n" +
				"DirContext ctx = null;\n" +
				"String filter = \"(&(uid={0}\" + \")(userPassword={1}\" + \"))\";\n" +
				"try {\n" +
				"NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new Object[] {user, pass}, new SearchControls());\n"
				+
				"} catch (NamingException e) {\n" +
				"	e.printStackTrace();\n" +
				"}";
		assertChange(original, expected);
		List<RefactoringMarkerEvent> events = RefactoringMarkers.getAllEvents();
		assertEquals(1, events.size());
	}
}