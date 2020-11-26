package eu.jsparrow.core.rule.impl;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.osgi.framework.Version;

import eu.jsparrow.core.exception.runtime.ITypeNotFoundRuntimeException;
import eu.jsparrow.core.visitor.junit.ReplaceExpectedExceptionByAssertThrowsASTVisitor;
import eu.jsparrow.i18n.Messages;
import eu.jsparrow.rules.common.RefactoringRuleImpl;
import eu.jsparrow.rules.common.RuleDescription;
import eu.jsparrow.rules.common.Tag;

public class ReplaceExpectedExceptionByAssertThrowsRule
		extends RefactoringRuleImpl<ReplaceExpectedExceptionByAssertThrowsASTVisitor> {

	private static final Logger logger = LoggerFactory
		.getLogger(ReplaceExpectedExceptionByAssertThrowsRule.class);

	private static final String ORG_JUNIT_ASSERT = "org.junit.Assert";
	private static final String MIN_JUNIT_4_VERSION = "4.13";
	private static final String MIN_JUNIT_5_VERSION = "5.0";

	public ReplaceExpectedExceptionByAssertThrowsRule() {
		this.visitorClass = ReplaceExpectedExceptionByAssertThrowsASTVisitor.class;
		this.id = "ReplaceExpectedWithAssertThrows"; //$NON-NLS-1$
		this.ruleDescription = new RuleDescription(
				"Replace ExpectedException rule with assertThrows",
				"The expected exception rule is deperecated since 4.13. Assert throws should be used instead.",
				Duration.ofMinutes(10), Arrays.asList(Tag.JAVA_1_1, Tag.JUNIT));
	}

	@Override
	protected String provideRequiredJavaVersion() {
		return JavaCore.VERSION_1_8;
	}

	@Override
	public boolean ruleSpecificImplementation(IJavaProject project) {
		String fullyQuallifiedClassName = ORG_JUNIT_ASSERT;
		Predicate<Version> versionComparator = version -> version
			.compareTo(Version.parseVersion(MIN_JUNIT_4_VERSION)) >= 0
				|| version.compareTo(Version.parseVersion(MIN_JUNIT_5_VERSION)) >= 0;

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

	private IPackageFragmentRoot getProject(IJavaElement iJavaElement) {
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

}
