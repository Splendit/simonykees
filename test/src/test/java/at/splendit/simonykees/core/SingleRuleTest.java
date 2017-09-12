package at.splendit.simonykees.core;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.junit.BeforeClass;

import at.splendit.simonykees.core.refactorer.RefactoringPipeline;
import at.splendit.simonykees.core.rule.RefactoringRule;
import at.splendit.simonykees.core.util.RulesTestUtil;

public abstract class SingleRuleTest {

	protected static IPackageFragmentRoot root;

	protected IJavaProject testproject;
	
	private static final String POSTRULE_BASE_PACKAGE = RulesTestUtil.BASE_PACKAGE + ".postRule."; //$NON-NLS-1$
	
	private static final String POSTRULE_BASE_DIRECTORY = RulesTestUtil.BASE_DIRECTORY + "/postRule/"; //$NON-NLS-1$

	@BeforeClass
	public static void classSetUp() throws Exception {
		root = RulesTestUtil.getPackageFragementRoot(JavaCore.VERSION_1_8);
	}

	protected String applyRefactoring(RefactoringRule<?> rule, Path preFile) throws Exception {
		String packageString = "at.splendit.simonykees.sample.preRule"; //$NON-NLS-1$
		IPackageFragment packageFragment = root.createPackageFragment(packageString, true, null);
		String fileName = preFile.getFileName().toString();
		String content = new String(Files.readAllBytes(preFile), StandardCharsets.UTF_8);

		ICompilationUnit compilationUnit = packageFragment.createCompilationUnit(fileName, content, true, null);

		List<IJavaElement> javaElements = new ArrayList<>();
		javaElements.add(compilationUnit);

		RefactoringPipeline refactoringPipeline = new RefactoringPipeline(Arrays.asList(rule));
		IProgressMonitor monitor = new NullProgressMonitor();

		refactoringPipeline.prepareRefactoring(javaElements, monitor);
		refactoringPipeline.doRefactoring(monitor);
		refactoringPipeline.commitRefactoring();

		return compilationUnit.getSource();
	}

	protected String replacePackageName(String compilationUnitSource, String postRulePackage) {
		return compilationUnitSource = StringUtils.replace(compilationUnitSource, RulesTestUtil.PRERULE_PACKAGE,
				postRulePackage);
	}
	
	protected Path getPreRuleFile(String fileName){
		return Paths.get(RulesTestUtil.PRERULE_DIRECTORY, fileName);
	}
	
	protected Path getPostRuleFile(String fileName){
		return getPostRuleFile(fileName, POSTRULE_BASE_DIRECTORY);
	}
	
	protected Path getPostRuleFile(String fileName, String subdirectory){
		String postruleDirectory = RulesTestUtil.BASE_DIRECTORY + "/postRule/"+subdirectory; //$NON-NLS-1$
		return Paths.get(postruleDirectory, fileName);
	}
	
	protected String getPostRulePackage(String postRulePackage){
		return POSTRULE_BASE_PACKAGE + postRulePackage;
	}
	
	

}
