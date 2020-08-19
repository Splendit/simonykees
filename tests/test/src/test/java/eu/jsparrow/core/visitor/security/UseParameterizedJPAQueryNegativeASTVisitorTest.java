package eu.jsparrow.core.visitor.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.visitor.impl.UsesJDTUnitFixture;

public class UseParameterizedJPAQueryNegativeASTVisitorTest extends UsesJDTUnitFixture {

	@BeforeEach
	public void setUp() throws Exception {
		addDependency("javax.persistence", "persistence-api", "1.0.2");
		defaultFixture.addImport("javax.persistence.EntityManager");
		defaultFixture.addImport("javax.persistence.Query");
		setDefaultVisitor(new UseParameterizedJPAQueryASTVisitor());

	}

	@AfterEach
	public void tearDown() throws Exception {
		fixtureProject.clear();
	}

	@Test
	public void visit_MethodHasNoExpression_shouldNotTransform() throws Exception {
		String original = "" +
				"		Query createQuery(String query) {\n" +
				"			return null;\n" +
				"		}\n" +
				"		void test() {\n" +
				"			String orderId = \"100000000\";\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"			jpqlQuery.getResultList();\n" +
				"		}\n" +
				"";

		assertNoChange(original);
	}

	@Test
	public void visit_MethodHasNotRequiredExpressionType_shouldNotTransform() throws Exception {
		String original = "" +
				"		static class FakeEntityManager {\n" +
				"			Query createQuery(String query) {\n" +
				"				return null;\n" +
				"			}\n" +
				"		}\n" +
				"		void test() {\n" +
				"			String orderId = \"100000000\";\n" +
				"			FakeEntityManager entityManager = new FakeEntityManager();\n" +
				"			Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"			jpqlQuery.getResultList();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	public void visit_MethodHasNotRequiredName_shouldNotTransform() throws Exception {
		String original = "" +
				"void test() {\n" +
				"			String orderId = \"100000000\";\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = entityManager\n" +
				"					.createNamedQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"			jpqlQuery.getResultList();\n" +
				"		}";
		assertNoChange(original);
	}

	@Test
	public void visit_QueryWithMissingEqualsOperatorBeforeInput_shouldNotTransform() throws Exception {
		String original = "" +
				"		void test() {\n" +
				"			String orderId = \"1\";\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id \" + orderId);\n"
				+
				"			jpqlQuery.getResultList();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	public void visit_EmptyStringLiteralAfterInput_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		String orderId = \"1\";\n" +
				"		EntityManager entityManager = null;\n" +
				"		Query jpqlQuery = entityManager\n" +
				"				.createQuery(\"Select order from Orders order where order.id  = \" + orderId + \"\");\n"
				+
				"			jpqlQuery.getResultList();\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_StringLiteralWithQuotationMarkAfterInput_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		String orderId = \"1\";\n" +
				"		EntityManager entityManager = null;\n" +
				"		Query jpqlQuery = entityManager\n" +
				"				.createQuery(\"Select order from Orders order where order.id  = \" + orderId + \" '\");\n"
				+
				"		jpqlQuery.getResultList();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	public void visit_QueryWitMissingSpaceAfterInput_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		String orderId = \"1\";\n" +
				"		EntityManager entityManager = null;\n" +
				"		Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId + \"000\");\n"
				+
				"		jpqlQuery.getResultList();\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void TestCreateQueryContainingOnlyStringLiterals() throws Exception {
		String original = "" +
				"		void test() {\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = entityManager\n" +
				"					.createQuery(\"Select order from Orders order where order.id = \" + \"100000000\");\n"
				+
				"			jpqlQuery.getResultList();\n" +
				"		}";
		assertNoChange(original);
	}

	@Test
	public void visit_WhereKeywordNotFound_shouldNotTransform() throws Exception {
		String original = "" +
				"		void test() {\n" +
				"			String orderId = \"100000000\";\n" +
				"			EntityManager entityManager = null;\n" +
				"			String whereKeyword = \"where\";\n" +
				"			Query jpqlQuery = entityManager\n" +
				"					.createQuery(\"Select order from Orders order \" + whereKeyword + \" order.id = \" + orderId);\n"
				+
				"			jpqlQuery.getResultList();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	public void visit_QueryDeclaredAsField_shouldNotTransform() throws Exception {
		String original = "" +
				"		private EntityManager entityManager = null;\n" +
				"		private Query jpqlQuery;\n" +
				"		void test() {\n" +
				"			String orderId = \"100000000\";\n" +
				"			jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"			jpqlQuery.getResultList();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	public void visit_QueryUsedAsInvocationArgument_shouldNotTransform() throws Exception {
		String original = "" +
				"	void useQuery(Query jpqlQuery) {\n" +
				"		}\n" +
				"		void test() {\n" +
				"			String orderId = \"100000000\";\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"			jpqlQuery.getResultList();\n" +
				"			useQuery(jpqlQuery);\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	public void visit_QueryAsInitializerOfOtherVariable_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" +
				"		String orderId = \"100000000\";\n" +
				"		EntityManager entityManager = null;\n" +
				"		Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"		jpqlQuery.getResultList();\n" +
				"		Query jpqlQuery1 = jpqlQuery;\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_QueryUsedAsAssignementRHS_shouldNotTransform() throws Exception {
		String original = "" +
				"		void test() {\n" +
				"			String orderId = \"100000000\";\n" +
				"			EntityManager entityManager = null;			\n" +
				"			Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);			\n"
				+
				"			jpqlQuery.getResultList();\n" +
				"			Query jpqlQuery1;\n" +
				"			jpqlQuery1 = jpqlQuery;\n" +
				"		}";
		assertNoChange(original);
	}

	@Test
	public void visit_InitializedQueryReAssigned_shouldNotTransform() throws Exception {
		String original = "" +
				"		void test() {\n" +
				"			String orderId = \"100000000\";\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"			jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"			jpqlQuery.getResultList();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	public void visit_QueryNeverExecuted_shouldNotTransform() throws Exception {
		String original = "" +
				"		void test() {\n" +
				"			String orderId = \"100000000\";\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"		}";

		assertNoChange(original);

	}

	@Test
	public void visit_QueryWithSetMaxResultNeverExecuted_shouldNotTransform() throws Exception {
		String original = "" +
				"		void test() {\n" +
				"			String orderId = \"100000000\";\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+ "			jpqlQuery.setMaxResults(1000);" +
				"		}";

		assertNoChange(original);

	}

	@Test
	public void visit_QueryExecutedMoreThanOnce_shouldNotTransform() throws Exception {
		String original = "" +
				"		void test() {\n" +
				"			String orderId = \"100000000\";\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"			jpqlQuery.getResultList();\n" +
				"			jpqlQuery.getSingleResult();\n" +
				"		}";

		assertNoChange(original);

	}

	@Test
	public void visit_QueryWithSetParameterInvocation_shouldNotTransform() throws Exception {
		String original = ""
				+ "		void test() {\n" +
				"			String firstName = \"Max\";\n" +
				"			String lastName = \"Mustermann\";\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery = entityManager.createQuery( //\n" +
				"					\"Select id from Persons p where p.firstName = ?1 and p.lastName = \" + lastName);\n"
				+
				"			jpqlQuery.setParameter(1, firstName);\n" +
				"			jpqlQuery.getResultList();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	public void visit_QualifiedQueryVariable_shouldNotTransform() throws Exception {

		String original = "" +
				"		void test() {\n" +
				"			class LocalClass {\n" +
				"				 Query jpqlQuery;\n" +
				"			}\n" +
				"			String orderId = \"100000000\";\n" +
				"			EntityManager entityManager = null;\n" +
				"			LocalClass localClass = new LocalClass();\n" +
				"			localClass.jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"			localClass.jpqlQuery.getResultList();\n" +
				"		}";

		assertNoChange(original);
	}

	@Test
	public void visit_QueryVariableWithSubsequentSetParameter_shouldNotTransform() throws Exception {

		String original = "" +
				"	void test() {\n" +
				"		String orderId = \"100000000\";\n" +
				"		EntityManager entityManager = null;\n" +
				"		Query jpqlQuery = entityManager\n" +
				"				.createQuery(\"Select order from Orders order where order.id = \" + orderId + \" or order.id = ?1\")\n"
				+
				"				.setParameter(1, \"200000000\");\n" +
				"		jpqlQuery.getResultList();\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_createQueryInFieldDeclaration_shouldNotTransform() throws Exception {
		String original = "" +
				"	EntityManager entityManager = null;\n" +
				"	String orderId = \"100000000\";		\n" +
				"	Query jpqlQuery = entityManager.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n"
				+
				"	void test() {\n" +
				"		jpqlQuery.getResultList();\n" +
				"	}";

		assertNoChange(original);
	}

	@Test
	public void visit_CreateQueryInAssignmentChain_shouldNotTransform() throws Exception {
		String original = "" +
				"		void test() {\n" +
				"			String orderId = \"100000000\";\n" +
				"			EntityManager entityManager = null;\n" +
				"			Query jpqlQuery1;\n" +
				"			Query jpqlQuery2;\n" +
				"			jpqlQuery1 = jpqlQuery2 = entityManager\n" +
				"					.createQuery(\"Select order from Orders order where order.id = \" + orderId);\n" +
				"			jpqlQuery2.getResultList();\n" +
				"		}";
		assertNoChange(original);
	}

	@Test
	public void visit_HiddenInitialization_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" + 
				"		EntityManager entityManager = null;\n" + 
				"		String query;\n" + 
				"		{\n" + 
				"			String orderId = \"100000000\";\n" + 
				"			query = \"Select order from Orders order where order.id = \" + orderId;\n" + 
				"		}\n" + 
				"		Query jpqlQuery = entityManager.createQuery(query);\n" + 
				"		jpqlQuery.getResultList();\n" + 
				"	}";
		assertNoChange(original);
	}
	
	@Test
	public void visit_HiddenPlusAssign_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" + 
				"		EntityManager entityManager = null;\n" + 
				"		String query;\n" + 
				"		String orderId = \"100000000\";\n" + 
				"		query = \"Select order from Orders order where order.id = \" + orderId;\n" + 
				"		{\n" + 
				"			String orderId2 = \"200000000\";\n" + 
				"			query += \" or order.id = \" + orderId2;\n" + 
				"		}\n" + 
				"		Query jpqlQuery = entityManager.createQuery(query);\n" + 
				"		jpqlQuery.getResultList();\n" + 
				"	}";
		assertNoChange(original);
	}
	
	@Test
	public void visit_PlusAssignToNullValue_shouldNotTransform() throws Exception {
		String original = "" +
				"	void test() {\n" + 
				"		EntityManager entityManager = null;\n" + 
				"		String query = null;\n" + 
				"		String orderId = \"100000000\";\n" + 
				"		query += \"Select order from Orders order where order.id = \" + orderId;\n" + 
				"		\n" + 
				"		Query jpqlQuery = entityManager.createQuery(query);\n" + 
				"		jpqlQuery.getResultList();\n" + 
				"	}";
		assertNoChange(original);
	}

}
