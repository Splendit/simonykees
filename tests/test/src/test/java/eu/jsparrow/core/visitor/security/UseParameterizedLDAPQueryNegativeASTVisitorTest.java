package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.common.UsesSimpleJDTUnitFixture;

public class UseParameterizedLDAPQueryNegativeASTVisitorTest extends UsesSimpleJDTUnitFixture {

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
	public void visit_PlusAssignOnFilterNullValue_shouldNotTransform() throws Exception {
		String original = "" +
				"		String user = null;\n" +
				"		String pass = null;\n" +
				"		DirContext ctx = null;\n" +
				"		String filter = null;\n" +
				"		filter += \"(&(uid=\";\n" +
				"		filter += user;\n" +
				"		filter += \")(userPassword=\";\n" +
				"		filter += pass;\n" +
				"		filter += \"))\";\n" +
				"		try {\n" +
				"			NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new SearchControls());\n"
				+
				"		} catch (NamingException e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		assertNoChange(original);
	}

	@Test
	public void visit_ReassignInitializedFilter_shouldNotTransform() throws Exception {
		String original = "" +
				"		String user = null;\n" +
				"		String pass = null;\n" +
				"		DirContext ctx = null;\n" +
				"		String filter = \"(&(uid=\";\n" +
				"		filter = null;\n" +
				"		filter += user;\n" +
				"		filter += \")(userPassword=\";\n" +
				"		filter += pass;\n" +
				"		filter += \"))\";\n" +
				"		try {\n" +
				"			NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new SearchControls());\n"
				+
				"		} catch (NamingException e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		assertNoChange(original);
	}

	@Test
	public void visit_FilterUsingIntegerInput_shouldNotTransform() throws Exception {
		String original = "" +
				"			int userId = 11111111;\n" +
				"			DirContext ctx = null;\n" +
				"			String filter = \"(uid=\" + userId + \")\";\n" +
				"			try {\n" +
				"				NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new SearchControls());\n"
				+
				"			} catch (NamingException e) {\n" +
				"				e.printStackTrace();\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_EqualsOperatorNotImmediatelyBefore_shouldNotTransform() throws Exception {
		String original = "" +
				"			String userId = null;\n" +
				"			DirContext ctx = null;\n" +
				"			String filter = \"(uid= \" + userId + \")\";\n" +
				"			try {\n" +
				"				NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new SearchControls());\n"
				+
				"			} catch (NamingException e) {\n" +
				"				e.printStackTrace();\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_ClosingParenthesisNotImmediatelyAfter_shouldNotTransform() throws Exception {
		String original = "" +
				"			String userId = null;\n" +
				"			DirContext ctx = null;\n" +
				"			String filter = \"(uid=\" + userId + \" )\";\n" +
				"			try {\n" +
				"				NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new SearchControls());\n"
				+
				"			} catch (NamingException e) {\n" +
				"				e.printStackTrace();\n" +
				"			}";
		assertNoChange(original);
	}

	@Test
	public void visit_ConcatenationInAnonymousBlock_shouldNotTransform() throws Exception {
		String original = "" +
				"		DirContext ctx = null;\n" +
				"		String filter = \"(&(uid=\";\n" +
				"		{\n" +
				"			String user = null;\n" +
				"			String pass = null;\n" +
				"			filter += user;\n" +
				"			filter += \")(userPassword=\";\n" +
				"			filter += pass;\n" +
				"			filter += \"))\";\n" +
				"		}\n" +
				"		try {\n" +
				"			NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new SearchControls());\n"
				+
				"		} catch (NamingException e) {\n" +
				"			e.printStackTrace();\n" +
				"		}";
		assertNoChange(original);
	}

	@Test
	public void visit_FilterInitializationAsMethodArgument_shouldNotTransform() throws Exception {
		String original = "" +
				"	class FilterInitializationAsMethodArgument {\n" +
				"\n" +
				"		String useFilter(String filter) {\n" +
				"			return filter;\n" +
				"		}\n" +
				"		public void test() {\n" +
				"			String user = null;\n" +
				"			String pass = null;\n" +
				"			DirContext ctx = null;\n" +
				"\n" +
				"			String filter;\n" +
				"			useFilter(filter = \"(&(uid=\" + user + \")(userPassword=\" + pass + \"))\");\n" +
				"			try {\n" +
				"				NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new SearchControls());\n"
				+
				"			} catch (NamingException e) {\n" +
				"				e.printStackTrace();\n" +
				"			}\n" +
				"		}\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	public void visit_ConditionalFilterInitialization_shouldNotTransform() throws Exception {
		String original = "" +
				"	class ConditionalFilterInitialization {\n" +
				"	\n" +
				"		public void test() {\n" +
				"			String user = null;\n" +
				"			String pass = null;\n" +
				"			DirContext ctx = null;\n" +
				"\n" +
				"			String filter;\n" +
				"			if (true)\n" +
				"				filter = \"(&(uid=\" + user + \")(userPassword=\" + pass + \"))\";\n" +
				"\n" +
				"			try {\n" +
				"				NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\", filter, new SearchControls());\n"
				+
				"			} catch (NamingException e) {\n" +
				"				e.printStackTrace();\n" +
				"			}\n" +
				"		}\n" +
				"	}";
		assertNoChange(original);
	}

	@Test
	public void visit_InvocationAsFilterArgument_shouldNotTransform() throws Exception {
		String original = "" +
				"	class InvocationAsFilterArgument {\n" + 
				"\n" + 
				"		String useFilter(String filter) {\n" + 
				"			return filter;\n" + 
				"		}\n" + 
				"		public void test() {\n" + 
				"			String user = null;\n" + 
				"			String pass = null;\n" + 
				"			DirContext ctx = null;\n" + 
				"			try {\n" + 
				"				NamingEnumeration<SearchResult> results = ctx.search(\"ou=system\",\n" + 
				"						useFilter(\"(&(uid=\" + user + \")(userPassword=\" + pass + \"))\"), new SearchControls());\n" + 
				"			} catch (NamingException e) {\n" + 
				"				e.printStackTrace();\n" + 
				"			}\n" + 
				"		}\n" + 
				"	}";
		assertNoChange(original);
	}

}
