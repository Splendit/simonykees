package at.splendit.simonykees.core;

import static org.junit.Assert.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import at.splendit.simonykees.core.refactorer.AbstractRefactorer;
import at.splendit.simonykees.core.rule.RulesContainer;
import at.splendit.simonykees.core.util.RulesTestUtil;

@RunWith(Parameterized.class)
public class AllRulesTest {

	private String fileName;
	private Path preRule, postRule;
	
	public AllRulesTest(String fileName, Path preRule, Path postRule) {
		this.fileName = fileName;
		this.preRule = preRule;
		this.postRule = postRule;
	}
	
	@Parameters(name = "{index}: test file[{0}]")
	public static Collection<Object[]> data() throws Exception {
		List<Object[]> data = new ArrayList<>();
		for (Path preRulePath : Files.newDirectoryStream(Paths.get(RulesTestUtil.PRERULE_DIRECTORY), RulesTestUtil.RULE_SUFFIX)) { 
			Path postRulePath = Paths.get(RulesTestUtil.POSTRULE_DIRECTORY, preRulePath.getFileName().toString());
			data.add(new Object[] {preRulePath.getFileName().toString(), preRulePath, postRulePath});
		}
		return data;
	}
	
	@Test
	public void test() throws Exception {
		String expectedSource = new String(Files.readAllBytes(postRule));
		String content = new String(Files.readAllBytes(preRule));
		
		IPackageFragment packageFragment = RulesTestUtil.getPackageFragement();
		ICompilationUnit compilationUnit = packageFragment.createCompilationUnit(fileName, content, true, null);
		
		List<IJavaElement> javaElements = new ArrayList<>();
		javaElements.add(compilationUnit);
		
		AbstractRefactorer refactorer = new AbstractRefactorer(javaElements, RulesContainer.getAllRules()) {};
		
		refactorer.prepareRefactoring();
		refactorer.doRefactoring();
		refactorer.commitRefactoring();
		
		String compilationUnitSource = StringUtils.replace(compilationUnit.getSource(), RulesTestUtil.PRERULE_PACKAGE, RulesTestUtil.POSTRULE_PACKAGE);
		// TODO check if tabs and newlines make a difference
		assertEquals(expectedSource, compilationUnitSource);
	}
}
