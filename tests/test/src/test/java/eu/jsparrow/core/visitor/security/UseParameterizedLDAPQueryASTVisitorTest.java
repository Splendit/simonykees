package eu.jsparrow.core.visitor.security;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesSimpleJDTUnitFixture;

public class UseParameterizedLDAPQueryASTVisitorTest extends UsesSimpleJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		setVisitor(new UseParameterizedLDAPQueryASTVisitor());
		fixture.addImport(javax.naming.directory.DirContext.class.getName());
		fixture.addImport(javax.naming.NamingEnumeration.class.getName());
		fixture.addImport(javax.naming.directory.SearchResult.class.getName());
		fixture.addImport(javax.naming.directory.SearchControls.class.getName());
	}

	@Test
	public void visit_searchWithUserPass_shouldTransform() throws Exception {
		String original = "" +
				"String user = null;\n" +
				"String pass = null;\n" +
				"DirContext ctx = null;\n" +
				"String filter = \"(&(uid=\" + user + \")(userPassword=\" + pass + \"))\";\n" +
				"try {\n" +
				"	NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new SearchControls());\n" +
				"} catch (NamingException e) {}";
		String expected = "" +
				"String user = null;\n" +
				"String pass = null;\n" +
				"DirContext ctx = null;\n" +
				"String filter = \"(&(uid={0}\" + \")(userPassword={1}\" + \"))\";\n" +
				"try {\n" +
				"	NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new Object[] {user, pass}, new SearchControls());\n" +
				"} catch (NamingException e) {}";

		assertChange(original, expected);
	}

	public void sampleCode() {
		String user = null;
		String pass = null;
		DirContext ctx = null;

		String filter = "(&(uid=" + user + ")(userPassword=" + pass + "))";
		try {
			NamingEnumeration<SearchResult> results = ctx.search("ou=system", filter, new SearchControls());
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	public void expectedCode() {
		String user = null;
		String pass = null;
		DirContext ctx = null;

		String filter = "(&(uid={0}" + ")(userPassword={1}" + "))";
		try {
			NamingEnumeration<SearchResult> results = ctx.search("ou=system", filter, new String[] { user, pass },
					new SearchControls());
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

}
