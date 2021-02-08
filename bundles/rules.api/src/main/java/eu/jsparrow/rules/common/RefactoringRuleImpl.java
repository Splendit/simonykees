package eu.jsparrow.rules.common;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jface.text.Document;
import org.eclipse.ltk.core.refactoring.DocumentChange;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.TextEdit;
import org.osgi.framework.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.exception.RefactoringException;
import eu.jsparrow.rules.common.exception.runtime.ITypeNotFoundRuntimeException;
import eu.jsparrow.rules.common.statistics.RuleApplicationCount;
import eu.jsparrow.rules.common.util.RefactoringUtil;
import eu.jsparrow.rules.common.visitor.AbstractASTRewriteASTVisitor;

/**
 * Wrapper Class for {@link AbstractASTRewriteASTVisitor} that holds UI name,
 * description, if its enabled and the document changes for
 * {@link ICompilationUnit} that are processed
 * 
 * @author Martin Huter, Hannes Schweighofer, Ludwig Werzowa, Hans-Jörg Schrödl,
 *         Matthias Webhofer
 * @since 0.9
 *
 * @param <T>
 *            is the {@link AbstractASTRewriteASTVisitor} implementation that is
 *            applied by this rule
 */
public abstract class RefactoringRuleImpl<T extends AbstractASTRewriteASTVisitor> implements RefactoringRule {

	private static final Logger logger = LoggerFactory.getLogger(RefactoringRuleImpl.class);

	protected String id;

	protected RuleDescription ruleDescription;

	protected final String requiredJavaVersion;

	// default is true because of preferences page
	protected boolean enabled = true;
	protected boolean satisfiedJavaVersion = true;
	protected boolean satisfiedLibraries = true;

	protected Class<T> visitorClass;

	protected RefactoringRuleImpl() {
		this.requiredJavaVersion = provideRequiredJavaVersion();
	}

	/**
	 * the required java version of the implemented rule
	 * 
	 * @return
	 */
	protected abstract String provideRequiredJavaVersion();

	@Override
	public String getRequiredJavaVersion() {
		return requiredJavaVersion;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean isFree() {
		RuleDescription description = getRuleDescription();
		List<Tag> tags = description.getTags();
		return tags.contains(Tag.FREE);
	}

	public Class<T> getVisitor() {
		return visitorClass;
	}

	@Override
	public String getId() {
		return id;
	}

	/**
	 * Responsible to calculate if the rule is executable in the current
	 * project.
	 * 
	 * @param project
	 */
	@Override
	public void calculateEnabledForProject(IJavaProject project) {
		String compilerCompliance = project.getOption(JavaCore.COMPILER_COMPLIANCE, true);
		if (null == compilerCompliance) {
			/*
			 * if we cannot get the compiler compliance, we are unable to know
			 * whether or not the Java version is satisfied
			 */
			satisfiedJavaVersion = false;
		} else {
			satisfiedJavaVersion = JavaCore.compareJavaVersions(compilerCompliance, requiredJavaVersion) >= 0;
		}
		satisfiedLibraries = ruleSpecificImplementation(project);
		enabled = satisfiedJavaVersion && satisfiedLibraries;
	}

	/**
	 * JavaVersion independent requirements for rules that need to be defined
	 * for each rule. Returns true as default implementation
	 * 
	 * @param project
	 * @return
	 */
	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		return true;
	}

	protected AbstractASTRewriteASTVisitor visitorFactory() throws InstantiationException, IllegalAccessException {
		AbstractASTRewriteASTVisitor visitor = visitorClass.newInstance();
		visitor.addRewriteListener(RuleApplicationCount.getFor(this));
		return visitor;
	}

	/**
	 * Responsible to calculate of the rule is executable in the current
	 * project.
	 * 
	 */
	@Override
	public final DocumentChange applyRule(ICompilationUnit workingCopy, CompilationUnit astRoot)
			throws ReflectiveOperationException, JavaModelException, RefactoringException {

		String bind = NLS.bind(Messages.RefactoringRule_applying_rule_to_workingcopy, this.getRuleDescription()
			.getName(), workingCopy.getElementName());
		logger.trace(bind);

		return applyRuleImpl(workingCopy, astRoot);
	}

	/**
	 * This method may be overridden.
	 * 
	 * @param workingCopy
	 * 
	 * @return
	 * @throws ReflectiveOperationException
	 * @throws JavaModelException
	 * @throws RefactoringException
	 */
	protected DocumentChange applyRuleImpl(ICompilationUnit workingCopy, CompilationUnit astRoot)
			throws ReflectiveOperationException, JavaModelException, RefactoringException {

		final ASTRewrite astRewrite = ASTRewrite.create(astRoot.getAST());

		AbstractASTRewriteASTVisitor rule = visitorFactory();
		rule.setASTRewrite(astRewrite);
		rule.setCompilationUnit(workingCopy.getHandleIdentifier());
		try {
			astRoot.accept(rule);
		} catch (RuntimeException e) {
			throw new RefactoringException(e);
		}

		Document document = new Document(workingCopy.getSource());
		TextEdit edits = astRewrite.rewriteAST(document, workingCopy.getJavaProject()
			.getOptions(true));

		if (edits.hasChildren()) {

			/*
			 * The TextEdit instance changes as soon as it is applied to the
			 * working copy. This results in an incorrect preview of the
			 * DocumentChange. To fix this issue, a copy of the TextEdit is used
			 * for the DocumentChange.
			 */
			DocumentChange documentChange = RefactoringUtil.generateDocumentChange(visitorClass.getSimpleName(),
					document, edits.copy());

			workingCopy.applyTextEdit(edits, null);

			return documentChange;
		} else {
			return null;
		}
	}

	/**
	 * Independent library requirements for rules that need to be defined for
	 * each rule. Returns null as default implementation
	 * 
	 * @return String value of required library fully qualified class name
	 */
	@Override
	public String requiredLibraries() {
		return null;
	}

	/**
	 * Helper method for description building. Saves information if java version
	 * is satisfied for rule on selected project.
	 * 
	 * @return true if rule can be applied according to java version, false
	 *         otherwise
	 */
	@Override
	public boolean isSatisfiedJavaVersion() {
		return satisfiedJavaVersion;
	}

	/**
	 * Helper method for description building. Saves information if required
	 * libraries are satisfied for rule on selected project.
	 * 
	 * @return true if rule can be applied according to required libraries,
	 *         false otherwise
	 */
	@Override
	public boolean isSatisfiedLibraries() {
		return satisfiedLibraries;
	}

	@Override
	public RuleDescription getRuleDescription() {
		return this.ruleDescription;
	}

	/**
	 * If the given type name is present in the classpath of the project and the
	 * version of the found library satisfies the provided predicate.
	 * 
	 * @param project
	 *            the project being analyzed.
	 * @param fullyQuallifiedClassName
	 *            the fully qualified type name to search for.
	 * @param versionComparator
	 *            a predicate for verifying the library version.
	 * @return if a library containing the qualified type name is found and the
	 *         version of the library satisfies the given predicate.
	 */
	protected boolean isInProjectLibraries(IJavaProject project, String fullyQuallifiedClassName,
			Predicate<Version> versionComparator) {
		try {
			IType classtype = project.findType(fullyQuallifiedClassName);
			if (classtype != null) {

				IPackageFragmentRoot thirdPartyLib = getProject(classtype.getParent());
				if (thirdPartyLib != null) {
					// file with path to library jar
					IPath resourcePath = thirdPartyLib.getPath();
					File file = new File(resourcePath.toString());

					return isImplementationVersionValid(file, versionComparator);
				}
			} else {
				String loggerDebug = NLS.bind(Messages.StringUtilsRule_classNotInClassPath, fullyQuallifiedClassName);
				logger.debug(loggerDebug);
			}
		} catch (JavaModelException e) {
			logger.error(e.getMessage(), new ITypeNotFoundRuntimeException());
		}
		return false;
	}

	protected IPackageFragmentRoot getProject(IJavaElement iJavaElement) {
		if (null == iJavaElement) {
			return null;
		}
		IJavaElement parent = iJavaElement.getParent();
		if (null == parent || parent instanceof IPackageFragmentRoot) {
			return (IPackageFragmentRoot) parent;
		}
		return getProject(parent);
	}

	private boolean isImplementationVersionValid(File file, Predicate<Version> versionComparator) {
		try (JarFile jar = new JarFile(file)) {

			Manifest manifest = jar.getManifest();
			Attributes attributes = manifest.getMainAttributes();

			if (attributes != null) {
				for (Object attribute : attributes.keySet()) {
					Name key = (Name) attribute;
					String keyword = key.toString();
					if ("Implementation-Version".equals(keyword)) { //$NON-NLS-1$
						Version actualVersion = Version.parseVersion(attributes.getValue(key));
						return versionComparator.test(actualVersion);
					}
				}
			}
		} catch (IOException e) {
			logger.debug("Jar Manifest load error in:", e); //$NON-NLS-1$
			// Resolving version failed, rule can't be executed
		}
		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RefactoringRuleImpl<?> other = (RefactoringRuleImpl<?>) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Rule [id=" + id + ", name=" + this.getRuleDescription() //$NON-NLS-1$ //$NON-NLS-2$
			.getName() + "]"; //$NON-NLS-1$
	}

}
