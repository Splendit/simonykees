package eu.jsparrow.core;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.junit.Before;
import org.junit.Test;

import eu.jsparrow.core.util.ASTNodeUtil;
import eu.jsparrow.core.visitor.renaming.FieldReferencesSearchEngine;
import eu.jsparrow.core.visitor.renaming.ReferenceSearchMatch;

@SuppressWarnings("nls")
public class FieldReferencesSearchEngineTest extends AbstractRulesTest {

	private static final String ROOT_PACKAGE_NAME = "eu.jsparrow.core";
	private static final String CORE_PACKAGE = "package " + ROOT_PACKAGE_NAME + ";";
	private static final String FIELDS_IN_ANONYMOUS_CLASSES = CORE_PACKAGE + "\n"
			+ "public class FieldsInAnonymousClasses {\n" 
			+ "	public void avoidAnonymousClasses() {\n"
			+ "		Foo foo = new Foo() {\n" 
			+ "			\n" 
			+ "			public String foo_field;\n" 
			+ "			\n"
			+ "			@Override\n" 
			+ "			public void foo() {\n" 
			+ "				this.foo_field = \"\";\n"
			+ "			}\n" 
			+ "		};\n" 
			+ "		Foo foo2 = new Foo() {\n"
			+ "			public String foo_field;\n" 
			+ "			\n" 
			+ "			@Override\n"
			+ "			public void foo() {\n" 
			+ "				this.foo_field = \"\";\n" 
			+ "			}\n"
			+ "		};\n" 
			+ "	}\n" 
			+ "	abstract class Foo {\n" 
			+ "		public abstract void foo();\n" 
			+ "	}\n"
			+ "}";

	private static final Map<String, String> compilationUnitNameContents;
	static {
		Map<String, String> nameContents = new HashMap<>();
		nameContents.put("FieldsInAnonymousClasses.java", FIELDS_IN_ANONYMOUS_CLASSES);
		compilationUnitNameContents = Collections.unmodifiableMap(nameContents);
	}

	private IPackageFragment packageFragment;
	private List<CompilationUnit> compilationUnits;

	@Before
	public void setUpCompilationUnits() throws JavaModelException, IOException {
		packageFragment = root.createPackageFragment(ROOT_PACKAGE_NAME, true, null);
		compilationUnits = loadCompilationUnits(packageFragment, compilationUnitNameContents, ROOT_PACKAGE_NAME);
	}

	@Test
	public void referencesOfFields_anonymousClasses() {
		/*
		 * Having loaded two anonymous classes of the same type, both
		 * declaring a field with the same name
		 */
		List<VariableDeclarationFragment> declInAnonymousClasses = findDeclarationsInAnonymousClass();
		assertEquals(2, declInAnonymousClasses.size());

		/*
		 * When searching for the references of the first field
		 */
		VariableDeclarationFragment fragment = declInAnonymousClasses.get(0);
		FieldReferencesSearchEngine searchEngine = new FieldReferencesSearchEngine(
				new IJavaElement[] { packageFragment });

		List<ReferenceSearchMatch> references = searchEngine.findFieldReferences(fragment)
			.orElse(Collections.emptyList());
		
		/*
		 * Expecting references to be found...
		 * 
		 * FIXME: SIM-934 - indeed there is only one reference. But the search engine
		 * is confusing the references of the field in the second anonymous class
		 * with the ones in the first.
		 */
		assertEquals(2, references.size());
	}



	private List<VariableDeclarationFragment> findDeclarationsInAnonymousClass() {
		AnonymousClassFieldsVisitor visitor = new AnonymousClassFieldsVisitor();
		for(CompilationUnit cu : compilationUnits) {
			cu.accept(visitor);
		}
		return visitor.getFieldsInAnonymousClasses();
	}

	class AnonymousClassFieldsVisitor extends ASTVisitor {
		private List<VariableDeclarationFragment> fragments = new ArrayList<>();

		public boolean visit(FieldDeclaration field) {
			if (ASTNode.ANONYMOUS_CLASS_DECLARATION == field.getParent()
				.getNodeType()) {
				fragments.addAll(ASTNodeUtil.convertToTypedList(field.fragments(), VariableDeclarationFragment.class));
			}
			return true;
		}
		
		public List<VariableDeclarationFragment> getFieldsInAnonymousClasses() {
			return this.fragments;
		}
	}
}
