package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class UseParameterizedLDAPQueryASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new UseParameterizedLDAPQueryASTVisitor());
		fixture.addImport(javax.naming.directory.DirContext.class.getName());
		fixture.addImport(javax.naming.directory.SearchResult.class.getName());
		fixture.addImport(javax.naming.directory.SearchControls.class.getName());
		fixture.addImport(javax.naming.NamingEnumeration.class.getName());
		fixture.addImport(javax.naming.NamingException.class.getName());
	}

	@Test
	public void visit_searchWithUserPass_shouldTransform() throws Exception {
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
	}

	@Test
	public void visit_FilterArgumentNotStoredInVariable_shouldTransform() throws Exception {
		String original = "" +
				"			String user = null;\n" +
				"			String pass = null;\n" +
				"			DirContext ctx = null;\n" +
				"			try {\n" +
				"				NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\",\n" +
				"						\"(&(uid=\" + user + \")(userPassword=\" + pass + \"))\", new SearchControls());\n"
				+
				"			} catch (NamingException e) {\n" +
				"				e.printStackTrace();\n" +
				"			}";
		String expected = "" +
				"			String user = null;\n" +
				"			String pass = null;\n" +
				"			DirContext ctx = null;\n" +
				"			try {\n" +
				"				NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\",\n" +
				"						\"(&(uid={0}\" + \")(userPassword={1}\" + \"))\", new Object[] { user, pass }, new SearchControls());\n"
				+
				"			} catch (NamingException e) {\n" +
				"				e.printStackTrace();\n" +
				"			}";
		assertChange(original, expected);
	}

	@Test
	public void visit_FilterInitializedAfterDeclaration_shouldTransform() throws Exception {
		String original = "" +
				"			String user = null;\n" +
				"			String pass = null;\n" +
				"			DirContext ctx = null;\n" +
				"			String filter;\n" +
				"			filter = \"(&(uid=\";\n" +
				"			filter += user;\n" +
				"			filter += \")(userPassword=\";\n" +
				"			filter += pass;\n" +
				"			filter += \"))\";\n" +
				"			try {\n" +
				"				NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new SearchControls());\n"
				+
				"			} catch (NamingException e) {\n" +
				"				e.printStackTrace();\n" +
				"			}";

		String expected = "" +
				"			String user = null;\n" +
				"			String pass = null;\n" +
				"			DirContext ctx = null;\n" +
				"			String filter;\n" +
				"			filter = \"(&(uid={0}\";\n" +
				"			filter += \")(userPassword={1}\";\n" +
				"			filter += \"))\";\n" +
				"			try {\n" +
				"				NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new Object[] { user, pass },\n"
				+
				"						new SearchControls());\n" +
				"			} catch (NamingException e) {\n" +
				"				e.printStackTrace();\n" +
				"			}";

		assertChange(original, expected);
	}

	@Test
	public void visit_FilterInitializedWithNullAtDeclaration_shouldTransform() throws Exception {
		String original = "" +
				"			String user = null;\n" +
				"			String pass = null;\n" +
				"			DirContext ctx = null;\n" +
				"			String filter = null;\n" +
				"			filter = \"(&(uid=\";\n" +
				"			filter += user;\n" +
				"			filter += \")(userPassword=\";\n" +
				"			filter += pass;\n" +
				"			filter += \"))\";\n" +
				"			try {\n" +
				"				NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new SearchControls());\n"
				+
				"			} catch (NamingException e) {\n" +
				"				e.printStackTrace();\n" +
				"			}";

		String expected = "" +
				"			String user = null;\n" +
				"			String pass = null;\n" +
				"			DirContext ctx = null;\n" +
				"			String filter = null;\n" +
				"			filter = \"(&(uid={0}\";\n" +
				"			filter += \")(userPassword={1}\";\n" +
				"			filter += \"))\";\n" +
				"			try {\n" +
				"				NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new Object[] { user, pass },\n"
				+
				"						new SearchControls());\n" +
				"			} catch (NamingException e) {\n" +
				"				e.printStackTrace();\n" +
				"			}";

		assertChange(original, expected);
	}

	@Test
	public void visit_useSearchWithInstanceOfName_shouldTransform() throws Exception {
		fixture.addImport(javax.naming.Name.class.getName());
		String original = "" +
				"			String user = null;\n" +
				"			String pass = null;\n" +
				"			DirContext ctx = null;\n" +
				"			Name name = null;\n" +
				"			String filter = \"(&(uid=\" + user + \")(userPassword=\" + pass + \"))\";\n" +
				"			try {\n" +
				"				NamingEnumeration<SearchResult> results = ctx.search(name, filter, new SearchControls());\n"
				+
				"			} catch (NamingException e) {\n" +
				"				e.printStackTrace();\n" +
				"			}";
		String expected = "" +
				"			String user = null;\n" +
				"			String pass = null;\n" +
				"			DirContext ctx = null;\n" +
				"			Name name = null;\n" +
				"			String filter = \"(&(uid={0}\" + \")(userPassword={1}\" + \"))\";\n" +
				"			try {\n" +
				"				NamingEnumeration<SearchResult> results = ctx.search(name, filter, new Object[] { user, pass },\n"
				+
				"						new SearchControls());\n" +
				"			} catch (NamingException e) {\n" +
				"				e.printStackTrace();\n" +
				"			}";
		assertChange(original, expected);
	}

}
