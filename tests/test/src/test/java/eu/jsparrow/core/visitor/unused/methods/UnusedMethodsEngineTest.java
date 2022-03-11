package eu.jsparrow.core.visitor.unused.methods;

import static eu.jsparrow.core.visitor.unused.methods.UnusedMethodsSampleSources.BLACK_HOLE;
import static eu.jsparrow.core.visitor.unused.methods.UnusedMethodsSampleSources.ROOT_PACKAGE_NAME;
import static eu.jsparrow.core.visitor.unused.methods.UnusedMethodsSampleSources.UNUSED_PACKAGE_PRIVATE_METHODS;
import static eu.jsparrow.core.visitor.unused.methods.UnusedMethodsSampleSources.UNUSED_PRIVATE_METHODS;
import static eu.jsparrow.core.visitor.unused.methods.UnusedMethodsSampleSources.UNUSED_PROTECTED_METHODS;
import static eu.jsparrow.core.visitor.unused.methods.UnusedMethodsSampleSources.UNUSED_PROTECTED_METHODS_SUBCLASS;
import static eu.jsparrow.core.visitor.unused.methods.UnusedMethodsSampleSources.UNUSED_PUBLIC_METHODS;
import static eu.jsparrow.core.visitor.unused.methods.UnusedMethodsSampleSources.UNUSED_PUBLIC_METHODS_SUBCLASS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.jsparrow.core.AbstractRulesTest;
import eu.jsparrow.core.renaming.RenamingTestHelper;
import eu.jsparrow.core.visitor.unused.method.UnusedMethodWrapper;
import eu.jsparrow.core.visitor.unused.method.UnusedMethodsEngine;

class UnusedMethodsEngineTest extends AbstractRulesTest {

	private UnusedMethodsEngine unusedMethodsEngine;
	private IPackageFragment packageFragment;
	private Map<String, String> compilationUnitNameContents;
	
	
	@BeforeEach
	void setUp() throws Exception {
		unusedMethodsEngine = new UnusedMethodsEngine("Project");
		packageFragment = root.createPackageFragment(ROOT_PACKAGE_NAME, true, null);
		Map<String, String> contents = new HashMap<>();
		contents.put("BlackHole.java", BLACK_HOLE);
		contents.put("UnusedPublicMethods.java", UNUSED_PUBLIC_METHODS);
		contents.put("UnusedPublicMethodsSubclass.java", UNUSED_PUBLIC_METHODS_SUBCLASS);
		contents.put("UnusedProtectedMethods.java", UNUSED_PROTECTED_METHODS);
		contents.put("UnusedProtectedMethodsSubclass.java", UNUSED_PROTECTED_METHODS_SUBCLASS);
		contents.put("UnusedPackagePrivateMethods.java", UNUSED_PACKAGE_PRIVATE_METHODS);
		contents.put("UnusedPrivateMethods.java", UNUSED_PRIVATE_METHODS);
		compilationUnitNameContents = Collections.unmodifiableMap(contents);
	}
	
	@Test
	void test_allAccessModifiers_shouldFind9Unused() throws Exception {
		Map<String, Boolean> options = new HashMap<String, Boolean>();
		options.put("private-methods", true);
		options.put("protected-methods", true);
		options.put("package-private-methods", true);
		options.put("public-methods", true);
		NullProgressMonitor progressMonitor = new NullProgressMonitor();
		SubMonitor subMonitor = SubMonitor.convert(progressMonitor, 100);
		List<CompilationUnit> compilationUnits = RenamingTestHelper
				.loadCompilationUnitsFromString(packageFragment, compilationUnitNameContents);
		List<ICompilationUnit> icus = compilationUnits.stream()
				.map(cu -> (ICompilationUnit) cu.getJavaElement())
				.collect(Collectors.toList());
		
		List<UnusedMethodWrapper> unused = unusedMethodsEngine.findUnusedMethods(icus , options, subMonitor);
		Set<ICompilationUnit> targetCompilationUnits = unusedMethodsEngine.getTargetCompilationUnits();
		assertEquals(4, unused.size());
		assertEquals(4, targetCompilationUnits.size());
	}
}
